package srl.paros.piccolchain;

public class Transaction {
    private final String from;
    private final String to;
    private final long amount;

    public Transaction(String from, String to, long amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", amount=" + amount +
                '}';
    }
}
