package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.tool.RsaKeyGenerator;
import com.phelim.system.love_certificate.util.KeyLoaderUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class RsaSignatureService {

    private final KeyLoaderUtil keyLoaderUtil;
    private final RsaKeyGenerator rsaKeyGenerator;

    @Value("${local.public-key.path}")
    private String publicKeyPemPath;
    @Value("${local.private-key.path}")
    private String privateKeyPemPath;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    //Manual
    @PostConstruct
    public void init() {
        try {
            String privateKeyPem = keyLoaderUtil.loadPemFromResource(privateKeyPemPath);
            String publicKeyPem = keyLoaderUtil.loadPemFromResource(publicKeyPemPath);

            privateKey = loadPrivateKey(privateKeyPem);
            publicKey = loadPublicKey(publicKeyPem);

            if (privateKey == null || publicKey == null) {
                log.error("[RsaSignatureService][init] RSA keys not initialized");
                throw new BusinessException(ErrorCode.RSA_KEYS_NOT_INITIALIZED, null);
            }
            log.info("[RsaSignatureService][init] RSA keys loaded successfully");
        } catch (IOException e) {
            log.error("[RsaSignatureService][init] Failed to load key files", e);
            throw new BusinessException(ErrorCode.LOAD_KEY_FAILED, "Cannot read key file");

        } catch (Exception e) {
            log.error("[RsaSignatureService][init] Failed to parse RSA key", e);
            throw new BusinessException(ErrorCode.LOAD_KEY_FAILED, "Invalid key format");
        }
    }

    private PrivateKey loadPrivateKey(String pemContent) throws Exception {
        String key = pemContent
                .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

        return KeyFactory.getInstance(BaseConstants.RSA).generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String pemContent) throws Exception {
        String key = pemContent
                .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

        return KeyFactory.getInstance(BaseConstants.RSA).generatePublic(spec);
    }

    public String sign(byte[] data) {
        try {
            Signature sig = Signature.getInstance(BaseConstants.ALGORITHM_SHA256_WITH_RSA);
            sig.initSign(privateKey);
            sig.update(data);
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            log.error("[RsaSignatureService][sign] Failed to sign data", e);
            throw new BusinessException(ErrorCode.SIGNATURE_SIGN_FAILED, null);
        }
    }

    public boolean verify(byte[] data, String signatureStr) {
        try {
            Signature sig = Signature.getInstance(BaseConstants.ALGORITHM_SHA256_WITH_RSA);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(Base64.getDecoder().decode(signatureStr));
        } catch (Exception e) {
            log.error("[RsaSignatureService][verify] Failed to verify signature. exception ={}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SIGNATURE_VERIFY_FAILED, null);
        }
    }

}
