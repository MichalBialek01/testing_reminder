package com.example.testing_reminder.payment;

import com.example.testing_reminder.customer.Customer;
import com.example.testing_reminder.customer.CustomerRepository;
import com.example.testing_reminder.payment.charger.CardPaymentCharge;
import com.example.testing_reminder.payment.charger.CardPaymentCharger;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@Description("""
        Test cases:
        1. Positive case - chargeCard() successful
        2. 
        """)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CardPaymentCharger cardPaymentCharger;
    @InjectMocks
    private PaymentService paymentService;
    @Captor
    private ArgumentCaptor<Payment> paymentArgumentCaptor;

    @Test
    void itShouldChargeCard() {
        //Given - customerId, paymentRequest
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(null, customerId, BigDecimal.valueOf(1000), Currency.PLN, "Biedronka", "SHOP_1234_RECEIPT_123456543");
        PaymentRequest paymentRequest = new PaymentRequest(payment);
        // Stubbujemy customerRepo i cardPaymentCharger żeby zwracał z wszystkimi polami i isCharget = true

        Mockito.when(customerRepository.
                findById(paymentRequest.getPayment().getCustomerId()))
                .thenReturn(Optional.of(Mockito.mock(Customer.class))); //Zamiast tworzyć customer no args, można po prostu zrobić Mockito.mock(class_name.class)
        Mockito.when(cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription()
        )).thenReturn(new CardPaymentCharge(true));


        //When
        paymentService.chargeCard(customerId,paymentRequest);



        //Then - wydarzy się to:
//        paymentRequest.getPayment().setCustomerId(customerId);
//        paymentRepository.save(paymentRequest.getPayment());
//        Czyli 1. musimy się upewnić, że paymentRequest posiada dobre customerId
//        Pobieramy argument przekazany do paymentRepositoy - capture
//        oraz 2. payment repozitory zapisało płatność. to jest jedyna interakcja i obiekt jest poprawny

        BDDMockito.then(paymentRepository).should().save(paymentArgumentCaptor.capture());

        Payment savedPayment = paymentArgumentCaptor.getValue();

        Assertions.assertThat(savedPayment.getCustomerId()).isEqualTo(customerId);

        Assertions.assertThat(savedPayment)
                .usingRecursiveComparison()
                .ignoringFields("customerId")
                .isEqualTo(paymentRequest.getPayment());
    }

    @Test
    void itShouldThrowCardIsNotDebited() {
        //Given
        //Given - customerId, paymentRequest
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(null, customerId, BigDecimal.valueOf(1000), Currency.PLN, "Biedronka", "SHOP_1234_RECEIPT_123456543");
        PaymentRequest paymentRequest = new PaymentRequest(payment);
        // Stubbujemy customerRepo i cardPaymentCharger żeby zwracał z wszystkimi polami i isCharget = true

        Mockito.when(customerRepository.
                        findById(paymentRequest.getPayment().getCustomerId()))
                .thenReturn(Optional.of(Mockito.mock(Customer.class))); //Zamiast tworzyć customer no args, można po prostu zrobić Mockito.mock(class_name.class)
        Mockito.when(cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription()
        )).thenReturn(new CardPaymentCharge(false));

        Assertions.assertThatThrownBy(() -> paymentService.chargeCard(customerId,paymentRequest))
                .hasMessageContaining("Card not debited for customer %s".formatted(customerId))
                .isInstanceOf(IllegalStateException.class);

        BDDMockito.then(paymentRepository).should(Mockito.never()).save(Mockito.any(Payment.class));
    }

    @Test
    void itShouldThrowWhenCurrencyIsNotSupported() {
        //Given
        //Given - customerId, paymentRequest
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(null, customerId, BigDecimal.valueOf(1000), null, "Biedronka", "SHOP_1234_RECEIPT_123456543");
        PaymentRequest paymentRequest = new PaymentRequest(payment);
        // Stubbujemy customerRepo
        Mockito.when(customerRepository.
                        findById(paymentRequest.getPayment().getCustomerId()))
                .thenReturn(Optional.of(Mockito.mock(Customer.class))); //Zamiast tworzyć customer no args, można po prostu zrobić Mockito.mock(class_name.class)
        Assertions.assertThatThrownBy(() -> paymentService.chargeCard(customerId,paymentRequest))
                .hasMessageContaining("Provided currency: [%s] is invalid".formatted(paymentRequest.getPayment().getCurrency()))
                .isInstanceOf(IllegalArgumentException.class);
        BDDMockito.then(cardPaymentCharger).shouldHaveNoInteractions();
        BDDMockito.then(paymentRepository).should(Mockito.never()).save(Mockito.any(Payment.class));
//      lub BDDMockito.then(paymentRepository).shouldHaveNoInteractions();



        //When
        //Then
    }
        @Test
    void itShouldThrowWhenCustomerDoesNotExist() {
        //Given
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(null, customerId, BigDecimal.valueOf(1000), Currency.PLN, "Biedronka", "SHOP_1234_RECEIPT_123456543");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        Mockito.when(customerRepository.findById(paymentRequest.getPayment().getCustomerId())).thenReturn(Optional.empty());
        //When
            Assertions.assertThatThrownBy(() -> paymentService.chargeCard(customerId,paymentRequest))
                    .hasMessageContaining("Provided customer with id: [%s] does not exist".formatted(customerId))
                    .isInstanceOf(EntityNotFoundException.class);


        //Then
    }
}