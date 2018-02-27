package srl.paros.piccolchain.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.servlet.SparkApplication;
import srl.paros.piccolchain.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;

public class GuestList implements SparkApplication {

    private Logger log = LoggerFactory.getLogger(getClass());

    private UUID id;
    private List<UUID> nodes = new ArrayList<>();

    public GuestList(UUID id) {
        this.id = id;
    }

    @Override
    public void init() {
        post("/" + id + "/", (req, res) -> {
            UUID node = UUID.randomUUID();
            log.info("A node request to join {}", node);
            nodes.add(node);
            return node.toString();
        });

        get("/" + id + "/", (req, res) -> nodes, Json::toJson);

        exception(Exception.class, (exception, request, response) -> log.error("Exception", exception));
    }

    public UUID id() {
        return id;
    }
}
