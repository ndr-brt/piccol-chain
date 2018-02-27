package srl.paros.piccolchain;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.UUID;
import java.util.stream.IntStream;

public class Node {

    public static final String URL = "http://localhost:4567";

    public static void main(String[] args) throws UnirestException {
        UUID nodeId = UUID.randomUUID();
        new WebServer(nodeId).init();

        System.out.printf("Blockchain at start\n%s\n",
                Unirest.get(URL + "/blocks").asString().getBody());

        System.out.printf("Add a transaction\n%s\n",
                Unirest.post(URL + "/").body(Json.toJson(new Transaction("120843275", "38543134", 3))).asString().getBody());

        System.out.printf("Mine a block\n%s\n",
                Unirest.get(URL + "/mine").asString().getBody());

        System.out.printf("Blockchain\n%s\n",
                Unirest.get(URL + "/blocks").asString().getBody());

    }

}
