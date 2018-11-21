package srl.paros.piccolchain.node.domain;

import srl.paros.piccolchain.Json;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;

public enum Nodes {
    InMemory;

    private Map<String, Boolean> nodes = new HashMap<>();

    public String asJson() {
        return Json.toJson(nodes.keySet());
    }

    public void add(String node) {
        nodes.put(node, TRUE);
    }

    public Stream<String> stream() {
        return nodes.keySet().stream();
    }

    public Set<String> names() {
        return nodes.keySet();
    }
}
