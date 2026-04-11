package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.entity.Certificate;
import com.phelim.system.love_certificate.entity.CertificateSession;
import com.phelim.system.love_certificate.enums.CertificateType;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final SpringTemplateEngine templateEngine;
    private final QrService qrService;

    public String renderCertificate(Certificate cert, CertificateSession session, String verifyUrl) {

        log.info("[TemplateService][renderCertificate] Start render certId={}", cert.getCertId());

        Context context = new Context();

        context.setVariable("maleName", session.getMaleName());
        context.setVariable("femaleName", session.getFemaleName());
        context.setVariable("days", cert.getDurationDays());
        context.setVariable("type", cert.getType());

        String qrBase64 = qrService.generateBase64(verifyUrl);

        if (qrBase64 == null || qrBase64.isBlank()) {
            log.warn("[TemplateService][renderCertificate] QR generation failed certId={}", cert.getCertId());
            throw new BusinessException(ErrorCode.QR_GENERATION_FAILED, "certId=" + cert.getCertId());
        }

        log.debug("[TemplateService][renderCertificate] QR length={}", qrBase64.length());

        context.setVariable("qr", qrBase64);

        // 🔥 2. Resolve template
        String templateName = resolveTemplate(cert.getType());

        log.info("[TemplateService][renderCertificate] Using template={}", templateName);

        try {
            String html = templateEngine.process(templateName, context);

            // Debug HTML
            Path path = Path.of(
                    BaseConstants.BASE_PATH,
                    BaseConstants.HTML_PATH,
                    BaseConstants.CERTIFICATE_NAME_HTML_FORMAT
            );
            try {
                Path parentDir = path.getParent();
                if (parentDir != null && Files.notExists(parentDir)) {
                    Files.createDirectories(parentDir);
                    log.info("[TemplateService] Created directory: {}", parentDir);
                }
                // Save file
                Files.writeString(path, html);
            } catch (Exception e) {
                log.warn("[TemplateService] Cannot write file={}, path={}, error={}",
                        BaseConstants.CERTIFICATE_NAME_HTML_FORMAT, path.getFileName(), e.getMessage(), e);
            }
            return html;

        } catch (Exception ex) {
            log.error("[TemplateService][renderCertificate] Template render failed. template={}, error={}",
                    templateName, ex.getMessage(), ex);

            throw new BusinessException(ErrorCode.TEMPLATE_RENDER_FAILED, "template=" + templateName);
        }
    }

    /**
     * Resolve template by certificate type
     */
    private String resolveTemplate(CertificateType type) {

        if (type == null) {
            return BaseConstants.A_CERTIFICATE_TEMPLATE_NAME;
        }

        return switch (type) {
            case CertificateType.A -> BaseConstants.A_CERTIFICATE_TEMPLATE_NAME;
            case CertificateType.B -> BaseConstants.B_CERTIFICATE_TEMPLATE_NAME;
            case CertificateType.C -> BaseConstants.C_CERTIFICATE_TEMPLATE_NAME;
            default -> {
                log.warn("[TemplateService][resolveTemplate] Unknown type={}, fallback to A", type);
                yield BaseConstants.A_CERTIFICATE_TEMPLATE_NAME;
            }
        };
    }
}
