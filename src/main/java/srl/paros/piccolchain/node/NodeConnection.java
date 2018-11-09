package srl.paros.piccolchain.node;

import com.google.gson.reflect.TypeToken;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import srl.paros.piccolchain.Json;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeConnection extends AbstractVerticle {

    private static final Type SET_STRING = new TypeToken<Set<String>>() {}.getType();
    private boolean connected = false;
    private HttpClient httpClient;
    private String name;
    private Set<String> peers;

    public NodeConnection(String name) {
        this.name = name;
    }

    @Override
    public void start() {
        this.httpClient = vertx.createHttpClient();

        vertx.setPeriodic(5000, it -> {
            if (!connected) {
                httpClient.postAbs("http://guestlist:4567/nodes")
                        .handler(res -> connected = res.statusCode() == 201)
                        .end(Json.toJson(Map.of("name", name)));
            }
        });

        vertx.setPeriodic(2000, it -> {
            if (connected) {
                httpClient.getAbs("http://guestlist:4567/nodes")
                        .handler(res -> res.bodyHandler(buffer -> {
                            Set<String> actualPeers = Json.fromJson(buffer.toString(), SET_STRING);
                            peers = actualPeers.stream()
                                    .filter(node -> !node.equals(name))
                                    .collect(Collectors.toSet());
                        }))
                        .end();
            }
        });
    }
}
