package com.github.smvfal.placementresolver.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Cluster {

    private List<Pod> pods;
    private List<Node> nodes;

    @Override
    public String toString() {
        return "Cluster{" +
                "podList=" + pods +
                ", nodeList=" + nodes +
                '}';
    }
}
