package srl.paros.piccolchain;

import java.util.ArrayList;
import java.util.List;

public interface Transactions {

    enum Singleton {
        INSTANCE;

        private final Transactions transactions;

        Singleton() {
            transactions = new InMemory();
        }
    }

    static Transactions transactions() {
        return Singleton.INSTANCE.transactions;
    }

    void add(Transaction transaction);

    class InMemory implements Transactions {

        private final List<Transaction> list = new ArrayList<>();

        @Override
        public void add(Transaction transaction) {
            list.add(transaction);
        }
    }
}
