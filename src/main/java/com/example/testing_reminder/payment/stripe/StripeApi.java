package com.example.testing_reminder.payment.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripeApi {
    public PaymentIntent create(PaymentIntentCreateParams params, RequestOptions options) throws StripeException {
        return PaymentIntent.create(params,options);
    }

}
