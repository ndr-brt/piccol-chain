package srl.paros.piccolchain;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.guestlist.GuestList;
import srl.paros.piccolchain.node.Node;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length < 1) {
            log.error("Node type parameter expected: 'guestlist' or 'node'");
        } else {
            Vertx.vertx().deployVerticle(node(args[0]));
        }
    }

    private static Verticle node(String type) {
        switch (type) {
            case "guestlist": return new GuestList();
            case "node": return new Node();
            default: throw new RuntimeException("node type " + type + " not existent");
        }
    }

}
