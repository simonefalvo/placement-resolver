package com.github.smvfal.placementresolver.resolver;

import com.github.smvfal.placementresolver.entity.*;
import com.github.smvfal.placementresolver.network.DelayCalculator;
import com.github.smvfal.placementresolver.network.DelayLoader;
import com.github.smvfal.placementresolver.resolver.exception.GatewayNotFoundException;
import com.github.smvfal.placementresolver.resolver.exception.InsufficientResourcesException;
import com.github.smvfal.placementresolver.resolver.exception.PlacementException;

import java.util.ArrayList;
import java.util.List;

public class NetworkAwareHeuristicResolver implements Resolver{

    private final PlacementPolicyType mode;
    private final DelayCalculator delayCalculator;
    private final double maxDelay;

    public NetworkAwareHeuristicResolver(PlacementPolicyType mode, DelayLoader delayLoader, double maxDelay) {
        this.mode = mode;
        this.delayCalculator = new DelayCalculator(delayLoader);
        this.maxDelay = maxDelay;
    }

    private class Bound {

        public int instances;
        public double delay;

        public Bound() {
            switch (mode) {
                case MINIMIZE:
                    instances = Integer.MAX_VALUE;
                    delay = Double.MAX_VALUE;
                    break;
                case SPREAD:
                    instances = 0;
                    delay = 0;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + mode);
            }
        }

        public boolean toUpdate(int newInstances, double newDelay) {
            boolean toUpdate = false;

            // in case of the same number of instances, update if the delay is lower
            if (newInstances == instances && newDelay < delay)
                return true;

            switch (mode) {
                case MINIMIZE:
                    if (newInstances < instances)
                        toUpdate = true;
                    break;
                case SPREAD:
                    if (newInstances > instances)
                        toUpdate = true;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + mode);
            }

            return toUpdate;
        }
    }

    @Override
    public Placement solve(Cluster cluster) throws PlacementException {
        System.err.println("\nSolving placement with Network Aware Heuristic");

        List<Pod> pods = cluster.getPods();
        List<Node> nodes = cluster.getNodes();

        List<Mapping> mappingList = new ArrayList<>();
        List<Node> closeNodes = new ArrayList<>();
        List<Node> farNodes = new ArrayList<>();

        List<Node> gatewayNodes = NetworkAwareUtils.findGatewayNodes(nodes);
        if (gatewayNodes.size() == 0)
            throw new GatewayNotFoundException("Gateway not found in the available nodes");
        System.err.printf("Gateway nodes: %s\n", gatewayNodes);

        for (Pod pod: pods) {
            System.err.printf("Searching for a suitable node for pod %s\n", pod);

            // select nodes near to the API gateway
            System.err.println("Selecting closest nodes to gateway nodes");
            closeNodes.clear();
            farNodes.clear();
            for (Node node: nodes) {
                if (node.canHost(pod)) {
                    if (delayCalculator.isNodeCloseToOne(node, gatewayNodes, maxDelay))
                        closeNodes.add(node);
                    else
                        farNodes.add(node);
                }
            }
            System.err.printf("Close nodes: %s\n", closeNodes);
            System.err.printf("Far nodes: %s\n", farNodes);

            // select placement node
            System.err.println("Checking nodes near the API gateway...");
            Node targetNode = selectNode(pod, closeNodes, gatewayNodes);
            if (targetNode == null) {
                System.err.println("Pod can't be placed near to the Gateway, checking far nodes...");
                targetNode = selectNode(pod, farNodes, gatewayNodes);
            }

            if (targetNode == null)
                throw new InsufficientResourcesException("Insufficient resources");

            if (targetNode.assignPod(pod)) {
                mappingList.add(new Mapping(
                        pod.getName(),
                        pod.getNamespace(),
                        pod.getUID(),
                        targetNode.getName())
                );
                System.err.printf("Found a suitable node. Updated node: %s\n", targetNode);
            }
            else
                throw new PlacementException("Unexpected error: pod can't be assigned to target node");
        }

        return new Placement(mappingList);
    }

    private Node selectNode(Pod pod, List<Node> nodes, List<Node> gatewayNodes) {
        Bound currentBound = new Bound();
        Node targetNode = null;
        for (Node node: nodes) {
            int nodeMaxInstances = node.maxInstances(pod);
            double delayFromGateway = delayCalculator.minDelay(node, gatewayNodes);
            String nodeName = node.getName();
            System.err.printf("Node: %s, max pod instances: %d\n", nodeName, nodeMaxInstances);
            System.err.printf("Node: %s, delay from gateway: %f\n", nodeName, delayFromGateway);
            if (currentBound.toUpdate(nodeMaxInstances, delayFromGateway)) {
                currentBound.instances = nodeMaxInstances;
                currentBound.delay = delayFromGateway;
                targetNode = node;
                System.err.printf("Updated target node: %s\n", nodeName);
            }
        }
        return targetNode;
    }

}
