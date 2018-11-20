package srl.paros.piccolchain.node;

import com.google.gson.reflect.TypeToken;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;
import srl.paros.piccolchain.domain.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static srl.paros.piccolchain.Hostname.HOSTNAME;
import static srl.paros.piccolchain.Json.toJson;
import static srl.paros.piccolchain.domain.Transactions.transactions;

public class Node extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Type SET_STRING = new TypeToken<Set<String>>() {}.getType();

    private final Transactions transactions = transactions();
    private final Blockchain blockchain = Blockchain.blockchain();
    private final String name;
    private boolean connected = false;
    private Set<String> peers;

    public Node() {
        this.name = HOSTNAME.get();
    }

    @Override
    public void start() {
        final var router = Router.router(vertx);
        final var eventBus = vertx.eventBus();

        var engine = ThymeleafTemplateEngine.create();
        router.get("/").handler(context -> {
                    context.put("name", name);
                    context.put("transactions", transactions.get());
                    context.put("blocks", blockchain.blocks());
                    engine.render(context, "templates/", "node.html", res -> {
                        if (res.succeeded()) {
                            context.response().end(res.result());
                        } else {
                            context.fail(res.cause());
                        }
                    });
                });

        router.post("/transactions")
                .consumes("application/json")
                .handler(context -> context.request().bodyHandler(buffer -> {
                    String body = buffer.toString();
                    Transaction transaction = Json.fromJson(body, Transaction.class);
                    transactions.append(transaction);
                    log.info("New transaction: {}", transaction);
                    eventBus.publish("transaction", toJson(transaction));
                    context.response().end("Transaction created");
                }));

        router.post("/transactions")
                .consumes("application/x-www-form-urlencoded")
                .handler(BodyHandler.create())
                .handler(context -> {
                    HttpServerRequest req = context.request();
                    Transaction transaction = new Transaction(
                            req.getParam("from"),
                            req.getParam("to"),
                            Long.valueOf(req.getParam("amount"))
                    );
                    transactions.append(transaction);
                    eventBus.publish("transaction", toJson(transaction));

                    context.response()
                            .putHeader("Location", "/")
                            .end("Transaction created");
                });

        router.get("/mine").handler(context -> {
            Block lastBlock = blockchain.last();
            int lastProofOfWork = lastBlock.data().proofOfWork();
            Integer proof = ProofOfWork.proofOfWork.apply(lastProofOfWork);

            transactions.append(new Transaction("network", name, 1));

            Block newBlock = new Block(
                    lastBlock.index() + 1,
                    Instant.now().toEpochMilli(),
                    new Data(proof, transactions.get()),
                    lastBlock.hash()
            );

            blockchain.append(newBlock);
            transactions.empty();
            eventBus.publish("block", toJson(newBlock));

            context.response()
                    .putHeader("Location", "/")
                    .end(toJson(blockchain.last()));
        });

        router.get("/blocks").handler(context -> context.response().end(toJson(blockchain.blocks())));

        router.exceptionHandler(error -> log.error("Error", error));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(4567);

        vertx.createNetServer().connectHandler(socket -> {
            log.info("New websocket connection from {}", socket.remoteAddress());

            socket.handler(buffer -> {
                String message = buffer.toString();
                log.info("Message: {}", message);
                int separatorIndex = message.indexOf(":");
                String type = message.substring(0, separatorIndex);
                String content = message.substring(separatorIndex + 1);
                log.info("Message received: {} - {}", type, content);
                switch (type) {
                    case "transaction":
                        log.info("New transaction by peer, append");
                        transactions.append(Json.fromJson(content, Transaction.class));
                        break;
                    case "block":
                        log.info("New block by peer, update chain and empty transactions");
                        blockchain.append(Json.fromJson(content, Block.class));
                        transactions.empty();
                        break;
                    default:
                        log.warn("Message type unknown {}", type);
                }
            });

            socket.endHandler(it -> log.info("Client disconnected"));
        }).listen(4568);

        eventBus.consumer("transaction", message -> {
            peers.forEach(peer -> {
                vertx.createNetClient().connect(4568, peer, it -> {
                    log.info("Connect to {}", peer);
                    if (it.succeeded()) {
                        it.result().end(Buffer.buffer("transaction:" + message.body()));
                    }
                });
            });
        });

        vertx.deployVerticle(new NodeConnection(name));

        var httpClient = vertx.createHttpClient();
        vertx.setPeriodic(2000, period -> {
            if (!connected) {
                httpClient.postAbs("http://guestlist:4567/nodes")
                        .handler(res -> eventBus.publish("connected", connected = res.statusCode() == 201))
                        .end(toJson(Map.of("name", name)));
            }
        });

        eventBus.consumer("connected", message -> {
            var connected = (boolean)message.body();
            if (connected) {
                httpClient.getAbs("http://guestlist:4567/nodes")
                        .handler(res -> res.bodyHandler(buffer -> {
                            Set<String> actualPeers = Json.fromJson(buffer.toString(), SET_STRING);
                            peers = actualPeers.stream()
                                    .filter(node -> !node.equals(name))
                                    .collect(Collectors.toSet());
                            log.info("Ottenuta la lista dei peers: {}", peers);
                        }))
                        .end();
            }
        });

        vertx.exceptionHandler(error -> log.error("Error", error));
    }

}
