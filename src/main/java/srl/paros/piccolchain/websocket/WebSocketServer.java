package srl.paros.piccolchain.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.*;

import java.util.ArrayList;
import java.util.List;


@WebSocket
public class WebSocketServer implements WebSocketListener {

    private Logger log = LoggerFactory.getLogger(getClass());
    private List<Session> sessions = new ArrayList<>();

    @OnWebSocketMessage
    @Override
    public void onWebSocketText(String message) {
        int separatorIndex = message.indexOf(":");
        String type = message.substring(0, separatorIndex);
        String content = message.substring(separatorIndex + 1);
        switch (type) {
            case "transaction":
                log.info("New transaction by peer, append");
                Transactions.transactions().append(Json.fromJson(content, Transaction.class));
                break;
            case "block":
                log.info("New block by peer, update chain");
                Blockchain.blockchain().append(Json.fromJson(content, Block.class));
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
        sessions.add(session);
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {

    }

    @Override
    public void onWebSocketError(Throwable throwable) {

    }

}
