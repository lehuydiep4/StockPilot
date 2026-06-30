package org.phalkun.repository;

import org.phalkun.exception.DataAccessException;
import org.phalkun.model.Product;
import org.phalkun.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository implements Repository<Product, Long> {
    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);

    @Override
    public void save(Product product) {
        try (Connection conn = DbUtil.getConnection()) {
            save(conn, product);
        } catch (SQLException e) {
            logger.error("Error saving product: {}", product, e);
            throw new DataAccessException("Failed to save product", e);
        }
    }

    public void save(Connection conn, Product product) throws SQLException {
        String sql = "INSERT INTO products (sku, name, category, price, stock_quantity) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getSku());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getCategory());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStockQuantity());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        try (Connection conn = DbUtil.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Error finding product by ID: {}", id, e);
            throw new DataAccessException("Failed to find product by ID: " + id, e);
        }
    }

    public Optional<Product> findById(Connection conn, Long id) throws SQLException {
        String sql = "SELECT id, sku, name, category, price, stock_quantity FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Product> findBySku(String sku) {
        try (Connection conn = DbUtil.getConnection()) {
            return findBySku(conn, sku);
        } catch (SQLException e) {
            logger.error("Error finding product by SKU: {}", sku, e);
            throw new DataAccessException("Failed to find product by SKU: " + sku, e);
        }
    }

    public Optional<Product> findBySku(Connection conn, String sku) throws SQLException {
        String sql = "SELECT id, sku, name, category, price, stock_quantity FROM products WHERE sku = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        try (Connection conn = DbUtil.getConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            logger.error("Error fetching all products", e);
            throw new DataAccessException("Failed to fetch all products", e);
        }
    }

    public List<Product> findAll(Connection conn) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, sku, name, category, price, stock_quantity FROM products ORDER BY id";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public void update(Product product) {
        try (Connection conn = DbUtil.getConnection()) {
            update(conn, product);
        } catch (SQLException e) {
            logger.error("Error updating product: {}", product, e);
            throw new DataAccessException("Failed to update product", e);
        }
    }

    public void update(Connection conn, Product product) throws SQLException {
        String sql = "UPDATE products SET sku = ?, name = ?, category = ?, price = ?, stock_quantity = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getSku());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getCategory());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStockQuantity());
            stmt.setLong(6, product.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = DbUtil.getConnection()) {
            deleteById(conn, id);
        } catch (SQLException e) {
            logger.error("Error deleting product by ID: {}", id, e);
            throw new DataAccessException("Failed to delete product by ID: " + id, e);
        }
    }

    public void deleteById(Connection conn, Long id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public void updateStock(Long id, int newQuantity) {
        try (Connection conn = DbUtil.getConnection()) {
            updateStock(conn, id, newQuantity);
        } catch (SQLException e) {
            logger.error("Error updating stock for product ID: {}", id, e);
            throw new DataAccessException("Failed to update stock for product ID: " + id, e);
        }
    }

    public void updateStock(Connection conn, Long id, int newQuantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String sku = rs.getString("sku");
        String name = rs.getString("name");
        String category = rs.getString("category");
        BigDecimal price = rs.getBigDecimal("price");
        int stockQuantity = rs.getInt("stock_quantity");
        return new Product(id, sku, name, category, price, stockQuantity);
    }
}
