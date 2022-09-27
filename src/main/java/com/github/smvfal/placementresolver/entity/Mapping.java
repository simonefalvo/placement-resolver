package com.github.smvfal.placementresolver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Mapping {

    private String namePod;
    private String namespacePod;
    @JsonProperty("UID")
    private String UID;
    private String nameNode;

}
