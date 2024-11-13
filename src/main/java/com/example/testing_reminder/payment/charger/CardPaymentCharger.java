package com.example.testing_reminder.payment.charger;

import com.example.testing_reminder.payment.Currency;

import java.math.BigDecimal;

public interface CardPaymentCharger {
    CardPaymentCharge chargeCard(
            String cardSource,
            BigDecimal amount,
            Currency currency,
            String description
    );
}
