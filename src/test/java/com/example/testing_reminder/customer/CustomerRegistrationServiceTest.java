package com.example.testing_reminder.customer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@Description("""
        Test cases:
        1.Positive case - registerNewCustomer(), gdzie customer ma wszystkie pola
        2.Positive case - registerNewCustomer(), gdzie customerId jest null
        3.Negative case - registerNewCustomer() - nie zapisuje użytkownika jeżeli w DB jest użytkownik bazując na nuemrze tel.
        4.Negative case - registerNewCustomer() - wyrzuca wyjątek, jeżeli jest custommer o tym samym imieniu, numerze, 
        """)
@ExtendWith(MockitoExtension.class)
class CustomerRegistrationServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private CustomerRegistrationService customerRegistrationService;

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;


    @Test
    void itShouldRegisterNewCustomer() {
        //Given - customer, oraz custmerRequest
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Michal", "666666666");
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);
        //When - metoda registerNewCustomer
        customerRegistrationService.registerNewCustomer(request);
        //Then Sprawdzamy, czy:
        //1. Przechwytujemy to co jest przekazywane do metody save w repozytorium (pręzdej tworząc customerArgumentCaptor)
        //i sprawdzamy, czy jest on taki sam jak podany customer
        BDDMockito.then(customerRepository).should().save(customerArgumentCaptor.capture());
        Assertions.assertThat(customerArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(customer);
    }

    @Test
    void itShouldRegisterNewCustomerWhereCustomerIdIsNull() {
        //Given - customer, oraz custmerRequest
        Customer customer = new Customer(null, "Michal", "666666666");
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);
        //When - metoda registerNewCustomer
        customerRegistrationService.registerNewCustomer(request);
        //Then Sprawdzamy, czy:
        //1. Czy wszystkie pola (bez id) przechwycone z sygnatury metody save są zgodne z customer podanym
//      //2. Sprawdzenie czy sama wartość id nie jest null (w serwisie jest dodawane UUID)
        BDDMockito.then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerCaptorValue = customerArgumentCaptor.getValue();
        Assertions.assertThat(customerCaptorValue).usingRecursiveComparison().isEqualTo(customer);
        Assertions.assertThat(customerCaptorValue.getId()).isNotNull();
    }

    @Test
    void itShouldNotSaveCustomerWhenCustomerExists() {
        //Given - customer + stubb customer z repozytorium
        Customer customer = new Customer(null, "Michal", "666666666");
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);
        when(customerRepository.selectCustomerByPhoneNumber(customer.getPhoneNumber())).thenReturn(Optional.of(customer));
        //When - wywołuejmy metode
        customerRegistrationService.registerNewCustomer(request);
        //Then - wchodzi w case, gdzie nic nie zwraca. Więc sprawdzamy, czy repo nie zapisuje
        BDDMockito.then(customerRepository)
                .should(Mockito.never()).
                save(Mockito.any(Customer.class));
    }
    @Test
    void itShouldThrowWhenPhoneNumberIsTaken() {
        //Given - 2x customer, gdzie mają takie same nr. telefonu, ale inne imiona
        String phoneNumber = "666666666";
        Customer customer = new Customer(UUID.randomUUID(), "Adrian", phoneNumber);
        Customer customerOther = new Customer(UUID.randomUUID(), "Elon", phoneNumber);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);
        when(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).thenReturn(Optional.of(customerOther));
        assertThatThrownBy(() -> customerRegistrationService.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("phone number [%s] is taken", phoneNumber));

        // Finally
        then(customerRepository).should(never()).save(any(Customer.class));

    }



}