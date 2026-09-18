package com.breakupstories.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailSender {

    // Separate from SMTP username so a transactional provider (Brevo/Resend/SES) can be swapped in via env.
    @Value("${BREAKUP_MAIL_FROM:${spring.mail.username}}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public void sendGmail(String to, String subject, String content)
            throws MessagingException, UnsupportedEncodingException {
        sendGmail(to, subject, null, content);
    }

    /** Sends multipart/alternative (plain + HTML); HTML-only mail scores worse with spam filters. */
    public void sendGmail(String to, String subject, String plainText, String html)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail, "Heal");
        helper.setTo(to);
        helper.setSubject(subject);
        if (plainText != null) {
            helper.setText(plainText, html);
        } else {
            helper.setText(html, true);
        }
        mailSender.send(message);
        log.info("Gmail sent successfully to {}", to);
    }
}