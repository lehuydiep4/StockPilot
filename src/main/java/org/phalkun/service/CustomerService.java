package org.phalkun.service;

import org.phalkun.exception.CustomerNotFoundException;
import org.phalkun.exception.InvalidInputException;
import org.phalkun.model.Customer;
import org.phalkun.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer registerCustomer(String name, String email, String phone) {
        if (customerRepository.findByEmail(email).isPresent()) {
            throw new InvalidInputException("Customer with email '" + email + "' already exists.");
        }
        Customer customer = new Customer(name, email, phone);
        customerRepository.save(customer);
        return customer;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
    }

    public Customer updateCustomer(Long id, String name, String email, String phone) {
        Customer existing = getCustomerById(id);

        if (!existing.getEmail().equalsIgnoreCase(email)) {
            Optional<Customer> withSameEmail = customerRepository.findByEmail(email);
            if (withSameEmail.isPresent() && !withSameEmail.get().getId().equals(id)) {
                throw new InvalidInputException("Customer with email '" + email + "' already exists.");
            }
        }

        existing.setName(name);
        existing.setEmail(email);
        existing.setPhone(phone);

        customerRepository.update(existing);
        return existing;
    }

    public void deleteCustomer(Long id) {
        getCustomerById(id); // Ensure exists
        customerRepository.deleteById(id);
    }
}
