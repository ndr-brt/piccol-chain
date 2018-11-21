package srl.paros.piccolchain.guestlist;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srl.paros.piccolchain.Json;
import srl.paros.piccolchain.node.domain.Nodes;

import java.util.Map;
import java.util.Set;

import static io.vertx.ext.web.Router.router;
import static java.util.stream.Collectors.toSet;

public class GuestList extends AbstractVerticle {

    private Logger log = LoggerFactory.getLogger(getClass());
    private Nodes nodes = Nodes.InMemory;

    @Override
    public void start() {

        var router = router(vertx);

        ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();

        router.get("/").handler(context -> {
            context.put("nodes", nodes.names());
            engine.render(context, "templates/", "guestlist.html", res -> {
                if (res.succeeded()) {
                    context.response().end(res.result());
                } else {
                    context.fail(res.cause());
                }
            });
        });

        router.get("/nodes")
                .handler(context -> context.response().end(nodes.asJson()));

        router.post("/nodes")
                .handler(context -> context.request()
                        .bodyHandler(buffer -> {
                            String body = buffer.toString();
                            log.info("Node connected {}", body);
                            String node = (String) Json.fromJson(body, Map.class).get("name");
                            nodes.add(node);
                            Set<String> peers = nodes.stream()
                                    .filter(it -> !node.equals(it))
                                    .collect(toSet());
                            context.response().setStatusCode(201).end(
                                    Json.toJson(peers)
                            );
                        }));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(4567);
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new GuestList());
    }
}
