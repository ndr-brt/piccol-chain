package srl.paros.piccolchain.node.task;

import com.google.gson.reflect.TypeToken;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class UpdatePeerList implements Handler<Long> {

    private static final Type SET_STRING = new TypeToken<Set<String>>() {}.getType();
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String name;
    private final AtomicBoolean connected;
    private final Set<String> peers;
    private final HttpClient httpClient;

    public UpdatePeerList(String name, AtomicBoolean connected, Set<String> peers, HttpClient httpClient) {
        this.name = name;
        this.connected = connected;
        this.peers = peers;
        this.httpClient = httpClient;
    }

    @Override
    public void handle(Long event) {
        if (connected.get()) {
            httpClient.getAbs("http://guestlist:4567/nodes")
                .handler(res -> res.bodyHandler(buffer -> {
                    Set<String> actualPeers = Json.fromJson(buffer.toString(), SET_STRING);
                    peers.clear();
                    peers.addAll(actualPeers.stream()
                            .filter(node -> !node.equals(name))
                            .collect(Collectors.toSet()));
                    log.info("Ottenuta la lista dei peers: {}", peers);
                }))
                .end();
        }
    }
}
