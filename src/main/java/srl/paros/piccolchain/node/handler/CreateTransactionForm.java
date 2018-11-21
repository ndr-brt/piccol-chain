package srl.paros.piccolchain.node.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import srl.paros.piccolchain.node.domain.Transaction;
import srl.paros.piccolchain.node.domain.Transactions;

import static srl.paros.piccolchain.Json.toJson;

public class CreateTransactionForm implements Handler<RoutingContext> {
    private Transactions transactions;

    public CreateTransactionForm(Transactions transactions) {
        this.transactions = transactions;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest req = context.request();
        Transaction transaction = new Transaction(
                req.getParam("from"),
                req.getParam("to"),
                Long.valueOf(req.getParam("amount"))
        );
        transactions.append(transaction);
        context.vertx().eventBus().publish("transaction", toJson(transaction));

        context.response()
                .putHeader("Location", "/")
                .end("Transaction created");
    }
}
