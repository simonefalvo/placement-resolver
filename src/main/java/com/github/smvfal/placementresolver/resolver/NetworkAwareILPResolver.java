package com.github.smvfal.placementresolver.resolver;

import com.github.smvfal.placementresolver.entity.*;
import com.github.smvfal.placementresolver.network.DelayCalculator;
import com.github.smvfal.placementresolver.network.DelayLoader;
import com.github.smvfal.placementresolver.resolver.exception.GatewayNotFoundException;
import com.github.smvfal.placementresolver.resolver.exception.OptimizationException;
import com.github.smvfal.placementresolver.resolver.exception.PlacementException;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplexModeler;

import java.util.ArrayList;
import java.util.List;

public class NetworkAwareILPResolver implements Resolver{

    private final PlacementPolicyType mode;
    private final DelayLoader delayLoader;
    private final double maxDelay;
    private static final int GAMMA = 100;


    public NetworkAwareILPResolver(PlacementPolicyType mode, DelayLoader delayLoader, double maxDelay) {
        this.mode = mode;
        this.delayLoader = delayLoader;
        this.maxDelay = maxDelay;
    }

    @Override
    public Placement solve(Cluster cluster) throws PlacementException {
        System.err.println("\nSolving placement with Network Aware ILP");

        DelayCalculator delayCalculator = new DelayCalculator(delayLoader);

        List<Pod> pods = cluster.getPods();
        List<Node> nodes = cluster.getNodes();

        List<Mapping> mappingList = new ArrayList<>();

        // detect nodes running API gateway pods
        System.err.println("Detecting nodes running gateway pods");
        List<Node> gatewayNodes = NetworkAwareUtils.findGatewayNodes(nodes);
        if (gatewayNodes.size() == 0)
            throw new GatewayNotFoundException("Gateway not found in the available nodes");
        System.err.printf("Gateway nodes: %s\n", gatewayNodes);

        int P = pods.size();
        int N = nodes.size();

        assert GAMMA > P: "GAMMA constant is too small";

        // cplex model
        IloCplex cplex = null;

        try {

            // instantiate the optimization model
            cplex = new IloCplex();
            IloCplexModeler modeler = new IloCplexModeler();

            // set cplex output on standard error stream, set to null to turn off
            cplex.setOut(System.err);      // logging
            cplex.setWarning(System.err);  // warnings

            // initialize pod assignment variables
            IloNumVar[][] x  = new IloNumVar[P][N];
            for (int p = 0; p < P; p++)
                x[p] = modeler.boolVarArray(N);

            // initialize hosting nodes variables
            IloNumVar[] z = new IloNumVar[N];
            for (int n = 0; n < N; n++)
                z[n]= modeler.boolVar("z" + (n + 1));

            // define objective function
            IloLinearNumExpr obj = cplex.linearNumExpr();
            for (int n = 0; n < N; n++)
                obj.addTerm(1, z[n]);
            addObjective(cplex, obj, mode);

            // set capacity constraints
            for (int n = 0; n < N; n++) {
                Node node = nodes.get(n);
                IloLinearNumExpr cpuCapacityConstraint = cplex.linearNumExpr();
                IloLinearNumExpr memCapacityConstraint = cplex.linearNumExpr();

                for (int p = 0; p < P; p++) {
                    Pod pod = pods.get(p);
                    cpuCapacityConstraint.addTerm(pod.requiredCpu(), x[p][n]);
                    memCapacityConstraint.addTerm(pod.requiredMem(), x[p][n]);
                }
                cplex.addLe(cpuCapacityConstraint, node.getCpuAvail(), node.getName() + "_cpu_capacity");
                cplex.addLe(memCapacityConstraint, node.getMemAvail(), node.getName() + "_mem_capacity");
            }

            // hosting nodes variables constraints
            for (int n = 0; n < N; n++) {
                Node node = nodes.get(n);
                IloLinearNumExpr hostingNodeLBConstraint = cplex.linearNumExpr();
                IloLinearNumExpr hostingNodeUBConstraint = cplex.linearNumExpr();
                hostingNodeLBConstraint.addTerm(1, z[n]);
                hostingNodeUBConstraint.addTerm(1, z[n]);

                for (int p = 0; p < P; p++) {
                    hostingNodeUBConstraint.addTerm(- 1.0, x[p][n]);
                    hostingNodeLBConstraint.addTerm(- 1.0 / GAMMA, x[p][n]);
                }

                double hostingNode = node.hostPods() ? 1.0 : 0.0;
                String label = node.getName() + "z" + (n + 1) + "_definition";
                cplex.addLe(hostingNodeUBConstraint, hostingNode, label);
                cplex.addGe(hostingNodeLBConstraint, hostingNode / GAMMA, label);
            }

            // set consistency and delay constraints
            for (int p = 0; p < P; p++) {
                Pod pod = pods.get(p);

                IloLinearNumExpr consistencyConstraint = cplex.linearNumExpr();

                for (int n = 0; n < N; n++) {
                    Node node = nodes.get(n);

                    consistencyConstraint.addTerm(1, x[p][n]);

                    // delay constraints
                    for (Node gatewayNode : gatewayNodes) {
                        double delay = delayCalculator.delay(node, gatewayNode);
                        IloLinearNumExpr delayConstraint = cplex.linearNumExpr();
                        delayConstraint.addTerm(delay, x[p][n]);
                        String label = pod.getName() + "_delay_from_" + node.getName() + "_to_" + gatewayNode.getName();
                        cplex.addLe(delayConstraint, maxDelay, label);
                    }
                }

                // consistency constraint: each pod allocated on exactly one node
                cplex.addEq(consistencyConstraint, 1, pod.getName() + "_consistency");
            }

            cplex.exportModel("data/out/model.lp");

            // solve the model
            if (cplex.solve()) {  // found a feasible solution
                cplex.output().println("Solution status = " + cplex.getStatus());
                cplex.output().println("Solution value  = " + cplex.getObjValue());
                printAssignmentValues(cplex, x, pods, nodes);
                assignPods(cplex, x, pods, nodes, mappingList);
            }
            else throw new OptimizationException("Did not find a feasible solution");

        }
        catch (IloException e) {
            throw new OptimizationException(e.getMessage());
        }
        finally {
            if (cplex != null) cplex.end();
        }

        return new Placement(mappingList);
    }


    private void addObjective(IloCplex cplex, IloLinearNumExpr obj, PlacementPolicyType mode) throws IloException {
        switch (mode) {
            case MINIMIZE:
                cplex.addMinimize(obj);
                break;
            case SPREAD:
                cplex.addMaximize(obj);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mode);
        }
    }

    private void assignPods(IloCplex cplex, IloNumVar[][] x,
                            List<Pod> pods, List<Node> nodes, List<Mapping> mappingList)
            throws IloException, PlacementException {

        for (int p = 0; p < pods.size(); p++) {
            Pod pod = pods.get(p);
            double[] values = cplex.getValues(x[p]);
            for (int n = 0; n < nodes.size(); n++) {
                Node node = nodes.get(n);
                String podName = pod.getName();
                String nodeName = node.getName();
                if (values[n] == 1.0) {
                    if (node.assignPod(pod)) {
                        mappingList.add(new Mapping(
                                podName,
                                pod.getNamespace(),
                                pod.getUID(),
                                nodeName)
                        );
                        System.err.printf("assigned pod %s to node %s\n", podName, nodeName);
                        System.err.printf("Updated node %s\n", node);
                    } else
                        throw new PlacementException(
                                "Unexpected error: pod can't be assigned to designed node");
                }
            }
        }
    }

    private void printAssignmentValues(IloCplex cplex, IloNumVar[][] x, List<Pod> pods, List<Node> nodes)
            throws IloException {

        cplex.output().println("Pods assignment variables values:");
        StringBuilder output = new StringBuilder(String.format("%-20.20s", "pod name"));
        for (Node node : nodes)
            output.append(String.format("  %-20.20s", node.getName()));
        cplex.output().println(output);

        for (int p = 0; p < pods.size(); p++) {
            String podName = pods.get(p).getName();
            output = new StringBuilder(String.format("%-20.20s", podName));
            double[] values = cplex.getValues(x[p]);
            for (int n = 0; n < nodes.size(); n++)
                output.append(String.format("  %-20d", (int) values[n]));
            cplex.output().println(output);
        }
    }

}
