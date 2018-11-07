package srl.paros.piccolchain;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.node.GuestList;
import srl.paros.piccolchain.node.Node;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        if (args.length < 1) {
            log.error("Node type parameter expected: 'guestlist' or 'node'");
        }
        else {
            switch (args[0]) {
                case "guestlist":
                    Vertx.vertx().deployVerticle(new GuestList());
                    break;
                case "node":
                    new Node().init();
                    break;
                default:
                    log.error("node type {} not existent", args[0]);
            }
        }

    }

}
