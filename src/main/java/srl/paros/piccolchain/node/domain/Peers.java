package srl.paros.piccolchain.node.domain;

import io.netty.util.internal.ConcurrentSet;

import java.util.Set;
import java.util.function.Consumer;

public interface Peers {
    static Peers peers() {
        return Singleton.INSTANCE.peers;
    }

    void reset(Set<String> newValues);

    enum Singleton {
        INSTANCE;
        private final Peers peers = new InMemory();
    }

    void forEach(Consumer<String> action);

    class InMemory implements Peers {

        private final ConcurrentSet<String> peers = new ConcurrentSet<>();

        @Override
        public void reset(Set<String> newValues) {
            peers.addAll(newValues);
            peers.retainAll(newValues);
        }

        @Override
        public void forEach(Consumer<String> action) {
            peers.forEach(action);
        }

        @Override
        public String toString() {
            return peers.toString();
        }
    }
}
