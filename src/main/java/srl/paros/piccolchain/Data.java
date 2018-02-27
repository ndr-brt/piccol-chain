package srl.paros.piccolchain;

import java.util.ArrayList;
import java.util.Collection;

public class Data {

    private final Collection<Transaction> transactions = new ArrayList<>();

    @Override
    public String toString() {
        return "Data{" +
                "transactions=" + transactions +
                '}';
    }
}
