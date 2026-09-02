package com.quantorbital.mesh.service;

import com.quantorbital.core.crypto.CommandCryptographyEngine;
import com.quantorbital.core.model.SpaceCommand;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPublicKeyParameters;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Handles dense inter-satellite communications in orbit.
 * Leverages Java 21 Virtual Threads (Project Loom) for ultra-lightweight thread allocation per connection.
 */
public class SatelliteMeshNode {

    private static final Logger logger = Logger.getLogger(SatelliteMeshNode.class.getName());
    private final String satelliteId;
    private final CommandCryptographyEngine cryptoEngine;
    private final MLDSAPublicKeyParameters groundStationPublicKey;
    private final ExecutorService virtualThreadExecutor;
    private final ConcurrentHashMap<Long, Boolean> processedCommandsLog;

    public SatelliteMeshNode(String satelliteId, CommandCryptographyEngine cryptoEngine, MLDSAPublicKeyParameters groundStationPublicKey) {
        this.satelliteId = satelliteId;
        this.cryptoEngine = cryptoEngine;
        this.groundStationPublicKey = groundStationPublicKey;
        // Allocates a virtual thread per task instead of expensive operating system platform threads
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.processedCommandsLog = new ConcurrentHashMap<>();
    }

    /**
     * Simulates receiving a high-frequency stream of commands from the constellation mesh network.
     */
    public void onReceiveIncomingCommand(SpaceCommand command) {
        // Delegate command verification to a lightweight virtual thread
        virtualThreadExecutor.submit(() -> {
            if (processedCommandsLog.containsKey(command.commandId())) {
                logger.warning(String.format("[%s] Command %d rejected: Already processed to prevent replay attacks.", satelliteId, command.commandId()));
                return;
            }

            logger.info(String.format("[%s] Critical payload received. Initiating Post-Quantum verification pipeline...", satelliteId));

            // Execute heavy PQC decryption inside the virtual thread
            boolean isAuthentic = cryptoEngine.verifyCommand(command, groundStationPublicKey);

            if (isAuthentic) {
                processedCommandsLog.put(command.commandId(), true);
                executeSatelliteManeuver(command);
            } else {
                triggerSirenSecurityAlert(command);
            }
        });
    }

    private void executeSatelliteManeuver(SpaceCommand command) {
        logger.info(String.format("🚀 [%s] SUCCESS: PQC Validation Passed. Executing mission operation: %s", satelliteId, command.operationType()));
        // Actual thruster or sensor native integration happens here
    }

    private void triggerSirenSecurityAlert(SpaceCommand command) {
        logger.severe(String.format("🚨 🚨 [ALERT] [%s] CRITICAL SECURITY BREACH: Invalid PQC Signature detected for operation %s! Command source is untrusted or quantum-tampered!", 
            satelliteId, command.operationType()));
        // Broadcast quarantine flag to other satellites in the mesh network
    }

    /**
     * Graceful shutdown of the satellite networking node.
     */
    public void shutdownNode() {
        this.virtualThreadExecutor.shutdown();
    }
}
