package com.example.testing_reminder.customer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Description;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

@DataJpaTest(
        properties = {
                "spring.jpa.properties.javax.persistence.validation.mode=none"
        }
)
@Description("""
        Test cases:
        1. Positive case - saving new customer
        2. Negative case - throwing exception while customer name is null
        3. Negative case - throwing exception while customer phoneNumber is null
        """)
class CustomerRepositoryTest {
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void itShouldSaveNewCustomer() {
        //Given - potrzebujemy customera do zapisania
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "Michal", "666666666");
        //When - zapisujemy customera
        customerRepository.save(customer);
        //Then - sprawdzamy:
        // 1.Czy customer o podanym id istnieje (isPresent)
        // 2.Czy pola zgadzają sie z podanym customerem (isEqualToComparingFieldByField)

        Optional<Customer> customerThatShouldExist = customerRepository.findById(customerId);
        Assertions.assertThat(customerThatShouldExist)
                .isPresent()
                .hasValueSatisfying(
                        foundCustomer -> Assertions.assertThat(foundCustomer).usingRecursiveComparison().isEqualTo(customer));
    }

    @Test
    void itShouldNotSaveCustomerWhenPhoneNumberIsNull() {
        //Given - customer bez phonNumber
        UUID id = UUID.randomUUID();
        //When and Then - assertThatThrownBy - sprawdza czy wywołana akcja wyrzuca wyjątek
        Customer customer = new Customer(id, "Adam", null);
        Assertions.assertThatThrownBy(() -> customerRepository.save(customer))
                .hasMessageContaining("not-null property references a null or transient value : pl.bialel.testing.customer.Customer.phoneNumber")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveCustomerWhenNameIsNull() {
        //Given - customer bez customerName
        UUID id = UUID.randomUUID();
        //When and Then - assertThatThrownBy - sprawdza czy wywołana akcja wyrzuca wyjątek
        Customer customer = new Customer(id, null, "666666777");
        Assertions.assertThatThrownBy(() -> customerRepository.save(customer))
                .hasMessageContaining("not-null property references a null or transient value : pl.bialel.testing.customer.Customer.customerName")
                .isInstanceOf(DataIntegrityViolationException.class);
    }


}