package org.phalkun.model;

import org.phalkun.exception.InvalidInputException;
import java.util.Objects;
import java.util.regex.Pattern;

public class Customer {
    // Basic email validation regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    // Standard phone validation: optional '+' followed by 9 to 15 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d{9,15}$");

    private Long id;
    private String name;
    private String email;
    private String phone;

    public Customer() {}

    public Customer(Long id, String name, String email, String phone) {
        setId(id);
        setName(name);
        setEmail(email);
        setPhone(phone);
    }

    public Customer(String name, String email, String phone) {
        this(null, name, email, phone);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("Customer name cannot be empty.");
        }
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidInputException("Customer email cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidInputException("Customer email format is invalid.");
        }
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new InvalidInputException("Customer phone number cannot be empty.");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidInputException("Customer phone format is invalid (should be between 9 to 15 digits, e.g. +84123456789).");
        }
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) && Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
