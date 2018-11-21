package srl.paros.piccolchain.node.consumer;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.net.NetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.node.domain.Peers;

public class BroadcastTransactions implements Handler<Message<String>> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Peers peers;
    private NetClient netClient;

    public BroadcastTransactions(Peers peers, NetClient netClient) {
        this.peers = peers;
        this.netClient = netClient;
    }

    @Override
    public void handle(Message<String> message) {
        peers.forEach(peer -> {
            netClient.connect(4568, peer, it -> {
                log.info("Connect to {}", peer);
                if (it.succeeded()) {
                    it.result().end(Buffer.buffer("transaction:" + message.body()));
                }
            });
        });
    }
}
