package com.github.smvfal.placementresolver.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

public class FileDelayLoader implements DelayLoader {

    @Override
    public HashMap<String, Double> load() {

        HashMap<String, Double> delayMap = new HashMap<>();

        byte[] jsonData = new byte[0];
        try {
            jsonData = Files.readAllBytes(Paths.get("data/delays.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert rootNode != null;
        JsonNode nodesNode = rootNode.path("nodes");
        Iterator<JsonNode> nodes = nodesNode.elements();
        while (nodes.hasNext()) {
            JsonNode node = nodes.next();
            String nodeName = node.path("name").asText();
            JsonNode delaysNode = node.path("delays");
            Iterator<JsonNode> delays = delaysNode.elements();
            while (delays.hasNext()) {
                JsonNode delay = delays.next();
                String delayNode = delay.path("node").asText();
                double value = delay.path("value").asDouble();
                delayMap.put(nodeName + "_" + delayNode, value);
            }
        }

        return delayMap;
    }
}
