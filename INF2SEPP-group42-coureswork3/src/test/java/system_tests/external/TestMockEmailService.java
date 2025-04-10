package system_tests.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import external.MockEmailService;
import external.EmailService;

public class TestMockEmailService {

    private MockEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new MockEmailService();
    }

    @Test
    @DisplayName("Valid sender and recipient should return STATUS_SUCCESS")
    public void testSendEmail_ValidSenderAndRecipient_ReturnsSuccess() {
        int result = emailService.sendEmail("sender@example.com", "recipient@example.com", "Subject", "Body");
        assertEquals(EmailService.STATUS_SUCCESS, result,
                "Expected STATUS_SUCCESS for valid sender and recipient emails.");
    }

    @Test
    @DisplayName("Invalid sender email should return STATUS_INVALID_SENDER_EMAIL")
    public void testSendEmail_InvalidSenderEmail_ReturnsInvalidSender() {
        int result = emailService.sendEmail("invalid-email", "recipient@example.com", "Subject", "Body");
        assertEquals(EmailService.STATUS_INVALID_SENDER_EMAIL, result,
                "Expected STATUS_INVALID_SENDER_EMAIL for invalid sender address.");
    }

    @Test
    @DisplayName("Invalid recipient email should return STATUS_INVALID_RECIPIENT_EMAIL")
    public void testSendEmail_InvalidRecipientEmail_ReturnsInvalidRecipient() {
        int result = emailService.sendEmail("sender@example.com", "not-an-email", "Subject", "Body");
        assertEquals(EmailService.STATUS_INVALID_RECIPIENT_EMAIL, result,
                "Expected STATUS_INVALID_RECIPIENT_EMAIL for invalid recipient address.");
    }
}