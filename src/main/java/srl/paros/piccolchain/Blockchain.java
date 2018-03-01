package srl.paros.piccolchain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface Blockchain {

    static Blockchain blockchain() {
        return Singleton.INSTANCE.blockchain;
    }

    enum Singleton {
        INSTANCE;

        private final Blockchain blockchain = new InMemory();
    }

    void append(Block newBlock);

    Block last();

    List<Block> blocks();


    class InMemory implements Blockchain {

        final List<Block> blocks;

        InMemory() {
            blocks = new ArrayList<>();
            blocks.add(new Block(0, Instant.now().toEpochMilli(), new Data(1, Collections.emptyList()), "0"));
        }

        public void append(Block newBlock) {
            blocks.add(newBlock);
        }

        public Block last() {
            return blocks.get(blocks.size() - 1);
        }

        public List<Block> blocks() {
            return blocks;
        }

        @Override
        public String toString() {
            return "Blockchain{" +
                    "blocks=" + blocks +
                    '}';
        }
    }
}
