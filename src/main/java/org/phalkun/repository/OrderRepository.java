package org.phalkun.repository;

import org.phalkun.exception.DataAccessException;
import org.phalkun.model.Customer;
import org.phalkun.model.Order;
import org.phalkun.model.OrderItem;
import org.phalkun.model.Product;
import org.phalkun.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository implements Repository<Order, Long> {
    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);

    @Override
    public void save(Order order) {
        try (Connection conn = DbUtil.getConnection()) {
            save(conn, order);
        } catch (SQLException e) {
            logger.error("Error saving order: {}", order, e);
            throw new DataAccessException("Failed to save order", e);
        }
    }

    public void save(Connection conn, Order order) throws SQLException {
        String insertOrderSql = "INSERT INTO orders (customer_id, order_date, total_amount, discount_amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, order.getCustomer().getId());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            stmt.setBigDecimal(3, order.getTotalAmount());
            stmt.setBigDecimal(4, order.getDiscountAmount());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    order.setId(rs.getLong(1));
                }
            }
        }

        String insertItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem item : order.getItems()) {
                stmt.setLong(1, order.getId());
                stmt.setLong(2, item.getProduct().getId());
                stmt.setInt(3, item.getQuantity());
                stmt.setBigDecimal(4, item.getPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                for (OrderItem item : order.getItems()) {
                    if (rs.next()) {
                        item.setId(rs.getLong(1));
                    }
                }
            }
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        try (Connection conn = DbUtil.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Error finding order by ID: {}", id, e);
            throw new DataAccessException("Failed to find order by ID: " + id, e);
        }
    }

    public Optional<Order> findById(Connection conn, Long id) throws SQLException {
        String sql = "SELECT o.id, o.order_date, o.total_amount, o.discount_amount, " +
                     "c.id AS customer_id, c.name AS customer_name, c.email AS customer_email, c.phone AS customer_phone " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.id " +
                     "WHERE o.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findItemsByOrderId(conn, order.getId()));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findAll() {
        try (Connection conn = DbUtil.getConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            logger.error("Error fetching all orders", e);
            throw new DataAccessException("Failed to fetch all orders", e);
        }
    }

    public List<Order> findAll(Connection conn) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.order_date, o.total_amount, o.discount_amount, " +
                     "c.id AS customer_id, c.name AS customer_name, c.email AS customer_email, c.phone AS customer_phone " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.id " +
                     "ORDER BY o.id";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                orders.add(order);
            }
        }
        // Fetch items for all orders to complete the objects
        for (Order order : orders) {
            order.setItems(findItemsByOrderId(conn, order.getId()));
        }
        return orders;
    }

    @Override
    public void update(Order order) {
        try (Connection conn = DbUtil.getConnection()) {
            update(conn, order);
        } catch (SQLException e) {
            logger.error("Error updating order: {}", order, e);
            throw new DataAccessException("Failed to update order", e);
        }
    }

    public void update(Connection conn, Order order) throws SQLException {
        String sql = "UPDATE orders SET customer_id = ?, order_date = ?, total_amount = ?, discount_amount = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, order.getCustomer().getId());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            stmt.setBigDecimal(3, order.getTotalAmount());
            stmt.setBigDecimal(4, order.getDiscountAmount());
            stmt.setLong(5, order.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = DbUtil.getConnection()) {
            deleteById(conn, id);
        } catch (SQLException e) {
            logger.error("Error deleting order by ID: {}", id, e);
            throw new DataAccessException("Failed to delete order by ID: " + id, e);
        }
    }

    public void deleteById(Connection conn, Long id) throws SQLException {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private List<OrderItem> findItemsByOrderId(Connection conn, Long orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.id, oi.quantity, oi.price, " +
                     "p.id AS product_id, p.sku AS product_sku, p.name AS product_name, " +
                     "p.category AS product_category, p.price AS product_price, p.stock_quantity AS product_stock_quantity " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE oi.order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long itemId = rs.getLong("id");
                    int quantity = rs.getInt("quantity");
                    BigDecimal purchasePrice = rs.getBigDecimal("price");

                    Long prodId = rs.getLong("product_id");
                    String sku = rs.getString("product_sku");
                    String name = rs.getString("product_name");
                    String category = rs.getString("product_category");
                    BigDecimal currentPrice = rs.getBigDecimal("product_price");
                    int stockQuantity = rs.getInt("product_stock_quantity");

                    Product product = new Product(prodId, sku, name, category, currentPrice, stockQuantity);
                    OrderItem item = new OrderItem(itemId, orderId, product, quantity, purchasePrice);
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        LocalDateTime orderDate = rs.getTimestamp("order_date").toLocalDateTime();
        BigDecimal totalAmount = rs.getBigDecimal("total_amount");
        BigDecimal discountAmount = rs.getBigDecimal("discount_amount");

        Long custId = rs.getLong("customer_id");
        String custName = rs.getString("customer_name");
        String custEmail = rs.getString("customer_email");
        String custPhone = rs.getString("customer_phone");
        Customer customer = new Customer(custId, custName, custEmail, custPhone);

        return new Order(id, customer, orderDate, new ArrayList<>(), totalAmount, discountAmount);
    }
}
