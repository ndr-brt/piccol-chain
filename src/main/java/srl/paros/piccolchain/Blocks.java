package srl.paros.piccolchain;

import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Blocks {

    Supplier<Block> genesis = () -> new Block(0, Instant.now().toEpochMilli(), "Genesis block", "0");

    Function<Block, Block> next = previous -> new Block(
            previous.index + 1,
            Instant.now().toEpochMilli(),
            "Hey! I'm block " + (previous.index + 1),
            previous.hash
    );

}
