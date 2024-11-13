package com.example.testing_reminder.payment;

import org.assertj.core.api.Assertions;
import org.hibernate.annotations.AttributeAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
        properties = {
                "spring.jpa.properties.javax.persistence.validation.mode=none"
        }
)
class PaymentRepositoryTest {
        @Autowired
        private PaymentRepository paymentRepository;
        @Test
        void itShouldSavePayment() {
                //Given
                UUID customerId = UUID.randomUUID();
                Payment payment = new Payment(1L,
                        customerId,
                        BigDecimal.valueOf(500),
                        Currency.PLN,
                        "cart" + UUID.randomUUID(),
                        "For shopping");
                //When
                paymentRepository.save(payment);
                //Then
                Optional<Payment> savedPayment = paymentRepository.findById(1L);

                Assertions.assertThat(savedPayment).isPresent()
                        .hasValueSatisfying(
                                (pay ->Assertions.assertThat(pay).usingRecursiveComparison().isEqualTo(payment))
                        );



        }
}