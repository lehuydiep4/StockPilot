package org.phalkun.repository;

import org.phalkun.exception.DataAccessException;
import org.phalkun.model.Customer;
import org.phalkun.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository implements Repository<Customer, Long> {
    private static final Logger logger = LoggerFactory.getLogger(CustomerRepository.class);

    @Override
    public void save(Customer customer) {
        try (Connection conn = DbUtil.getConnection()) {
            save(conn, customer);
        } catch (SQLException e) {
            logger.error("Error saving customer: {}", customer, e);
            throw new DataAccessException("Failed to save customer", e);
        }
    }

    public void save(Connection conn, Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    customer.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        try (Connection conn = DbUtil.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Error finding customer by ID: {}", id, e);
            throw new DataAccessException("Failed to find customer by ID: " + id, e);
        }
    }

    public Optional<Customer> findById(Connection conn, Long id) throws SQLException {
        String sql = "SELECT id, name, email, phone FROM customers WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findByEmail(String email) {
        try (Connection conn = DbUtil.getConnection()) {
            return findByEmail(conn, email);
        } catch (SQLException e) {
            logger.error("Error finding customer by email: {}", email, e);
            throw new DataAccessException("Failed to find customer by email: " + email, e);
        }
    }

    public Optional<Customer> findByEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT id, name, email, phone FROM customers WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        try (Connection conn = DbUtil.getConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            logger.error("Error fetching all customers", e);
            throw new DataAccessException("Failed to fetch all customers", e);
        }
    }

    public List<Customer> findAll(Connection conn) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT id, name, email, phone FROM customers ORDER BY id";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    @Override
    public void update(Customer customer) {
        try (Connection conn = DbUtil.getConnection()) {
            update(conn, customer);
        } catch (SQLException e) {
            logger.error("Error updating customer: {}", customer, e);
            throw new DataAccessException("Failed to update customer", e);
        }
    }

    public void update(Connection conn, Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.setLong(4, customer.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = DbUtil.getConnection()) {
            deleteById(conn, id);
        } catch (SQLException e) {
            logger.error("Error deleting customer by ID: {}", id, e);
            throw new DataAccessException("Failed to delete customer by ID: " + id, e);
        }
    }

    public void deleteById(Connection conn, Long id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        return new Customer(id, name, email, phone);
    }
}
