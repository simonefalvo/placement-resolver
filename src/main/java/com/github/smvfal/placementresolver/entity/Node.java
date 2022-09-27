package com.github.smvfal.placementresolver.entity;

import com.github.smvfal.placementresolver.resolver.exception.PodAlreadyInNodeException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.floor;

@Getter
@Setter
@NoArgsConstructor
public class Node {

    private String name;
    private double memAvail;
    private double cpuAvail;
    private String zone;
    private String size;
    private List<String> podsInNode;

    public Node(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", memAvail=" + memAvail +
                ", cpuAvail=" + cpuAvail +
                //", zone='" + zone + '\'' +
                //", size='" + size + '\'' +
                ", podsInNode=" + podsInNode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(name, node.name) && Objects.equals(zone, node.zone);
    }

    /**
     * Assigns a pod to the node if there are enough memory and cpu resources.
     * @param pod the pod to be assigned
     * @return true if the node can be assigned to the node, false otherwise
     * @throws PodAlreadyInNodeException if the pod is already assigned to the node
     */
    public boolean assignPod(Pod pod) throws PodAlreadyInNodeException {

        String podName = pod.getName();
        double podRequiredCpu = pod.requiredCpu();
        double podRequiredMem = pod.requiredMem();

        if (!canHost(pod))
            return false;

        if (podsInNode == null)
            podsInNode = new ArrayList<>();
        if (podsInNode.contains(podName)) {
            String msg = String.format("pod %s already in node %s", pod.getName(), name);
            throw new PodAlreadyInNodeException(msg);
        }

        podsInNode.add(podName);
        cpuAvail -= podRequiredCpu;
        memAvail -= podRequiredMem;

        return true;
    }

    /**
     * Tests whether a pod can be hosted by the node according to the available resources
     * @param pod the pod to be evaluated
     * @return true if the node can host the pod, false otherwise
     */
    public boolean canHost(Pod pod) {
        double podRequiredCpu = pod.requiredCpu();
        double podRequiredMem = pod.requiredMem();
        return podRequiredCpu <= cpuAvail && podRequiredMem <= memAvail;
    }

    /**
     * Calculates how many pods instances fit in the node according to the available resources.
     * @param pod the pod to be evaluated
     * @return the maximum number of pod instances that fit in the node
     */
    public int maxInstances(Pod pod) {
        int cpuInstances = (int) floor(cpuAvail / pod.requiredCpu());
        int memInstances = (int) floor(memAvail / pod.requiredMem());
        return Integer.min(cpuInstances, memInstances);
    }

    /**
     * Tests whether the node hosts a pod of the given deployment according to the standard naming
     * conventions of kubernetes
     * @param deploymentName the name of the deployment
     * @return true if the node host the pod, false otherwise
     */
    public boolean hostDeploymentPod(String deploymentName) {

        if (podsInNode == null)
            return false;

        Pattern pattern = Pattern.compile("^" + deploymentName + "-");

        for (String podName: podsInNode) {
            Matcher matcher = pattern.matcher(podName);
            if (matcher.find())
                return true;
        }

        return false;
    }


    /**
     * Tests whether a node hosts at least a pod
     * @return true if the node hosts at least a pod, false otherwise
     */
    public boolean hostPods() {
        return podsInNode != null;
    }

}
