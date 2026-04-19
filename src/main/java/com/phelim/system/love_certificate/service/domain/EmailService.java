package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailJobService emailJobService;

    @Value("${app.mail.from}")
    private String from;

    @Async(BaseConstants.EXECUTOR_SCHEDULER)
    public void sendMilestoneEmail(String toEmail, String maleName, String femaleName, int days, String label) {
        log.info("[EmailService][sendMilestoneEmail] Start to={}, days={}, label={}", toEmail, days, label);

        String subject = "🎉 " + label + " - Your Love Milestone!";

        String body = String.format("""
                Dear %s & %s 💖

                Today marks your special milestone: %s

                You have been together for %d days 🎉

                Wishing you endless love and happiness 💕
                
                — Love Certificate System
                """,
                maleName,
                femaleName,
                label,
                days);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        log.info("[EmailService][sendMilestoneEmail] Success to={}", toEmail);
        try {
            mailSender.send(message);
            log.info("[EmailService][sendMilestoneEmail] Success toEmail={}", toEmail);
        } catch (Exception ex) {
            log.error("[EmailService][sendMilestoneEmail] Failed toEmail={}", toEmail, ex);
            emailJobService.saveFailedJob(toEmail, subject, body, ex.getMessage());
        }
    }
}
