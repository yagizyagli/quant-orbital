package com.quantorbital.core.crypto;

import org.bouncycastle.pqc.crypto.mldsa.MLDSAKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAKeyPairGenerator;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import java.security.SecureRandom;

/**
 * Core cryptographic manager responsible for generating NIST-standard ML-DSA 
 * (Dilithium) post-quantum key pairs for satellite authentication.
 */
public class PqcKeyManager {

    private final MLDSAKeyPairGenerator keyPairGenerator;

    public PqcKeyManager() {
        this.keyPairGenerator = new MLDSAKeyPairGenerator();
        // ML-DSA-65 provides security roughly equivalent to AES-192, optimal for space constraints
        MLDSAKeyGenerationParameters params = new MLDSAKeyGenerationParameters(
            new SecureRandom(), 
            MLDSAParameters.ml_dsa_65
        );
        this.keyPairGenerator.init(params);
    }

    /**
     * Generates a quantum-resistant asymmetric key pair.
     * @return AsymmetricCipherKeyPair containing PQC public and private components.
     */
    public AsymmetricCipherKeyPair generateSpaceKeyPair() {
        return keyPairGenerator.generateKeyPair();
    }
}
