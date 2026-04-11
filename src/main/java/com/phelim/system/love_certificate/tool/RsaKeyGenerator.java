package com.phelim.system.love_certificate.tool;

import com.phelim.system.love_certificate.constant.BaseConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@Slf4j
@Component
public class RsaKeyGenerator {

    //Manual
    public static void main(String[] args) throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(BaseConstants.RSA);
        keyGen.initialize(2048);

        KeyPair pair = keyGen.generateKeyPair();
        String basePrivateKeyPath = BaseConstants.GENERATED_KEY_BASE_PATH + BaseConstants.PRIVATE_KEY_PEM;
        String basePublicKeyPath = BaseConstants.GENERATED_KEY_BASE_PATH + BaseConstants.PUBLIC_KEY_PEM;

        // private key
        String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

        try (FileWriter writer = new FileWriter(basePrivateKeyPath)) {
            writer.write("-----BEGIN PRIVATE KEY-----\n");
            writer.write(privateKey);
            writer.write("\n-----END PRIVATE KEY-----");
        }

        log.info("[RsaKeyGenerator][main] Private Key generated successfully!, path={}", basePrivateKeyPath);

        // public key
        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

        try (FileWriter writer = new FileWriter(basePublicKeyPath)) {
            writer.write("-----BEGIN PUBLIC KEY-----\n");
            writer.write(publicKey);
            writer.write("\n-----END PUBLIC KEY-----");
        }

        log.info("[RsaKeyGenerator][main] Public Key generated successfully!, path={}", basePublicKeyPath);
    }
}

