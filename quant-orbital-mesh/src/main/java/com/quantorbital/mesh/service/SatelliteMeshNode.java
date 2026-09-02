package com.quantorbital.mesh.service;

import com.quantorbital.core.crypto.CommandCryptographyEngine;
import com.quantorbital.core.model.SpaceCommand;
import java.security.PublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Handles dense inter-satellite communications in orbit.
 * Leverages Java 21 Virtual Threads for lightweight thread allocation per connection.
 */
public class SatelliteMeshNode {

    private static final Logger logger = Logger.getLogger(SatelliteMeshNode.class.getName());
    private final String satelliteId;
    private final CommandCryptographyEngine cryptoEngine;
    private final PublicKey groundPublicKey;
    private final ExecutorService virtualThreadExecutor;
    private final ConcurrentHashMap<Long, Boolean> processedCommandsLog;

    public SatelliteMeshNode(String satelliteId, CommandCryptographyEngine cryptoEngine, PublicKey groundPublicKey) {
        this.satelliteId = satelliteId;
        this.cryptoEngine = cryptoEngine;
        this.groundPublicKey = groundPublicKey;
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.processedCommandsLog = new ConcurrentHashMap<>();
    }

    /**
     * Simulates receiving a high-frequency stream of commands from the constellation mesh network.
     */
    public void onReceiveIncomingCommand(SpaceCommand command) {
        virtualThreadExecutor.submit(() -> {
            if (processedCommandsLog.containsKey(command.commandId())) {
                logger.warning(String.format("[%s] Command %d rejected: Already processed to prevent replay attacks.", satelliteId, command.commandId()));
                return;
            }

            logger.info(String.format("[%s] Critical payload received. Initiating Post-Quantum verification pipeline...", satelliteId));

            // Execute PQC decryption via standard security architecture
            boolean isAuthentic = cryptoEngine.verifyCommand(command, groundPublicKey);

            if (isAuthentic) {
                processedCommandsLog.put(command.commandId(), true);
                logger.info(String.format("🚀 [%s] SUCCESS: PQC Validation Passed. Executing mission operation: %s", satelliteId, command.operationType()));
            } else {
                logger.severe(String.format("🚨 🚨 [ALERT] [%s] CRITICAL SECURITY BREACH: Invalid PQC Signature detected for operation %s!", satelliteId, command.operationType()));
            }
        });
    }

    /**
     * Graceful shutdown of the satellite networking node.
     */
    public void shutdownNode() {
        this.virtualThreadExecutor.shutdown();
    }
}
