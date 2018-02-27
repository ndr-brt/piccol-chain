package srl.paros.piccolchain;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class Block {

    final int index;
    final long timestamp;
    final Object data;
    final String previousHash;
    final String hash;

    public Block(int index, long timestamp, Object data, String previousHash) {
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

    @Override
    public String toString() {
        return Json.toJson(this);
    }
}
