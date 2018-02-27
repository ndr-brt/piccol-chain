package srl.paros.piccolchain;

import java.util.ArrayList;
import java.util.List;

import static srl.paros.piccolchain.Blocks.genesis;
import static srl.paros.piccolchain.Blocks.next;

public class Blockchain {

    final List<Block> blocks;

    public Blockchain() {
        blocks = new ArrayList<>();
        blocks.add(genesis.get());
    }

    void addBlock() {
        blocks.add(next.apply(last()));
    }

    Block last() {
        return blocks.get(blocks.size() - 1);
    }

    @Override
    public String toString() {
        return "Blockchain{" +
                "blocks=" + blocks +
                '}';
    }
}
