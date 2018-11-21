package srl.paros.piccolchain.node.consumer;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.net.NetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class BroadcastTransactions implements Handler<Message<String>> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Set<String> peers;
    private NetClient netClient;

    public BroadcastTransactions(Set<String> peers, NetClient netClient) {
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
