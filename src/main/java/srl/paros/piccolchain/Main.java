package srl.paros.piccolchain;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import srl.paros.piccolchain.node.Node;

import java.util.UUID;

public class Main {

    public static final String BASE_URL = "http://localhost:4567/";

    public static void main(String[] args) throws UnirestException {
        UUID nodeId = UUID.randomUUID();
        new Node(nodeId).init();

        System.out.printf("Blockchain at start\n%s\n",
                Unirest.get(BASE_URL + nodeId + "/blocks").asString().getBody());

        System.out.printf("Add a transaction\n%s\n",
                Unirest.post(BASE_URL + nodeId + "/").body(Json.toJson(new Transaction("120843275", "38543134", 3))).asString().getBody());

        System.out.printf("Mine a block\n%s\n",
                Unirest.get(BASE_URL + nodeId + "/mine").asString().getBody());

        System.out.printf("Blockchain\n%s\n",
                Unirest.get(BASE_URL + nodeId + "/blocks").asString().getBody());

    }
}
