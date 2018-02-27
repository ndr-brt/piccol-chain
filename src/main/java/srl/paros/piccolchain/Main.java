package srl.paros.piccolchain;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.node.GuestList;
import srl.paros.piccolchain.node.Node;

import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    private static final String BASE_URL = "http://localhost:4567/";

    public static void main(String[] args) throws Exception {
        GuestList guestList = new GuestList(UUID.randomUUID());
        guestList.init();

        log.info("Guest list created. Id {}", guestList.id());

        UUID nodeId = UUID.fromString(Unirest.post(BASE_URL + guestList.id() + "/").asString().getBody());
        Node firstNode = new Node(nodeId);
        firstNode.init();

        log.info("Registered new node {}", firstNode.id());
        log.info("All nodes {}", allNodes(guestList));
        log.info("Blockchain at start {}", blockChainOf(firstNode));

        for (int i = 0; i < random(5); i++) {
            newTransaction(firstNode);
        }

        log.info("Mine a block {}", mine(firstNode));

        for (int i = 0; i < random(5); i++) {
            newTransaction(firstNode);
        }

        log.info("Mine a block {}", mine(firstNode));

        log.info("Blockchain {}", blockChainOf(firstNode));

    }

    private static String mine(Node node) throws UnirestException {
        return Unirest.get(BASE_URL + node.id() + "/mine").asString().getBody();
    }

    private static String newTransaction(Node node) {
        try {
            return Unirest.post(BASE_URL + node.id() + "/")
                    .body(Json.toJson(randomTransaction()))
                    .asString()
                    .getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private static String blockChainOf(Node node) throws UnirestException {
        return Unirest.get(BASE_URL + node.id() + "/blocks").asString().getBody();
    }

    private static String allNodes(GuestList guestList) throws UnirestException {
        return Unirest.get(BASE_URL + guestList.id() + "/").asString().getBody();
    }

    private static Transaction randomTransaction() {
        return new Transaction(UUID.randomUUID().toString(), UUID.randomUUID().toString(), random(100));
    }

    private static int random(int bound) {
        return new Random().nextInt(bound) + 1;
    }
}
