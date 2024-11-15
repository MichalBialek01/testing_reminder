package com.example.testing_reminder.payment.stripe;

import com.example.testing_reminder.payment.Currency;
import com.example.testing_reminder.payment.charger.CardPaymentCharge;
import com.example.testing_reminder.payment.charger.CardPaymentCharger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(
        value = "stripe.enabled",
        havingValue = "false"
)
public class MockStripeService implements CardPaymentCharger {
    @Override
    public CardPaymentCharge chargeCard(String cardSource, BigDecimal amount, Currency currency, String description) {
        return new CardPaymentCharge(true);
    }
}
