package srl.paros.piccolchain.node;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.servlet.SparkApplication;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import srl.paros.piccolchain.*;
import srl.paros.piccolchain.domain.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.function.Supplier;

import static spark.Spark.*;
import static srl.paros.piccolchain.domain.Transactions.transactions;

public class Node implements SparkApplication {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Transactions transactions = transactions();
    private final Blockchain blockchain = Blockchain.blockchain();
    private final String name;
    private final WebSocketServer webSocketServer;
    private final WebSocketClient webSocketClient;

    public Node() {
        this.name = hostname.get();
        webSocketServer = new WebSocketServer(transactions, blockchain);
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

        get("/", (req, res) -> new ModelAndView(
                Map.of("name", name,
                        "transactions", transactions.get(),
                        "blocks", blockchain.blocks()), "node"),
                new ThymeleafTemplateEngine());

        post("/transactions", "application/json", (req, res) -> {
            Transaction transaction = Json.fromJson(req.body(), Transaction.class);
            transactions.append(transaction);
            log.info("New transaction: " + req.body());
            broadcast("transaction", req.body());
            return "Transaction created";
        });

        post("/transactions", "application/x-www-form-urlencoded", (req, res) -> {
            Transaction transaction = new Transaction(
                    req.queryParams("from"),
                    req.queryParams("to"),
                    Long.valueOf(req.queryParams("amount"))
            );
            transactions.append(transaction);
            String json = Json.toJson(transaction);
            log.info("New transaction: " + json);
            broadcast("transaction", json);
            res.redirect("/");
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
            broadcast("block", Json.toJson(newBlock));

            res.redirect("/");
            return blockchain.last();
        }, Json::toJson);

        get("/blocks", (req, res) -> blockchain.blocks(), Json::toJson);

        post("/addPeer", (req, res) -> connectToPeer(req.body()));

        exception(Exception.class, (exception, request, response) -> log.error("Exception", exception));

        joinTheNet();

        Timer timer = new Timer();
        timer.schedule(new RefreshPeerConnections(this.name()), 5000, 30000);

    }

    private final Supplier<String> hostname = () -> {
        try {
            String name = InetAddress.getLocalHost().getHostName();
            log.info("Node's name {}", name);
            return name;
        } catch (UnknownHostException e) {
            log.error("Error getting hostname, give an uuid", e);
            return UUID.randomUUID().toString();
        }
    };

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
            Unirest.post("http://guestlist:4567/nodes")
                    .body(Json.toJson(Map.of("name", name)))
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
}
