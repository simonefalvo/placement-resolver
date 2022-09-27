package com.github.smvfal.placementresolver.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Placement {
    List<Mapping> deployment;
}
