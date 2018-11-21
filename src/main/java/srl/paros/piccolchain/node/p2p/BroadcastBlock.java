package srl.paros.piccolchain.node.p2p;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.net.NetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.node.domain.Peers;

public class BroadcastBlock implements Handler<Message<String>> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Peers peers;
    private final NetClient netClient;

    public BroadcastBlock(Peers peers, NetClient netClient) {
        this.peers = peers;
        this.netClient = netClient;
    }

    @Override
    public void handle(Message<String> message) {
        log.info("Broadcast block");
        peers.forEach(peer -> {
            netClient.connect(4568, peer, it -> {
                if (it.succeeded()) {
                    it.result().end(Buffer.buffer("block:" + message.body()));
                }
            });
        });
    }
}
