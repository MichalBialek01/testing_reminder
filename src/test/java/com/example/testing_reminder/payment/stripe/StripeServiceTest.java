package com.example.testing_reminder.payment.stripe;

import com.example.testing_reminder.payment.Currency;
import com.example.testing_reminder.payment.charger.CardPaymentCharge;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.validation.constraints.AssertTrue;
import org.apache.coyote.Request;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
/***
 * Problematyczna linijka: PaymentIntent paymentIntent = PaymentIntent.create(params,option);
 * 1. Łaczymy się z usługą Stripe
 * 2. Jest to statyczna metoda
 *
 * Rozwiązanie:
 * 1. Zewnetrzny framework do symulaacji statycznych metod (PowerMock)
 * 2. Ztworzenie serwisu symulującego metodę statyczną (StripeApi), który jest wrapperem metody statycznej.
 *    Wstrzkujemy go do implementacji i zastępujemy metode statyczną, i możemy go też wstrzyknąć do klasy testowej i mockować. (genialne)
 *
 *
 *
 *  Powinno być jeszcze sprawdzenie, negatywnego case, gdzie PaymentIntentCreateParams potrzymuje nieprawidłowe pola....
 *
 */
class StripeServiceTest {
    @InjectMocks
    private StripeService stripeService;
    @Mock
    private StripeApi stripeApi;
    @Mock
    private StripeException stripeException;
    @Mock
    private CardPaymentCharge cardPaymentCharge;
    @Captor
    private ArgumentCaptor<PaymentIntentCreateParams> paymentIntentCreateParamsArgumentCaptor;
    @Captor
    private ArgumentCaptor<RequestOptions> requestOptionsArgumentCaptor;

    @Test
    void itShouldChargeCard() throws StripeException {
        //Given - podajemy dane po pobrania pieniędzy
        String cardSource = "ae2e423e2d32";
        BigDecimal amount = new BigDecimal(100);
        Currency currency = Currency.PLN;
        String description = "Donation";

        PaymentIntentCreateParams testPaymentIntentCreateParams =
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


        PaymentIntent intent = new PaymentIntent();
        BDDMockito.when(stripeApi.create(
                                BDDMockito.any(PaymentIntentCreateParams.class),
                                BDDMockito.any(RequestOptions.class)))
                                    .thenReturn(intent);
        intent.setStatus("succeeded");


        //When - wywołanie serwisu
        CardPaymentCharge cardPaymentCharge = stripeService.chargeCard(cardSource, amount, currency, description);
        //Then - wywołanie             PaymentIntent paymentIntent = stripeApi.create(params,option);
        // tak więc jest to zewnętrzny serwis, do którego przekazujemy parametry -> Captor

        BDDMockito.then(stripeApi).should()
                .create(paymentIntentCreateParamsArgumentCaptor.capture(), requestOptionsArgumentCaptor.capture());
        //Sprawdzamy czy przekazene parametry zgadzają się z naszymit Given

        PaymentIntentCreateParams paramsArgumentCaptorValue = paymentIntentCreateParamsArgumentCaptor.getValue();
        RequestOptions requestOptionsArgumentCaptorValue = requestOptionsArgumentCaptor.getValue();

        Assertions.assertThat(paramsArgumentCaptorValue).usingRecursiveComparison().isEqualTo(testPaymentIntentCreateParams);
        Assertions.assertThat(requestOptionsArgumentCaptorValue).isNotNull();
        Assertions.assertThat(cardPaymentCharge.isCardDebited()).isEqualTo(intent.getStatus().equals("succeeded"));
    }

    @Test
    void itShouldNotChargeWhenApiThrowsException() throws StripeException {
        //Given
        String cardSource = "ae2e423e2d32";
        BigDecimal amount = new BigDecimal(100);
        Currency currency = Currency.PLN;
        String description = "Donation";
//        Mockujemy wyrzucenie przez stripeApi wyjątku podczas tworzenia (poprawne argumenty)
        Mockito.doThrow(stripeException).when(stripeApi).create(Mockito.any(PaymentIntentCreateParams.class),Mockito.any(RequestOptions.class));

        //When,Then -> wywołanie wyrzuca
        Assertions.assertThatThrownBy(() -> stripeService.chargeCard(cardSource,amount,currency,description))
                .isInstanceOf(IllegalStateException.class)
                .hasRootCause(stripeException)
                .hasMessageContaining("Cannot make stripe charge");

        //When
        //Then





        //When

        //Then
    }
}