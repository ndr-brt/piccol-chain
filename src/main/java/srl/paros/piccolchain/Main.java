package srl.paros.piccolchain;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.node.GuestList;
import srl.paros.piccolchain.node.Node;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    private static final String BASE_URL = "http://localhost:4567/";

    public static void main(String[] args) throws Exception {
        GuestList guestList = new GuestList(UUID.randomUUID());
        guestList.init();

        log.info("Guest list created. Id {}", guestList.id());

        int nodes = 2;

        ExecutorService pool = Executors.newFixedThreadPool(nodes);
        for (int i = 0; i < nodes; i++) {
            pool.submit(() -> {
                Node node = new Node(createNode(guestList));
                node.init();

                log.info("Registered new node {}", node.id());

                while (true) {

                    for (int j = 0; j < random(5); j++) {
                        newTransaction(node);
                    }

                    log.info("Mine a block {}", mine(node));

                    Thread.sleep(random(1000));

                    log.info("Blockchain {}", blockChainOf(node));
                }

            });
        }

        Thread.sleep(2000);

        log.info("All nodes {}", allNodes(guestList));
    }

    private static UUID createNode(GuestList guestList) {
        try {
            return UUID.fromString(Unirest.post(BASE_URL + guestList.id() + "/")
                    .asString()
                    .getBody());
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private static String mine(Node node) {
        try {
            return Unirest.get(BASE_URL + node.id() + "/mine").asString().getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
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

    private static String blockChainOf(Node node) {
        try {
            return Unirest.get(BASE_URL + node.id() + "/blocks")
                    .asString()
                    .getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private static String allNodes(GuestList guestList) {
        try {
            return Unirest.get(BASE_URL + guestList.id() + "/")
                    .asString()
                    .getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private static Transaction randomTransaction() {
        return new Transaction(UUID.randomUUID().toString(), UUID.randomUUID().toString(), random(100));
    }

    private static int random(int bound) {
        return new Random().nextInt(bound) + 1;
    }
}
