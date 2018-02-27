package srl.paros.piccolchain;

import java.util.stream.IntStream;

public class Node {

    public static void main(String[] args) {

        new WebServer().init();

        Blockchain blockchain = new Blockchain();

        IntStream.range(1, 4).forEachOrdered(i -> {
            blockchain.addBlock();
            Block added = blockchain.last();
            System.out.println("block " + added.index + " added");
            System.out.println("Hash: " + added.hash);
        });

        System.out.println(blockchain);
    }

}
