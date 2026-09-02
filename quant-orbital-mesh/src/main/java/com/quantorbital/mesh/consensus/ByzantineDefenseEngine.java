package com.quantorbital.mesh.consensus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * High-reliability security engine that isolates compromised or rogue satellites.
 * Implements a decentralized consensus mechanism based on Byzantine Fault Tolerance (BFT) principles.
 */
public class ByzantineDefenseEngine {

    private static final Logger logger = Logger.getLogger(ByzantineDefenseEngine.class.getName());
    
    private final String localSatelliteId;
    private final int totalConstellationNodes;
    private final int consensusThreshold; // Fault tolerance threshold: (N - 1) / 3

    // Tracks security anomaly reports per satellite: [RogueSatelliteId -> UniqueReporterCount]
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> incidentReports;
    // Tracks blacklisted satellites that are permanently isolated from the mesh network
    private final ConcurrentHashMap<String, Boolean> blacklistedNodes;

    public ByzantineDefenseEngine(String localSatelliteId, int totalConstellationNodes) {
        this.localSatelliteId = localSatelliteId;
        this.totalConstellationNodes = totalConstellationNodes;
        // Classical BFT requirement: System can tolerate up to 'f' failures where N >= 3f + 1
        this.consensusThreshold = (totalConstellationNodes - 1) / 3;
        this.incidentReports = new ConcurrentHashMap<>();
        this.blacklistedNodes = new ConcurrentHashMap<>();
    }

    /**
     * Processes an incoming security alert sent by a peer satellite in the mesh network.
     * 
     * @param reportingNode The satellite that observed and reported the attack.
     * @param suspectNode The satellite suspected of being hacked or malfunctioning.
     */
    public void registerThreatAlert(String reportingNode, String suspectNode) {
        if (blacklistedNodes.containsKey(suspectNode)) {
            return; // Already isolated
        }

        // Thread-safe isolation initialization for suspect node reports
        incidentReports.putIfAbsent(suspectNode, new ConcurrentHashMap<>());
        ConcurrentHashMap<String, Boolean> reporters = incidentReports.get(suspectNode);

        // Record the vote from the reporting satellite
        reporters.put(reportingNode, true);
        int uniqueVotes = reporters.size();

        logger.warning(String.format("[%s] Security alert received from peer [%s] against suspect [%s]. Total unique alert votes: %d/%d",
                localSatelliteId, reportingNode, suspectNode, uniqueVotes, consensusThreshold + 1));

        // Evaluate Byzantine consensus threshold
        if (uniqueVotes > consensusThreshold) {
            isolateRogueNode(suspectNode);
        }
    }

    /**
     * Executes immediate emergency containment of the rogue satellite.
     */
    private void isolateRogueNode(String rogueNodeId) {
        blacklistedNodes.put(rogueNodeId, true);
        logger.severe(String.format("🚨 🚨 [BYZANTINE QUARANTINE TRIGGERED] [%s] Node [%s] has crossed the malicious threshold! " +
                "Decentralized consensus reached. Cutting off all gRPC routing and PQC trust channels to this satellite permanently!", 
                localSatelliteId, rogueNodeId));
    }

    /**
     * Validates whether a specific node is trusted to communicate.
     */
    public boolean isNodeTrusted(String nodeId) {
        return !blacklistedNodes.containsKey(nodeId);
    }
}
