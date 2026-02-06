package com.example;

import com.example.payment.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorTest {

    @Mock
    private PaymentApi paymentApi;

    @Mock
    private DatabaseConnection databaseConnection;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    @Test
    @DisplayName("Should return true and save to database when payment is successful")
    void paymentSuccess() throws SQLException {
        PaymentApiResponse response = new PaymentApiResponse(true);
        double amount = 100.0;

        Mockito.when(paymentApi.charge("sk_test_123456", amount)).thenReturn(response);
        boolean result = paymentProcessor.processPayment(amount);

        assertThat(result).isTrue();
        verify(databaseConnection).executeUpdate(anyString());
    }

    @Test
    @DisplayName("Should send email notification with correct details on success")
    void sendEmailNotificationWhenPaymentIsSuccessful() throws SQLException {
        PaymentApiResponse response = new PaymentApiResponse(true);
        double amount = 100.0;
        String email = "user@example.com";

        Mockito.when(paymentApi.charge("sk_test_123456", amount)).thenReturn(response);
        boolean result = paymentProcessor.processPayment(amount);

        assertThat(result).isTrue();
        verify(databaseConnection).executeUpdate(anyString());
        verify(emailService).sendPaymentConfirmation(email, amount);
    }

    @Test
    @DisplayName("Should return false and perform no side effects when payment fails")
    void paymentFails() throws SQLException {
        PaymentApiResponse response = new PaymentApiResponse(false);
        double amount = 100.0;

        Mockito.when(paymentApi.charge("sk_test_123456", amount)).thenReturn(response);
        boolean result = paymentProcessor.processPayment(amount);

        assertThat(result).isFalse();
        verify(databaseConnection, never()).executeUpdate(anyString());
        verify(emailService, never()).sendPaymentConfirmation(anyString(), anyDouble());
    }

}
