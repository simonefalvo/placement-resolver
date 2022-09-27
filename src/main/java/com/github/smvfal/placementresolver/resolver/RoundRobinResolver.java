package com.github.smvfal.placementresolver.resolver;

import com.github.smvfal.placementresolver.entity.*;
import com.github.smvfal.placementresolver.resolver.exception.PodAlreadyInNodeException;
import com.github.smvfal.placementresolver.resolver.exception.InsufficientResourcesException;

import java.util.*;

public class RoundRobinResolver implements Resolver{


    private static final RoundRobinResolver instance = new RoundRobinResolver();
    private static Node lastNode;
    private final List<Node> nodeList;

    private RoundRobinResolver() {
        nodeList = new ArrayList<>();
    }

    public static RoundRobinResolver getInstance() {
        return instance;
    }

    @Override
    public Placement solve(Cluster cluster) throws InsufficientResourcesException, PodAlreadyInNodeException {

        System.err.println("\nSolving placement with Round Robin policy");

        List<Mapping> mappingList = new ArrayList<>();
        List<Node> currentNodes = cluster.getNodes();
        List<Pod> pods = cluster.getPods();


        System.err.printf("Old node list: %s\n", nodeList.toString());
        updateNodeList(nodeList, currentNodes);
        System.err.printf("Available nodes: %s\n", nodeList.toString());

        // check nodes available resources
        int assignedPodCounter = 0;
        for (Pod pod : pods) {
            System.err.printf("Searching for a suitable node for %s\n", pod);
            int lastNodeIndex = nodeList.indexOf(lastNode);
            System.err.printf("Last assigned node index: %d\n", lastNodeIndex);
            Iterator<Node> iterator = nodeList.listIterator(lastNodeIndex + 1);
            while (true) {
                if (!iterator.hasNext())  // last node of the list, restart from the head
                    iterator = nodeList.listIterator(0);
                Node node = iterator.next();
                System.err.printf("Checking %s\n", node);
                System.err.printf("Node index: %d....", nodeList.indexOf(node));
                if (node.assignPod(pod)) {
                    // found a node with sufficient resources
                    mappingList.add(new Mapping(
                            pod.getName(),
                            pod.getNamespace(),
                            pod.getUID(),
                            node.getName())
                    );
                    lastNode = node;
                    assignedPodCounter++;
                    System.err.printf("suitable!\nUpdated %s\n" , node);
                    break;
                }
                System.err.println("not suitable.");
                if (nodeList.indexOf(node) == lastNodeIndex)
                    break;
            }
        }
        if (assignedPodCounter != pods.size())
            throw new InsufficientResourcesException("insufficient resources");

        return new Placement(mappingList);
    }


    // update node list
    void updateNodeList(List<Node> nodeList, List<Node> currentNodes) {

        System.err.printf("current nodes in the cluster:\n %s\n", currentNodes);
        System.err.println("Searching for no more available nodes");
        // remove no more available nodes
        List<Node> oldNodes = new ArrayList<>(nodeList);
        for (Node node : oldNodes) {
            System.err.printf("checking node %s (zone %s)...\n",
                    node.getName(), node.getZone());
            if (!currentNodes.contains(node)) {
                System.err.printf("%s (zone %s) no more available\n",
                        node.getName(), node.getZone());
                // if lastNode has to be removed, the previous node become lastNode
                if (node.equals(lastNode)) {
                    int i = nodeList.indexOf(node);
                    int listSize = nodeList.size();
                    if (listSize > 1) {
                        lastNode = nodeList.get((i - 1) % listSize);
                        System.err.printf("New lastNode is %s (zone %s)\n",
                                lastNode.getName(), lastNode.getZone());
                    }
                    else { // lastNode is the only node in the list
                        lastNode = null;
                        System.err.println("lastNode = null");
                    }
                }
                nodeList.remove(node);
                System.err.printf("Removed %s (zone %s)\n",
                        node.getName(), node.getZone());
            }
        }

        // add missing nodes and update old nodes
        System.err.println("Searching for new nodes and updating old ones");
        for (Node node : currentNodes) {
            int i = nodeList.indexOf(node);
            if (i == -1) { // nodeList does not contain the node
                if (lastNode != null)  // add the new node after lastNode
                    nodeList.add(nodeList.indexOf(lastNode) + 1, node);
                else
                    nodeList.add(node);
                System.err.printf("Added new node %s (zone %s)\n",
                        node.getName(), node.getZone());
            }
            else {
                nodeList.set(i, node);  // update node infos
                System.err.printf("Updated %s\n", node.toString());
            }
        }

        if (lastNode == null)
            lastNode = nodeList.get(0);
    }
}
