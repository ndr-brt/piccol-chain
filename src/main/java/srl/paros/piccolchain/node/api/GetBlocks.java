package srl.paros.piccolchain.node.api;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import srl.paros.piccolchain.node.domain.Blockchain;

import static srl.paros.piccolchain.Json.toJson;

public class GetBlocks implements Handler<RoutingContext> {
    private Blockchain blockchain;

    public GetBlocks(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public void handle(RoutingContext context) {
        context.response().end(toJson(blockchain.blocks()));
    }
}
