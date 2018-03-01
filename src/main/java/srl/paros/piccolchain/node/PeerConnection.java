package srl.paros.piccolchain.node;

import com.mashape.unirest.http.Unirest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;
import srl.paros.piccolchain.websocket.WebSocketServer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class PeerConnection extends TimerTask {

    private Logger log = LoggerFactory.getLogger(getClass());

    List<String> peers = new ArrayList<>();
    private Node node;

    public PeerConnection(Node node) {
        this.node = node;
    }

    @Override
    public void run() {

        try {
            log.info("Ask peers to guestlist");
            String json = Unirest.get("http://guestlist:4567/")
                    .asString()
                    .getBody();

            log.info("Guestlist response: {}", json);

            Json.fromJson(json, List.class).stream()
                    .filter(peer -> !peer.equals(node.name()))
                    .forEach(peer -> {
                        try {
                            log.info("Hey {}, i'm here!", peer);
                            Unirest.post("http://" + peer + ":4567/addPeer")
                                    .body(node.name())
                                    .asString();
                        } catch (Exception e) {
                            log.error("Error telling the peer that I exist", e);
                        }
                    });

        } catch (Exception e) {
            log.error("Error obtain peers list", e);
        }
    }

    public static void main(String[] args) throws Exception {
        WebSocketClient client = new WebSocketClient();
        client.start();
        WebSocketServer socket = new WebSocketServer();
        client.connect(socket, URI.create("ws://localhost:10002/socket"));

        Thread.sleep(4000);
        System.out.println(client);
    }

}
