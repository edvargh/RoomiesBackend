package com.roomies.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Central place for sending all transactional e-mails
 * (verification, password-reset, etc.).
 */
@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  @Value("${roomies.mail.verify-base-url:http://localhost:8080/api/auth/confirm}")
  private String verifyBaseUrl;

  @Value("${roomies.mail.reset-base-url:http://localhost:8080/reset-password}")
  private String resetBaseUrl;

  @Value("${roomies.mail.reset-valid-min}")
  private int resetValidMin;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /** Called right after successful registration. */
  public void sendVerificationMail(String toEmail, String token) {
    String subject = "Confirm your Roomies account";
    String url     = verifyBaseUrl + "?token=" + token;

    String html = """
        <html>
          <body style="font-family:sans-serif">
            <h2>Welcome to <span style="color:#3b82f6">Roomies</span> 🎉</h2>
            <p>Please verify your e-mail to unlock all features.</p>
            <p style="margin:24px 0">
              <a href="%s"
                 style="background:#3b82f6;color:white;padding:12px 24px;
                        text-decoration:none;border-radius:6px;">
                Verify my e-mail
              </a>
            </p>
            <small>If you didn’t create a Roomies account, just ignore this message.</small>
          </body>
        </html>
        """.formatted(url);

    sendHtmlMail(toEmail, subject, html);
  }

  /** Same content – used when user taps “Resend verification link”. */
  public void resendVerificationMail(String toEmail, String token) {
    sendVerificationMail(toEmail, token);
  }

  /** Password-reset flow (user clicked “Forgot password”). */
  public void sendPasswordResetMail(String toEmail, String token) {
    String subject = "Reset your Roomies password";
    String url     = resetBaseUrl + "?token=" + token;

    String html = """
        <html>
          <body style="font-family:sans-serif">
            <h2>Password reset</h2>
            <p>Click the button below to choose a new password.
               The link is valid for %s.</p>
            <p style="margin:24px 0">
              <a href="%s"
                 style="background:#3b82f6;color:white;padding:12px 24px;
                        text-decoration:none;border-radius:6px;">
                Reset password
              </a>
            </p>
            <small>If you didn’t request this, you can safely ignore it.</small>
          </body>
        </html>
        """.formatted(Duration.ofMinutes(resetValidMin).toString().substring(2).toLowerCase(), url);

    sendHtmlMail(toEmail, subject, html);
  }

  /* ----------------------------------------------------------------
   * Internal helper
   * ---------------------------------------------------------------- */

  private void sendHtmlMail(String to, String subject, String html) {
    MimeMessage message = mailSender.createMimeMessage();

    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(html, true);

      mailSender.send(message);
      log.info("Mail sent to {}: {}", to, subject);
    } catch (MessagingException ex) {
      throw new MailSendException(
          String.format("Unable to send e-mail to %s (subject: %s)", to, subject),
          ex
      );
    }
  }
}
