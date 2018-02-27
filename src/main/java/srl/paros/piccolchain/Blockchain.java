package srl.paros.piccolchain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Blockchain {

    final List<Block> blocks;

    public Blockchain() {
        blocks = new ArrayList<>();
        blocks.add(new Block(0, Instant.now().toEpochMilli(), new Data(1, Collections.emptyList()), "0"));
    }

    void append(Block newBlock) {
        blocks.add(newBlock);
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
