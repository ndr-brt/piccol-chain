package srl.paros.piccolchain.node;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;
import srl.paros.piccolchain.domain.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

import static srl.paros.piccolchain.domain.Transactions.transactions;

public class NodeVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Transactions transactions = transactions();
    private final Blockchain blockchain = Blockchain.blockchain();
    private final String name;

    public NodeVerticle() {
        this.name = hostname.get();
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);

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
                    log.info("New transaction: {}", body);
                    // broadcast("transaction", body);
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
                    log.info("Create new transaction: {}",req.params());
                    transactions.append(transaction);
                    String json = Json.toJson(transaction);
                    //broadcast("transaction", json);
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
            //broadcast("block", Json.toJson(newBlock));

            context.response()
                    .putHeader("Location", "/")
                    .end(Json.toJson(blockchain.last()));
        });

        router.get("/blocks")
                .handler(context -> context.response().end(Json.toJson(blockchain.blocks())));

        router.exceptionHandler(error -> log.error("Error", error));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(4567);

        vertx.deployVerticle(new NodeConnection(name));

        vertx.exceptionHandler(error -> log.error("Error", error));
    }


    private final Supplier<String> hostname = () -> {
        try {
            String name = InetAddress.getLocalHost().getHostName();
            log.info("Node's name {}", name);
            return name;
        } catch (UnknownHostException e) {
            log.error("Error getting hostname, give an uuid", e);
            return UUID.randomUUID().toString();
        }
    };
}
