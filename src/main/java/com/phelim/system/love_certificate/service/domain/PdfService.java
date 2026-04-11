package com.phelim.system.love_certificate.service.domain;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;

@Service
@Slf4j
public class PdfService {

    public byte[] generatePdf(String html) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            String baseUrl = new File("src/main/resources/").toURI().toString();
            builder.withHtmlContent(html, baseUrl);
            builder.toStream(out);
            builder.run();

            return out.toByteArray();

        } catch (Exception ex) {
            log.error("[PdfService][generatePdf] PDF generation failed", ex);
            throw new BusinessException(ErrorCode.PDF_GENERATION_FAILED, null);
        }
    }
}
