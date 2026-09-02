package com.quantorbital.core.model;

import java.time.Instant;
import java.util.Arrays;

/**
 * Represents an immutable mission-critical command sent to the satellite.
 * Designed using Java Records for zero-allocation performance overhead.
 */
public record SpaceCommand(
    long commandId,
    String targetSatelliteId,
    String operationType,       // e.g., "ORBIT_SHIFT", "PROPULSION_ON", "SHUTDOWN"
    Instant timestamp,
    byte[] payload,             // Raw binary parameters for the command
    byte[] signature            // Post-Quantum Digital Signature (ML-DSA / Dilithium)
) {
    // Custom compact constructor for structural validation
    public SpaceCommand {
        if (targetSatelliteId == null || targetSatelliteId.isBlank()) {
            throw new IllegalArgumentException("Target satellite ID cannot be empty.");
        }
        if (operationType == null || operationType.isBlank()) {
            throw new IllegalArgumentException("Operation type must be explicitly defined.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Command timestamp is mandatory.");
        }
    }

    // Custom equals/hashCode/toString override since array identities in records default to reference checks
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpaceCommand that)) return false;
        return commandId == that.commandId &&
               targetSatelliteId.equals(that.targetSatelliteId) &&
               operationType.equals(that.operationType) &&
               timestamp.equals(that.timestamp) &&
               Arrays.equals(payload, that.payload) &&
               Arrays.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(commandId);
        result = 31 * result + targetSatelliteId.hashCode();
        result = 31 * result + operationType.hashCode();
        result = 31 * result + timestamp.hashCode();
        result = 31 * result + Arrays.hashCode(payload);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }
}
