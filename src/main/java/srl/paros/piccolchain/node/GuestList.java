package srl.paros.piccolchain.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.servlet.SparkApplication;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import srl.paros.piccolchain.Json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static spark.Spark.*;

public class GuestList implements SparkApplication {

    private Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> nodes = new HashSet<>();

    @Override
    public void init() {

        get("/", (req, res) -> new ModelAndView(Map.of("nodes", nodes), "guestlist"), new ThymeleafTemplateEngine());

        post("/nodes", (req, res) -> {
            String node = req.body();
            log.info("A node request to join {}", node);
            nodes.add(node);
            return Json.toJson(nodes.stream().filter(it -> !node.equals(it)).collect(toSet()));
        });

        get("/nodes", (req, res) -> nodes, Json::toJson);

        exception(Exception.class, (exception, request, response) -> log.error("Exception", exception));
    }
}
