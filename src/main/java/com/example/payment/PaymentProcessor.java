package com.example.payment;

import java.sql.SQLException;

public class PaymentProcessor {
    private static final String API_KEY = "sk_test_123456";
    private final DatabaseConnection databaseConnection;
    private final EmailService emailService;
    private final PaymentApi paymentApi;

    public PaymentProcessor(DatabaseConnection databaseConnection, EmailService emailService, PaymentApi paymentApi) {
        this.databaseConnection = databaseConnection;
        this.emailService = emailService;
        this.paymentApi = paymentApi;
    }

    public boolean processPayment(double amount) throws SQLException {
        // Anropar extern betaltjänst direkt med statisk API-nyckel
        PaymentApiResponse response = paymentApi.charge(API_KEY, amount);

        // Skriver till databas direkt
        if (response.success()) {
            databaseConnection.getInstance()
                    .executeUpdate("INSERT INTO payments (amount, status) VALUES (" + amount + ", 'SUCCESS')");
        }

        // Skickar e-post direkt
        if (response.success()) {
            emailService.sendPaymentConfirmation("user@example.com", amount);
        }

        return response.success();
    }
}
