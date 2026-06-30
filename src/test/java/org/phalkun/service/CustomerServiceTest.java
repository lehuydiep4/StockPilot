package org.phalkun.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phalkun.exception.CustomerNotFoundException;
import org.phalkun.exception.InvalidInputException;
import org.phalkun.model.Customer;
import org.phalkun.repository.CustomerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {

    private CustomerRepository mockRepository;
    private CustomerService customerService;
    private List<Customer> database;

    @BeforeEach
    void setUp() {
        database = new ArrayList<>();
        mockRepository = new CustomerRepository() {
            private long idSequence = 1L;

            @Override
            public void save(Customer customer) {
                customer.setId(idSequence++);
                database.add(customer);
            }

            @Override
            public Optional<Customer> findById(Long id) {
                return database.stream().filter(c -> c.getId().equals(id)).findFirst();
            }

            @Override
            public Optional<Customer> findByEmail(String email) {
                return database.stream().filter(c -> c.getEmail().equalsIgnoreCase(email)).findFirst();
            }

            @Override
            public List<Customer> findAll() {
                return new ArrayList<>(database);
            }

            @Override
            public void update(Customer customer) {
                deleteById(customer.getId());
                database.add(customer);
            }

            @Override
            public void deleteById(Long id) {
                database.removeIf(c -> c.getId().equals(id));
            }
        };

        customerService = new CustomerService(mockRepository);
    }

    @Test
    void testRegisterCustomer_Success() {
        Customer c = customerService.registerCustomer("Alice Smith", "alice@example.com", "+84123456789");
        assertNotNull(c.getId());
        assertEquals("Alice Smith", c.getName());
        assertEquals(1, customerService.getAllCustomers().size());
    }

    @Test
    void testRegisterCustomer_DuplicateEmailThrowsException() {
        customerService.registerCustomer("Alice Smith", "alice@example.com", "+84123456789");
        assertThrows(InvalidInputException.class, () ->
                customerService.registerCustomer("Bob Jones", "alice@example.com", "+84987654321")
        );
    }

    @Test
    void testGetCustomerById_NotFoundThrowsException() {
        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(99L));
    }
}
