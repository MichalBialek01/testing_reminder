package com.example.testing_reminder.payment;

import com.example.testing_reminder.customer.Customer;
import com.example.testing_reminder.customer.CustomerRegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Test
    void itShouldCreatePaymentSuccessfully() throws Exception {
        //Given - customerRegistrationRequest
        UUID customerId = UUID.randomUUID();
        CustomerRegistrationRequest registrationRequest =
                new CustomerRegistrationRequest(new Customer(customerId, "Michal", "Bialek"));
        //When
        ResultActions customerAction = mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(registrationRequest))));
        //Then
        long paymentId = 1L;

        Payment payment = new Payment(
                paymentId,
                customerId,
                new BigDecimal("100.00"),
                Currency.PLN,
                "xadadadw21313wd2",
                "Donation"
        );


        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // ... When payment is sent
        ResultActions paymentResultActions = mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(paymentRequest))));

        // Then both customer registration and payment requests are 200 status code
        customerAction.andExpect(status().isOk());
        paymentResultActions.andExpect(status().isOk());
    }
}
