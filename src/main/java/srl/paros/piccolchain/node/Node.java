package srl.paros.piccolchain.node;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import spark.servlet.SparkApplication;
import srl.paros.piccolchain.*;

import java.time.Instant;
import java.util.UUID;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;
import static srl.paros.piccolchain.Transactions.transactions;

public class Node implements SparkApplication {

    private final Transactions transactions = transactions();
    private final Blockchain blockchain = new Blockchain();
    private UUID nodeId;

    public Node(UUID nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public void init() {
        post("/" + nodeId + "/", (req, res) -> {
            Transaction transaction = Json.fromJson(req.body(), Transaction.class);
            transactions.append(transaction);
            System.out.println("New transaction: " + req.body());
            return "Transaction created";
        });

        get("/" + nodeId + "/mine", (req, res) -> {
            Block lastBlock = blockchain.last();
            int lastProofOfWork = lastBlock.data().proofOfWork();
            Integer proof = ProofOfWork.proofOfWork.apply(lastProofOfWork);

            transactions.append(new Transaction("network", nodeId.toString(), 1));

            Block newBlock = new Block(
                    lastBlock.index() + 1,
                    Instant.now().toEpochMilli(),
                    new Data(proof, transactions.get()),
                    lastBlock.hash()
            );

            blockchain.append(newBlock);

            transactions.empty();

            return blockchain.last();
        }, Json::toJson);

        get("/" + nodeId + "/blocks", (req, res) -> blockchain.blocks(), Json::toJson);

        exception(Exception.class, (exception, request, response) -> System.err.println(exception));
    }

    public UUID id() {
        return nodeId;
    }
}
