package com.phelim.system.love_certificate.service.domain;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@Slf4j
public class QrService {

    public String generateBase64(String text) {

        try {
            QRCodeWriter writer = new QRCodeWriter();

            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);

            byte[] bytes = out.toByteArray();

            return Base64.getEncoder().encodeToString(bytes);

        } catch (Exception ex) {
            log.error("[QrService][generateBase64] QR generation failed", ex);
            throw new BusinessException(ErrorCode.QR_GENERATION_FAILED, null);
        }
    }
}
