package srl.paros.piccolchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.function.Supplier;

public enum Hostname implements Supplier<String> {

    HOSTNAME {
        private final Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public String get() {
            try {
                String name = InetAddress.getLocalHost().getHostName();
                log.info("Node's name {}", name);
                return name;
            } catch (UnknownHostException e) {
                log.error("Error getting hostname, give an uuid", e);
                return UUID.randomUUID().toString();
            }
        }
    };


}
