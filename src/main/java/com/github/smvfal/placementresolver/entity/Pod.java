package com.github.smvfal.placementresolver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class Pod {

    @JsonProperty("UID")
    private String UID;
    private String name;
    private String namespace;
    private String appName;
    private double cpuReqs;
    private double cpuLimits;
    private double memReqs;
    private double memLimits;


    @Override
    public String toString() {
        return "Pod{" +
                "name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                //", appName='" + appName + '\'' +
                ", cpuReqs=" + cpuReqs +
                ", cpuLimits=" + cpuLimits +
                ", memReqs=" + memReqs +
                ", memLimits=" + memLimits +
                ", UID='" + UID + '\'' +
                '}';
    }

    public boolean belongToDeployment(String deploymentName) {
        Pattern pattern = Pattern.compile("^" + deploymentName + "-");
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    public double requiredCpu() {
        return cpuReqs;
    }

    public double requiredMem() {
        return memReqs;
    }
}
