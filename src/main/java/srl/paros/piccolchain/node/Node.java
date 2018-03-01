package srl.paros.piccolchain.node;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.servlet.SparkApplication;
import srl.paros.piccolchain.*;
import srl.paros.piccolchain.websocket.WebSocketServer;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Timer;

import static spark.Spark.*;
import static srl.paros.piccolchain.Transactions.transactions;

public class Node implements SparkApplication {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Transactions transactions = transactions();
    private final Blockchain blockchain = new Blockchain();
    private String name;
    private final WebSocketServer webSocketServer;
    private final WebSocketClient webSocketClient;

    public Node(String name) {
        this.name = name;
        webSocketServer = new WebSocketServer();
        webSocketClient = new WebSocketClient();
        try {
            webSocketClient.start();
        } catch (Exception e) {
            log.error("Error initializing websocket client", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {

        webSocket("/socket", webSocketServer);

        post("/", (req, res) -> {
            Transaction transaction = Json.fromJson(req.body(), Transaction.class);
            transactions.append(transaction);
            log.info("New transaction: " + req.body());
            broadcast("transaction", req.body());
            return "Transaction created";
        });

        get("/mine", (req, res) -> {
            Block lastBlock = blockchain.last();
            int lastProofOfWork = lastBlock.data().proofOfWork();
            Integer proof = ProofOfWork.proofOfWork.apply(lastProofOfWork);

            transactions.append(new Transaction("network", name, 1));

            Block newBlock = new Block(
                    lastBlock.index() + 1,
                    Instant.now().toEpochMilli(),
                    new Data(proof, transactions.get()),
                    lastBlock.hash()
            );

            blockchain.append(newBlock);

            transactions.empty();

            return blockchain.last();
        }, Json::toJson);

        get("/blocks", (req, res) -> blockchain.blocks(), Json::toJson);

        post("/addPeer", (req, res) -> connectToPeer(req.body()));

        exception(Exception.class, (exception, request, response) -> log.error("Exception", exception));

        joinTheNet();

        Timer timer = new Timer();
        timer.schedule(new PeerConnection(this), 5000);

    }

    private void broadcast(String type, String message) {
        webSocketClient.getOpenSessions().forEach(session -> {
            try {
                session.getRemote().sendString(type + ":" + message);
            } catch (IOException e) {
                log.error("Error sending message to peers", e);
            }
        });
    }

    private void joinTheNet() {
        try {
            Unirest.post("http://guestlist:4567/")
                    .body(name)
                    .asString();
        } catch (UnirestException e) {
            log.error("Impossible to join the net, verify the guestlist address", e);
            throw new RuntimeException(e);
        }
    }

    private String connectToPeer(String peer) {
        try {
            webSocketClient.connect(webSocketServer, URI.create("ws://" + peer + ":4567/socket"));
        } catch (Exception e) {
            log.error("Error connecting with peer", e);
            throw new RuntimeException(e);
        }
        return "Peer added";
    }

    public String name() {
        return name;
    }

    public WebSocketServer socket() {
        return webSocketServer;
    }
}
