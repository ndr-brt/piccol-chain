package srl.paros.piccolchain;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class Block {

    final int index;
    final long timestamp;
    final Data data;
    final String previousHash;
    final String hash;

    public Block(int index, long timestamp, Data data, String previousHash) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.previousHash = previousHash;
        this.hash = hash();
    }

    private String hash() {
        try {
            String key = String.valueOf(index) + timestamp + data + previousHash;
            return sha256Hex(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Data data() {
        return data;
    }

    @Override
    public String toString() {
        return "Block{" +
                "index=" + index +
                ", timestamp=" + timestamp +
                ", data=" + data +
                ", previousHash='" + previousHash + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
