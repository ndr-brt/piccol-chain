package srl.paros.piccolchain.node;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;
import srl.paros.piccolchain.domain.*;

import java.time.Instant;

import static srl.paros.piccolchain.Hostname.HOSTNAME;
import static srl.paros.piccolchain.domain.Transactions.transactions;

public class NodeVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Transactions transactions = transactions();
    private final Blockchain blockchain = Blockchain.blockchain();
    private final String name;

    public NodeVerticle() {
        this.name = HOSTNAME.get();
    }

    @Override
    public void start() {
        final var router = Router.router(vertx);
        final var eventBus = vertx.eventBus();

        router.get("/")
                .handler(context -> {
                    context.put("name", name);
                    context.put("transactions", transactions.get());
                    context.put("blocks", blockchain.blocks());
                })
                .handler(new StaticContentHandler("templates/", "node.html"));

        router.post("/transactions")
                .consumes("application/json")
                .handler(context -> context.request().bodyHandler(buffer -> {
                    String body = buffer.toString();
                    Transaction transaction = Json.fromJson(body, Transaction.class);
                    transactions.append(transaction);
                    log.info("New transaction: {}", body);
                    eventBus.publish("transaction", transaction);
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
                    eventBus.publish("transaction", transaction);

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
            eventBus.publish("block", newBlock);

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

}
