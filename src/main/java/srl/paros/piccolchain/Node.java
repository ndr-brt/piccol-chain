package srl.paros.piccolchain;

import java.util.UUID;
import java.util.stream.IntStream;

public class Node {

    public static void main(String[] args) {
        UUID nodeId = UUID.randomUUID();
        new WebServer(nodeId).init();
    }

}
