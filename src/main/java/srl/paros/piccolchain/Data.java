package srl.paros.piccolchain;

import java.util.Collection;

public class Data {

    private final int proofOfWork;
    private final Collection<Transaction> transactions;

    public Data(int proofOfWork, Collection<Transaction> transactions) {
        this.proofOfWork = proofOfWork;
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "Data{" +
                "transactions=" + transactions +
                '}';
    }

    public int proofOfWork() {
        return proofOfWork;
    }
}
