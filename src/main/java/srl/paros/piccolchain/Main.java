package srl.paros.piccolchain;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import srl.paros.piccolchain.node.GuestList;
import srl.paros.piccolchain.node.Node;

import java.io.InputStream;
import java.util.UUID;

public class Main {

    private static final String BASE_URL = "http://localhost:4567/";

    public static void main(String[] args) throws Exception {
        UUID guestListId = UUID.randomUUID();
        new GuestList(guestListId).init();

        System.out.printf("Guest list created. Id %s\n\n", guestListId);

        UUID nodeId = UUID.fromString(Unirest.post(BASE_URL + guestListId + "/").asString().getBody());
        Node firstNode = new Node(nodeId);
        firstNode.init();

        System.out.printf("Registered new node %s\n\n", firstNode.id());

        System.out.printf("All nodes %s\n\n", Unirest.get(BASE_URL + guestListId + "/").asString().getBody());

        System.out.printf("Blockchain at start\n%s\n\n",
                Unirest.get(BASE_URL + firstNode.id() + "/blocks").asString().getBody());

        System.out.printf("Add a transaction\n%s\n\n",
                Unirest.post(BASE_URL + firstNode.id() + "/").body(Json.toJson(new Transaction("120843275", "38543134", 3))).asString().getBody());

        System.out.printf("Mine a block\n%s\n\n",
                Unirest.get(BASE_URL + firstNode.id() + "/mine").asString().getBody());

        System.out.printf("Blockchain\n%s\n\n",
                Unirest.get(BASE_URL + firstNode.id() + "/blocks").asString().getBody());


    }
}
