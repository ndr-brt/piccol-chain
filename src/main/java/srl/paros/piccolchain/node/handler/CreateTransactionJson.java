package srl.paros.piccolchain.node.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;
import srl.paros.piccolchain.node.domain.Transaction;
import srl.paros.piccolchain.node.domain.Transactions;

import static srl.paros.piccolchain.Json.toJson;

public class CreateTransactionJson implements Handler<RoutingContext> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Transactions transactions;

    public CreateTransactionJson(Transactions transactions) {
        this.transactions = transactions;
    }

    @Override
    public void handle(RoutingContext context) {
        context.request().bodyHandler(buffer -> {
            String body = buffer.toString();
            Transaction transaction = Json.fromJson(body, Transaction.class);
            transactions.append(transaction);
            log.info("New transaction: {}", transaction);
            context.vertx().eventBus().publish("transaction", toJson(transaction));
            context.response().end("Transaction created");
        });
    }
}
