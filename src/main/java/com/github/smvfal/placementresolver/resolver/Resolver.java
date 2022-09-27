package com.github.smvfal.placementresolver.resolver;

import com.github.smvfal.placementresolver.entity.Cluster;
import com.github.smvfal.placementresolver.entity.Placement;
import com.github.smvfal.placementresolver.resolver.exception.PlacementException;

public interface Resolver {

    Placement solve(Cluster cluster) throws PlacementException;
}
