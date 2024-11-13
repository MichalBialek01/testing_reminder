package com.example.testing_reminder.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@EqualsAndHashCode(of = {"paymentId",
        "customerId",
        "amount",
        "currency",
        "source",
        "description"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    //Thre should be relationship one-to-many between customer and Payments
    private UUID customerId;
    private BigDecimal amount;
    private Currency currency;
    private String source;
    private String description;

}
