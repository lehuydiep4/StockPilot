package org.phalkun.service;

import org.phalkun.exception.CustomerNotFoundException;
import org.phalkun.exception.DataAccessException;
import org.phalkun.exception.InsufficientStockException;
import org.phalkun.exception.ProductNotFoundException;
import org.phalkun.model.Customer;
import org.phalkun.model.DiscountPolicy;
import org.phalkun.model.Order;
import org.phalkun.model.OrderItem;
import org.phalkun.model.Product;
import org.phalkun.repository.CustomerRepository;
import org.phalkun.repository.OrderRepository;
import org.phalkun.repository.ProductRepository;
import org.phalkun.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public Order placeOrder(Long customerId, Map<String, Integer> cart, DiscountPolicy discountPolicy) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty.");
        }

        try (Connection conn = DbUtil.getConnection()) {
            conn.setAutoCommit(false);
            return executeOrderTransaction(conn, customerId, cart, discountPolicy);
        } catch (SQLException e) {
            logger.error("Database connection/transaction failure during checkout", e);
            throw new DataAccessException("Order processing failed due to database error", e);
        }
    }

    private Order executeOrderTransaction(Connection conn, Long customerId, Map<String, Integer> cart, DiscountPolicy discountPolicy) throws SQLException {
        try {
            // 1. Fetch and validate Customer
            Customer customer = customerRepository.findById(conn, customerId)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

            Order order = new Order(customer);
            BigDecimal subtotal = BigDecimal.ZERO;

            // 2. Validate and adjust stock for each cart item
            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                String sku = entry.getKey();
                int qty = entry.getValue();

                Product product = productRepository.findBySku(conn, sku)
                        .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

                if (product.getStockQuantity() < qty) {
                    throw new InsufficientStockException("Insufficient stock for product SKU '" + sku + 
                            "'. Available: " + product.getStockQuantity() + ", Requested: " + qty);
                }

                // Decrement stock in DB
                int newStock = product.getStockQuantity() - qty;
                product.setStockQuantity(newStock);
                productRepository.updateStock(conn, product.getId(), newStock);

                // Create OrderItem
                OrderItem item = new OrderItem(product, qty, product.getPrice());
                order.addItem(item);

                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
                subtotal = subtotal.add(itemTotal);
            }

            // 3. Apply discount policy
            BigDecimal discount = discountPolicy.calculateDiscount(order);
            order.setDiscountAmount(discount);
            order.setTotalAmount(subtotal.subtract(discount));

            // 4. Save order and items
            orderRepository.save(conn, order);

            conn.commit();
            return order;
        } catch (Exception e) {
            conn.rollback();
            logger.error("Transaction rolled back due to error placing order for customer ID: {}", customerId, e);
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new DataAccessException("Order processing failed", e);
        }
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getAllOrders(Comparator<Order> comparator) {
        List<Order> orders = orderRepository.findAll();
        orders.sort(comparator);
        return orders;
    }
}
