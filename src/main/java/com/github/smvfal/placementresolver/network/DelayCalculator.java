package com.github.smvfal.placementresolver.network;

import com.github.smvfal.placementresolver.entity.Node;

import java.util.HashMap;
import java.util.List;

public class DelayCalculator {

    private final DelayLoader delayLoader;

    public DelayCalculator(DelayLoader delayLoader) {
        this.delayLoader = delayLoader;
    }

    public double delay(Node a, Node b) {
        HashMap<String, Double> delays = delayLoader.load();
        String key = a.getName() + "_" + b.getName();
        return delays.get(key);
    }

    public boolean isNodeCloseToOne(Node node, List<Node> nodes, double delay) {
        for (Node listNode: nodes) {
            if (delay(node, listNode) <= delay)
                return true;
        }
        return false;
    }

    public boolean isNodeCloseToAll(Node node, List<Node> nodes, double delay) {
        for (Node listNode: nodes) {
            if (delay(node, listNode) > delay)
                return false;
        }
        return true;
    }

    public double minDelay(Node node, List<Node> nodes) {

        if (nodes.size() == 0)
            return 0;

        double minDelay = Double.MAX_VALUE;
        for (Node listNode: nodes) {
            double delay = delay(node, listNode);
            if (delay < minDelay)
                minDelay = delay;
        }
        return minDelay;
    }
}
