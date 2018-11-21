package srl.paros.piccolchain.node;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.node.api.*;
import srl.paros.piccolchain.node.p2p.BroadcastBlock;
import srl.paros.piccolchain.node.p2p.BroadcastTransactions;
import srl.paros.piccolchain.node.domain.Blockchain;
import srl.paros.piccolchain.node.domain.Peers;
import srl.paros.piccolchain.node.domain.Transactions;
import srl.paros.piccolchain.node.p2p.PeerConnection;
import srl.paros.piccolchain.node.p2p.Initialize;
import srl.paros.piccolchain.node.p2p.UpdatePeerList;

import java.util.concurrent.atomic.AtomicBoolean;

import static srl.paros.piccolchain.Hostname.HOSTNAME;
import static srl.paros.piccolchain.node.domain.Transactions.transactions;

public class Node extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Transactions transactions = transactions();
    private final Blockchain blockchain = Blockchain.blockchain();
    private final String name;
    private AtomicBoolean connected = new AtomicBoolean(false);
    private Peers peers = Peers.peers();

    public Node() {
        this.name = HOSTNAME.get();
    }

    @Override
    public void start() {
        final var router = Router.router(vertx);

        router.get("/").handler(new Index(name, transactions, blockchain));
        router.get("/mine").handler(new Mine(name, blockchain, transactions));
        router.get("/blocks").handler(new GetBlocks(blockchain));

        router.post("/transaction")
                .consumes("application/json")
                .handler(new CreateTransactionJson(transactions));

        router.post("/transactions")
                .consumes("application/x-www-form-urlencoded")
                .handler(BodyHandler.create())
                .handler(new CreateTransactionForm(transactions));

        router.exceptionHandler(error -> log.error("Error", error));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(4567);

        vertx.createNetServer()
                .connectHandler(new PeerConnection(blockchain, transactions))
                .listen(4568);

        vertx.eventBus().consumer("transaction", new BroadcastTransactions(peers, vertx.createNetClient()));
        vertx.eventBus().consumer("block", new BroadcastBlock(peers, vertx.createNetClient()));

        var httpClient = vertx.createHttpClient();
        vertx.setPeriodic(5000, new Initialize(name, connected, httpClient));
        vertx.setPeriodic(2000, new UpdatePeerList(name, connected, peers, httpClient));

        vertx.exceptionHandler(error -> log.error("Error", error));
    }

}
