package srl.paros.piccolchain.node.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import srl.paros.piccolchain.node.domain.*;

import java.time.Instant;

import static srl.paros.piccolchain.Json.toJson;

public class Mine implements Handler<RoutingContext> {
    private final String name;
    private final Blockchain blockchain;
    private final Transactions transactions;

    public Mine(String name, Blockchain blockchain, Transactions transactions) {
        this.name = name;
        this.blockchain = blockchain;
        this.transactions = transactions;
    }

    @Override
    public void handle(RoutingContext context) {
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
        context.vertx().eventBus().publish("block", toJson(newBlock));

        context.response()
                .putHeader("Location", "/")
                .end(toJson(blockchain.last()));
    }
}
