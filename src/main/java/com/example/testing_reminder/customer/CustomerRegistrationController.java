package com.example.testing_reminder.customer;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/customer-registration")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CustomerRegistrationController {

    private final CustomerRegistrationService customerRegistrationService;
    @PutMapping
    public void registerNewCustomer(
            @RequestBody CustomerRegistrationRequest request) {
        customerRegistrationService.registerNewCustomer(request);
    }

}
