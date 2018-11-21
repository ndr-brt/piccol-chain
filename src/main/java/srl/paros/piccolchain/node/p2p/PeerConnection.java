package srl.paros.piccolchain.node.p2p;

import io.vertx.core.Handler;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;
import srl.paros.piccolchain.node.domain.Block;
import srl.paros.piccolchain.node.domain.Blockchain;
import srl.paros.piccolchain.node.domain.Transaction;
import srl.paros.piccolchain.node.domain.Transactions;

public class PeerConnection implements Handler<NetSocket> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Blockchain blockchain;
    private final Transactions transactions;

    public PeerConnection(Blockchain blockchain, Transactions transactions) {
        this.blockchain = blockchain;
        this.transactions = transactions;
    }

    @Override
    public void handle(NetSocket socket) {
        log.info("New websocket connection from {}", socket.remoteAddress());

        socket.handler(buffer -> {
            String message = buffer.toString();
            log.info("Message: {}", message);
            int separatorIndex = message.indexOf(":");
            String type = message.substring(0, separatorIndex);
            String content = message.substring(separatorIndex + 1);
            log.info("Message received: {} - {}", type, content);
            switch (type) {
                case "transaction":
                    log.info("New transaction by peer, append");
                    transactions.append(Json.fromJson(content, Transaction.class));
                    break;
                case "block":
                    log.info("New block by peer, update chain and empty transactions");
                    blockchain.append(Json.fromJson(content, Block.class));
                    transactions.empty();
                    break;
                default:
                    log.warn("Message type unknown {}", type);
            }
        });

        socket.endHandler(it -> log.info("Client disconnected"));
    }
}
