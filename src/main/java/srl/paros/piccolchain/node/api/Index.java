package srl.paros.piccolchain.node.api;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import srl.paros.piccolchain.node.domain.Blockchain;
import srl.paros.piccolchain.node.domain.Transactions;

public class Index implements Handler<RoutingContext> {
    private final String name;
    private final Transactions transactions;
    private final Blockchain blockchain;
    private final ThymeleafTemplateEngine engine;

    public Index(String name, Transactions transactions, Blockchain blockchain) {
        this.name = name;
        this.transactions = transactions;
        this.blockchain = blockchain;
        engine = ThymeleafTemplateEngine.create();
    }

    @Override
    public void handle(RoutingContext context) {
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
    }
}
