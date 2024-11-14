package com.example.testing_reminder.payment.stripe;

import com.example.testing_reminder.payment.Currency;
import com.example.testing_reminder.payment.charger.CardPaymentCharge;
import com.example.testing_reminder.payment.charger.CardPaymentCharger;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripeService implements CardPaymentCharger {

    private final StripeApi stripeApi;

    @Override
    public CardPaymentCharge chargeCard(String cardSource, BigDecimal amount, Currency currency, String description) {

    Stripe.apiKey = "sk_test_7mJuPfZsBzc3JkrANrFrcDqC";
    RequestOptions option = RequestOptions.builder()
                .setApiKey(Stripe.apiKey)
                .build();

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amount.multiply(new BigDecimal(100)).longValueExact())
                        .setCurrency(currency.toString())
                        .setPaymentMethod(cardSource)
                        .setDescription(description)
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();
        try {
            PaymentIntent paymentIntent = stripeApi.create(params,option);
            boolean isPaid = "succeeded".equals(paymentIntent.getStatus());
            return new CardPaymentCharge(isPaid);
        } catch (StripeException e) {
            throw new IllegalStateException("Can not make stripe charge",e);
        }
    }
}
