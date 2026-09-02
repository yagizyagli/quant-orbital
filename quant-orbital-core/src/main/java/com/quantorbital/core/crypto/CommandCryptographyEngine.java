package com.quantorbital.core.crypto;

import com.quantorbital.core.model.SpaceCommand;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class CommandCryptographyEngine {

    public boolean verifyCommand(SpaceCommand command, PublicKey publicKey) {
        if (command.signature() == null || command.signature().length == 0) return false;
        try {
            Signature verifier = Signature.getInstance("Dilithium", BouncyCastlePQCProvider.PROVIDER_NAME);
            verifier.initVerify(publicKey);
            verifier.update(serializeCommandData(command));
            return verifier.verify(command.signature());
        } catch (Exception e) {
            return false;
        }
    }

    public byte[] signCommand(SpaceCommand command, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance("Dilithium", BouncyCastlePQCProvider.PROVIDER_NAME);
            signer.initSign(privateKey);
            signer.update(serializeCommandData(command));
            return signer.sign();
        } catch (Exception e) {
            throw new SecurityException("Signing failure in core uplink", e);
        }
    }

    private byte[] serializeCommandData(SpaceCommand command) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(Long.toString(command.commandId()).getBytes());
            baos.write(command.targetSatelliteId().getBytes());
            baos.write(command.operationType().getBytes());
            return baos.toByteArray();
        }
    }
}
