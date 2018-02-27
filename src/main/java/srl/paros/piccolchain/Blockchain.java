package srl.paros.piccolchain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static srl.paros.piccolchain.Blocks.genesis;
import static srl.paros.piccolchain.Blocks.next;

public class Blockchain {

    final List<Block> blocks;

    public Blockchain() {
        blocks = new ArrayList<>();
        blocks.add(genesis.get());
    }

    private void addBlock() {
        blocks.add(next.apply(last()));
    }

    private Block last() {
        return blocks.get(blocks.size() - 1);
    }

    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain();

        IntStream.range(1, 50).forEachOrdered(i -> {
            blockchain.addBlock();
            Block added = blockchain.last();
            System.out.println("block " + added.index + " added");
            System.out.println("Hash: " + added.hash);
        });

        System.out.println(blockchain);
    }

    @Override
    public String toString() {
        return blocks.toString();
    }
}
