package srl.paros.piccolchain.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.servlet.SparkApplication;
import srl.paros.piccolchain.Json;

import java.util.*;

import static java.util.stream.Collectors.toSet;
import static spark.Spark.*;

public class GuestList implements SparkApplication {

    private Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> nodes = new HashSet<>();

    @Override
    public void init() {

        post("/", (req, res) -> {
            String node = req.body();
            log.info("A node request to join {}", node);
            nodes.add(node);
            return Json.toJson(nodes.stream().filter(it -> !node.equals(it)).collect(toSet()));
        });

        get("/", (req, res) -> nodes, Json::toJson);

        exception(Exception.class, (exception, request, response) -> log.error("Exception", exception));
    }
}
