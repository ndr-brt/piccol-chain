package srl.paros.piccolchain.node.domain;

import java.util.function.Function;

public interface ProofOfWork extends Function<Integer, Integer> {

    ProofOfWork proofOfWork = last -> {
        int incrementor = last + 1;
        while (!(incrementor % 9 == 0 && incrementor % last == 0)) {
            incrementor += 1;
        }
        return incrementor;
    };
}
