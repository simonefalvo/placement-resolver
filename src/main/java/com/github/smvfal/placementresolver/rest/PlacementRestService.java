package com.github.smvfal.placementresolver.rest;

import com.github.smvfal.placementresolver.controller.PlacementController;
import com.github.smvfal.placementresolver.entity.Cluster;
import com.github.smvfal.placementresolver.entity.Placement;
import com.github.smvfal.placementresolver.resolver.exception.PlacementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;


@RestController
@RequestMapping(path = "placement")
public class PlacementRestService {

    @Autowired
    private PlacementController placementController;


    @RequestMapping(path = "", method = RequestMethod.POST)
    public ResponseEntity<Cluster> printCluster(@RequestBody Cluster cluster) {

        placementController.printCluster(cluster);

        return new ResponseEntity<>(cluster, HttpStatus.OK);
    }

    @RequestMapping(path = "roundrobin", method = RequestMethod.POST)
    public ResponseEntity<Placement> roundRobin(@RequestBody Cluster cluster) {

        Placement result;

        try {
            result = placementController.roundRobin(cluster);
        } catch (PlacementException | IOException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.OK, e.getMessage(), e.getCause());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(path = "greedyfirstfit", method = RequestMethod.POST)
    public ResponseEntity<Placement> greedyFirstFit(@RequestBody Cluster cluster) {

        Placement result;

        try {
            result = placementController.greedyFirstFit(cluster);
        } catch (PlacementException | IOException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.OK, e.getMessage(), e.getCause());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(path = "networkawareheuristic/min/{maxDelay}", method = RequestMethod.POST)
    public ResponseEntity<Placement> networkAwareHeuristicMin(@RequestBody Cluster cluster,
                                                              @PathVariable double maxDelay) {
        Placement result;

        try {
            result = placementController.networkAwareHeuristicMin(cluster, maxDelay);
        } catch (PlacementException | IOException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.OK, e.getMessage(), e.getCause());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(path = "networkawareheuristic/spread/{maxDelay}", method = RequestMethod.POST)
    public ResponseEntity<Placement> networkAwareHeuristicSpread(@RequestBody Cluster cluster,
                                                                 @PathVariable double maxDelay) {
        Placement result;

        try {
            result = placementController.networkAwareHeuristicSpread(cluster, maxDelay);
        } catch (PlacementException | IOException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.OK, e.getMessage(), e.getCause());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(path = "networkawareilp/min/{maxDelay}", method = RequestMethod.POST)
    public ResponseEntity<Placement> networkAwareILPMin(@RequestBody Cluster cluster,
                                                        @PathVariable double maxDelay) {
        Placement result;

        try {
            result = placementController.networkAwareILPMin(cluster, maxDelay);
        } catch (PlacementException | IOException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.OK, e.getMessage(), e.getCause());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(path = "networkawareilp/spread/{maxDelay}", method = RequestMethod.POST)
    public ResponseEntity<Placement> networkAwareILPSpread(@RequestBody Cluster cluster,
                                                           @PathVariable double maxDelay) {
        Placement result;

        try {
            result = placementController.networkAwareILPSpread(cluster, maxDelay);
        } catch (PlacementException | IOException e) {
            System.err.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.OK, e.getMessage(), e.getCause());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}

