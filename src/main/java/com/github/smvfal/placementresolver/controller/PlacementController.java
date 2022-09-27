package com.github.smvfal.placementresolver.controller;

import com.github.smvfal.placementresolver.entity.Cluster;
import com.github.smvfal.placementresolver.entity.Placement;
import com.github.smvfal.placementresolver.network.DelayLoader;
import com.github.smvfal.placementresolver.network.FileDelayLoader;
import com.github.smvfal.placementresolver.resolver.*;
import com.github.smvfal.placementresolver.resolver.exception.PlacementException;
import com.github.smvfal.placementresolver.utils.TimeLogger;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class PlacementController {

    private final TimeLogger timeLogger = new TimeLogger();

    public void printCluster(Cluster cluster) {
        System.out.println(cluster);
    }

    public Placement networkAwareILPMin(Cluster cluster, double maxDelay) throws PlacementException, IOException {

        DelayLoader delayLoader = new FileDelayLoader();
        Resolver nailpResolver = new NetworkAwareILPResolver(PlacementPolicyType.MINIMIZE, delayLoader, maxDelay);

        long start = System.currentTimeMillis();
        Placement result = nailpResolver.solve(cluster);
        long end = System.currentTimeMillis();
        timeLogger.log(end - start, cluster.getPods().size(), "data/out/ilp-min.csv");

        return result;
    }

    public Placement networkAwareILPSpread(Cluster cluster, double maxDelay) throws PlacementException, IOException {

        DelayLoader delayLoader = new FileDelayLoader();
        Resolver nailpResolver = new NetworkAwareILPResolver(PlacementPolicyType.SPREAD, delayLoader, maxDelay);

        long start = System.currentTimeMillis();
        Placement result = nailpResolver.solve(cluster);
        long end = System.currentTimeMillis();
        timeLogger.log(end - start, cluster.getPods().size(), "data/out/ilp-spread.csv");

        return result;
    }

    public Placement networkAwareHeuristicMin(Cluster cluster, double maxDelay) throws PlacementException, IOException {

        DelayLoader delayLoader = new FileDelayLoader();
        Resolver nahResolver = new NetworkAwareHeuristicResolver(PlacementPolicyType.MINIMIZE, delayLoader, maxDelay);

        long start = System.currentTimeMillis();
        Placement result = nahResolver.solve(cluster);
        long end = System.currentTimeMillis();
        timeLogger.log(end - start, cluster.getPods().size(), "data/out/eur-min.csv");

        return result;
    }

    public Placement networkAwareHeuristicSpread(Cluster cluster, double maxDelay) throws PlacementException, IOException {

        DelayLoader delayLoader = new FileDelayLoader();
        Resolver nahResolver = new NetworkAwareHeuristicResolver(PlacementPolicyType.SPREAD, delayLoader, maxDelay);

        long start = System.currentTimeMillis();
        Placement result = nahResolver.solve(cluster);
        long end = System.currentTimeMillis();
        timeLogger.log(end - start, cluster.getPods().size(), "data/out/eur-spread.csv");

        return result;
    }

    public Placement roundRobin(Cluster cluster) throws PlacementException, IOException {

        Resolver rrResolver = RoundRobinResolver.getInstance();

        long start = System.currentTimeMillis();
        Placement result = rrResolver.solve(cluster);
        long end = System.currentTimeMillis();
        timeLogger.log(end - start, cluster.getPods().size(), "data/out/rr.csv");

        return result;
    }

    public Placement greedyFirstFit(Cluster cluster) throws PlacementException, IOException {

        Resolver gffResolver = new GreedyFirstFitResolver();

        long start = System.currentTimeMillis();
        Placement result = gffResolver.solve(cluster);
        long end = System.currentTimeMillis();
        timeLogger.log(end - start, cluster.getPods().size(), "data/out/gff.csv");

        return result;
    }

}
