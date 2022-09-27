package com.github.smvfal.placementresolver.resolver;

import com.github.smvfal.placementresolver.entity.*;
import com.github.smvfal.placementresolver.resolver.exception.PodAlreadyInNodeException;
import com.github.smvfal.placementresolver.resolver.exception.InsufficientResourcesException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreedyFirstFitResolver implements Resolver {

    private final Comparator<Node> nodeComparator = Comparator.comparingDouble(
            a -> a.getMemAvail() * a.getCpuAvail());


    @Override
    public Placement solve(Cluster cluster) throws InsufficientResourcesException, PodAlreadyInNodeException {

        System.err.println("\nSolving placement with Greedy First Fit policy");

        List<Mapping> mappingList = new ArrayList<>();
        List<Node> nodes = cluster.getNodes();
        List<Pod> pods = cluster.getPods();

        int assignedPods = 0;
        for (Pod pod : pods) {
            System.err.printf("Searching for a suitable node for pod %s \n", pod);

            sortNodes(nodes);
            System.err.printf("Sorted nodes: %s\n", nodes);

            for (Node node : nodes) {
                System.err.printf("Checking %s...\n", node);

                if (node.assignPod(pod)) {
                    mappingList.add(new Mapping(
                            pod.getName(),
                            pod.getNamespace(),
                            pod.getUID(),
                            node.getName())
                    );
                    assignedPods++;
                    System.err.printf("suitable!\nUpdated %s\n" , node);
                    break;
                }
                System.err.println("not suitable.");
            }
        }

        if (assignedPods != pods.size())
            throw new InsufficientResourcesException("insufficient resources");

        return new Placement(mappingList);
    }


    // sort nodes by area = memory * cpu
    void sortNodes(List<Node> nodes) {
        nodes.sort(nodeComparator);
    }

}
