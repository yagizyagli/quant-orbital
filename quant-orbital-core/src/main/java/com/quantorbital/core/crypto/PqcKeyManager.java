package com.quantorbital.core.crypto;

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;

public class PqcKeyManager {
    static {
        if (Security.getProvider(BouncyCastlePQCProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastlePQCProvider());
        }
    }

    public KeyPair generateSpaceKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("Dilithium", BouncyCastlePQCProvider.PROVIDER_NAME);
            // Strictly passing the exact DilithiumParameterSpec required by BouncyCastle 1.78+
            kpg.initialize(DilithiumParameterSpec.dilithium3, new SecureRandom()); 
            return kpg.generateKeyPair();
        } catch (Exception e) {
            throw new SecurityException("Failed to generate PQC Key Pair via JCA provider", e);
        }
    }
}
