package com.example.testing_reminder.payment;

import com.example.testing_reminder.customer.Customer;
import com.example.testing_reminder.customer.CustomerRepository;
import com.example.testing_reminder.payment.charger.CardPaymentCharge;
import com.example.testing_reminder.payment.charger.CardPaymentCharger;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final CardPaymentCharger cardPaymentCharger;

    /***
     *
     * Jak działa podana metoda:
     *  1. Pobieramy customera
     *  2. Sprawdzamy, czy waluta paymentRequest jest poprawna. Jeżeli jest to:
     *      1.Z payment requestu tworzymy CardPaymentCharge (interfejs) tutaj powinna być zaimplementowana następnie relane połączenie z bramkną płatności, która by zmieniała  CardPaymentCharge.isCardDebited na true
     *      2.Jeżeli karta nie została obciążona (info z zewnętrznego systemu) to wyrzucamy wyjątek
     *      3.Jeżeli obciążono, łączymy paymentrequest z uzytkownikiem na podstawie id
     *      4.Zapisujemy płatność w bazie danych
 *          5.TODO - SEND SMS
     */
    void chargeCard(UUID customerId, PaymentRequest paymentRequest){
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new EntityNotFoundException("Provided customer with id: [%s] does not exist".formatted(customerId)));
        boolean isCurrencyValid = checkingThatCurrencyIsValid(paymentRequest);

        if (isCurrencyValid) {
            CardPaymentCharge cardPaymentCharge = chargeCard(paymentRequest);
            if(!cardPaymentCharge.isCardDebited()){
                throw new IllegalStateException(String.format("Card not debited for customer %s", customerId));
            }
            paymentRequest.getPayment().setCustomerId(customerId);
            paymentRepository.save(paymentRequest.getPayment());
        }
    }
    private CardPaymentCharge chargeCard(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        return cardPaymentCharger.chargeCard(
                payment.getSource(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getDescription()
        );
    }

    private static boolean checkingThatCurrencyIsValid(PaymentRequest paymentRequest) {
        Currency currencyCode = Optional.ofNullable(paymentRequest)
                .map(PaymentRequest::getPayment)
                .map(Payment::getCurrency)
                .orElseThrow(() -> new IllegalArgumentException("Currency is missing in the payment request"));

        boolean isValidCurrency = Arrays.stream(Currency.values())
                .anyMatch(validCurrency -> validCurrency.equals(currencyCode));
        if (!isValidCurrency) {
            throw new IllegalArgumentException("Provided currency: [%s] is invalid".formatted(currencyCode));
        }
        return isValidCurrency;
    }



}
