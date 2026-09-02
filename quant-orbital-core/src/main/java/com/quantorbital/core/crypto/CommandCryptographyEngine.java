package com.quantorbital.core.crypto;

import com.quantorbital.core.model.SpaceCommand;
import org.bouncycastle.pqc.crypto.mldsa.MLDSASigner;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPublicKeyParameters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Mission-critical engine running edge-side on the satellite to sign and verify
 * incoming control commands using post-quantum signatures.
 */
public class CommandCryptographyEngine {

    /**
     * Verifies if the incoming SpaceCommand has a valid quantum-resistant signature.
     * 
     * @param command The received space command containing the payload and signature.
     * @param publicKey The trusted public key of the ground control station.
     * @return true if the signature is authentic, false if compromised.
     */
    public boolean verifyCommand(SpaceCommand command, MLDSAPublicKeyParameters publicKey) {
        if (command.signature() == null || command.signature().length == 0) {
            return false;
        }

        try {
            MLDSASigner verifier = new MLDSASigner();
            verifier.init(false, publicKey); // false indicates verification mode

            byte[] dataToVerify = serializeCommandData(command);
            return verifier.verifySignature(dataToVerify, command.signature());
        } catch (Exception e) {
            // In aviation, fail-secure approach is mandatory. Any exception equals rejection.
            return false;
        }
    }

    /**
     * Signs a command data payload at the ground station before uplink.
     */
    public byte[] signCommand(SpaceCommand command, MLDSAPrivateKeyParameters privateKey) {
        try {
            MLDSASigner signer = new MLDSASigner();
            signer.init(true, privateKey); // true indicates signing mode

            byte[] dataToSign = serializeCommandData(command);
            return signer.generateSignature(dataToSign);
        } catch (Exception e) {
            throw new SecurityException("Critical failure during post-quantum signing pipeline", e);
        }
    }

    /**
     * Serializes command metadata and payload deterministically into a binary stream.
     */
    private byte[] serializeCommandData(SpaceCommand command) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(Long.toString(command.commandId()).getBytes());
            baos.write(command.targetSatelliteId().getBytes());
            baos.write(command.operationType().getBytes());
            baos.write(command.timestamp().toString().getBytes());
            if (command.payload() != null) {
                baos.write(command.payload());
            }
            return baos.toByteArray();
        }
    }
}
