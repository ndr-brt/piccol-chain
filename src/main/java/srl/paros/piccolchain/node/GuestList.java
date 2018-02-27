package srl.paros.piccolchain.node;

import spark.servlet.SparkApplication;
import srl.paros.piccolchain.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;

public class GuestList implements SparkApplication {

    private UUID id;
    private List<UUID> nodes = new ArrayList<>();

    public GuestList(UUID id) {
        this.id = id;
    }

    @Override
    public void init() {
        post("/" + id + "/", (req, res) -> {
            System.out.println("A node request to join");
            UUID node = UUID.randomUUID();
            nodes.add(node);
            return node.toString();
        });

        get("/" + id + "/", (req, res) -> nodes, Json::toJson);

        exception(Exception.class, (exception, request, response) -> System.err.println(exception));
    }

    public UUID id() {
        return id;
    }
}
