package com.github.smvfal.placementresolver.resolver;

import com.github.smvfal.placementresolver.entity.Node;

import java.util.ArrayList;
import java.util.List;

public class NetworkAwareUtils {

    public static List<Node> findGatewayNodes(List<Node> nodes) {

        // assume gateway is on a controller node
        List<Node> gatewayNodes = new ArrayList<>();
        gatewayNodes.add(new Node("worker-usc-2"));

        // detect nodes running API gateway pods
        //System.err.println("Detecting nodes running gateway pods");
        //gatewayNodes.clear();
        //for (Node node: nodes) {
        //    if (node.hostDeploymentPod("gateway"))
        //        gatewayNodes.add(node);
        //}

        return gatewayNodes;
    }

}
