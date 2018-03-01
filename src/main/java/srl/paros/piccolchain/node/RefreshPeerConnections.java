package srl.paros.piccolchain.node;

import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;

import java.util.List;
import java.util.TimerTask;

public class RefreshPeerConnections extends TimerTask {

    private Logger log = LoggerFactory.getLogger(getClass());

    private String name;

    public RefreshPeerConnections(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        log.info("Refresh peer connections");
        try {
            log.info("Ask peers to guestlist");
            String json = Unirest.get("http://guestlist:4567/")
                    .asString()
                    .getBody();

            log.info("Guestlist response: {}", json);

            Json.fromJson(json, List.class).stream()
                    .filter(peer -> !peer.equals(name))
                    .forEach(peer -> {
                        try {
                            log.info("Hey {}, i'm here!", peer);
                            Unirest.post("http://" + peer + ":4567/addPeer")
                                    .body(name)
                                    .asString();
                        } catch (Exception e) {
                            log.error("Error telling the peer that I exist", e);
                        }
                    });

        } catch (Exception e) {
            log.error("Error refreshing peer connections", e);
        }
    }

}
