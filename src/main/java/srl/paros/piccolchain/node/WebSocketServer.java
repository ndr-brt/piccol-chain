package srl.paros.piccolchain.node;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;
import srl.paros.piccolchain.domain.Block;
import srl.paros.piccolchain.domain.Blockchain;
import srl.paros.piccolchain.domain.Transaction;
import srl.paros.piccolchain.domain.Transactions;

import java.util.ArrayList;
import java.util.List;


@WebSocket
public class WebSocketServer implements WebSocketListener {

    private final Transactions transactions;
    private final Blockchain blockchain;
    private Logger log = LoggerFactory.getLogger(getClass());

    public WebSocketServer(Transactions transactions, Blockchain blockchain) {
        this.transactions = transactions;
        this.blockchain = blockchain;
    }

    @OnWebSocketMessage
    @Override
    public void onWebSocketText(String message) {
        int separatorIndex = message.indexOf(":");
        String type = message.substring(0, separatorIndex);
        String content = message.substring(separatorIndex + 1);
        switch (type) {
            case "transaction":
                log.info("New transaction by peer, append");
                transactions.append(Json.fromJson(content, Transaction.class));
                break;
            case "block":
                log.info("New block by peer, update chain");
                blockchain.append(Json.fromJson(content, Block.class));
                break;
            default:
                log.warn("Message type unknown {}", type);
        }
    }

    @OnWebSocketClose
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        log.info("Close websocket connection: {}, {}", statusCode, reason);
    }

    @OnWebSocketConnect
    @Override
    public void onWebSocketConnect(Session session) {
        log.info("New websocket connection from {}", session);
    }

    @Override
    public void onWebSocketBinary(byte payload[], int offset, int len) {
        log.info("New binary message (not handled): {} {} {}", payload, offset, len);
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        log.error("Error on websocket communication", throwable);
    }

}
