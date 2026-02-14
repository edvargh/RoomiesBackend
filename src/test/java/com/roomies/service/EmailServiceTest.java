package com.roomies.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the EmailService class.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;
  @Mock private MimeMessage mimeMessage;

  @InjectMocks private EmailService emailService;

  @BeforeEach
  void setup() {
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
  }

  @Test
  void sendVerificationMail_shouldSendEmail() {
    // Arrange
    String email = "user@example.com";
    String token = "test-token";

    // Act & Assert (no exception means success)
    assertDoesNotThrow(() -> emailService.sendVerificationMail(email, token));

    // Verify that mailSender.send was called
    verify(mailSender).send(mimeMessage);
  }

  @Test
  void resendVerificationMail_shouldDelegateToSendVerificationMail() {
    // Arrange
    String email = "resend@example.com";
    String token = "resend-token";

    // Act
    emailService.resendVerificationMail(email, token);

    // Assert
    verify(mailSender).send(mimeMessage);
  }

  @Test
  void sendPasswordResetMail_shouldSendEmail() {
    // Arrange
    String email = "reset@example.com";
    String token = "reset-token";

    // Act & Assert
    assertDoesNotThrow(() -> emailService.sendPasswordResetMail(email, token));

    // Verify that mailSender.send was called
    verify(mailSender).send(mimeMessage);
  }

  @Test
  void sendHtmlMail_shouldThrowMailSendException() {
    // Arrange
    doThrow(new MailSendException("Simulated failure"))
        .when(mailSender)
        .send(any(MimeMessage.class));

    // Act & Assert
    MailSendException ex = assertThrows(MailSendException.class, () ->
        emailService.sendVerificationMail("fail@example.com", "token123")
    );

    assertTrue(ex.getMessage().contains("Simulated failure"));
  }
}
