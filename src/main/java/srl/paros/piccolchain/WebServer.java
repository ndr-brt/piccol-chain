package srl.paros.piccolchain;

import spark.servlet.SparkApplication;

import static spark.Spark.post;
import static srl.paros.piccolchain.Transactions.transactions;

public class WebServer implements SparkApplication {

    private final Transactions transactions = transactions();

    @Override
    public void init() {
        post("/", (req, res) -> {
            Transaction transaction = Json.fromJson(req.body(), Transaction.class);
            transactions.add(transaction);
            System.out.println("New transaction: " + req.body());
            return "Transaction created";
        });
    }
}
