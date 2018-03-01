package srl.paros.piccolchain.domain;

import java.util.ArrayList;
import java.util.List;

public interface Transactions {

    void empty();

    enum Singleton {
        INSTANCE;

        private final Transactions transactions;

        Singleton() {
            transactions = new InMemory();
        }

    }

    List<Transaction> get();

    static Transactions transactions() {
        return Singleton.INSTANCE.transactions;
    }

    void append(Transaction transaction);

    class InMemory implements Transactions {

        private final List<Transaction> list = new ArrayList<>();

        @Override
        public void empty() {
            list.clear();
        }

        @Override
        public List<Transaction> get() {
            return new ArrayList<>(list);
        }

        @Override
        public void append(Transaction transaction) {
            list.add(transaction);
        }
    }
}
