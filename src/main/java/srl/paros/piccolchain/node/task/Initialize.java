package srl.paros.piccolchain.node.task;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import srl.paros.piccolchain.Json;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Initialize implements Handler<Long> {
    private final String name;
    private final AtomicBoolean connected;
    private final HttpClient httpClient;

    public Initialize(String name, AtomicBoolean connected, HttpClient httpClient) {
        this.name = name;
        this.connected = connected;
        this.httpClient = httpClient;
    }

    @Override
    public void handle(Long period) {
        if (!connected.get()) {
            httpClient.postAbs("http://guestlist:4567/nodes")
                    .handler(res -> connected.set(res.statusCode() == 201))
                    .end(Json.toJson(Map.of("name", name)));
        }
    }
}
