package org.phalkun.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phalkun.exception.CustomerNotFoundException;
import org.phalkun.exception.InsufficientStockException;
import org.phalkun.exception.ProductNotFoundException;
import org.phalkun.model.*;
import org.phalkun.repository.CustomerRepository;
import org.phalkun.repository.OrderRepository;
import org.phalkun.repository.ProductRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private ProductRepository productRepository;
    private CustomerRepository customerRepository;
    private OrderRepository orderRepository;
    private OrderService orderService;

    private List<Product> productDb;
    private List<Customer> customerDb;
    private List<Order> orderDb;

    @BeforeEach
    void setUp() {
        productDb = new ArrayList<>();
        customerDb = new ArrayList<>();
        orderDb = new ArrayList<>();

        // 1. Mock Product Repository
        productRepository = new ProductRepository() {
            private long sequence = 1L;

            @Override
            public void save(Product p) {
                p.setId(sequence++);
                productDb.add(p);
            }

            @Override
            public Optional<Product> findById(Long id) {
                return productDb.stream().filter(p -> p.getId().equals(id)).findFirst();
            }

            @Override
            public Optional<Product> findById(Connection conn, Long id) {
                return findById(id);
            }

            @Override
            public Optional<Product> findBySku(String sku) {
                return productDb.stream().filter(p -> p.getSku().equalsIgnoreCase(sku)).findFirst();
            }

            @Override
            public Optional<Product> findBySku(Connection conn, String sku) {
                return findBySku(sku);
            }

            @Override
            public void updateStock(Long id, int qty) {
                findById(id).ifPresent(p -> p.setStockQuantity(qty));
            }

            @Override
            public void updateStock(Connection conn, Long id, int qty) {
                updateStock(id, qty);
            }
        };

        // 2. Mock Customer Repository
        customerRepository = new CustomerRepository() {
            private long sequence = 1L;

            @Override
            public void save(Customer c) {
                c.setId(sequence++);
                customerDb.add(c);
            }

            @Override
            public Optional<Customer> findById(Long id) {
                return customerDb.stream().filter(c -> c.getId().equals(id)).findFirst();
            }

            @Override
            public Optional<Customer> findById(Connection conn, Long id) {
                return findById(id);
            }
        };

        // 3. Mock Order Repository
        orderRepository = new OrderRepository() {
            private long sequence = 1L;

            @Override
            public void save(Order o) {
                o.setId(sequence++);
                orderDb.add(o);
            }

            @Override
            public void save(Connection conn, Order o) {
                save(o);
            }

            @Override
            public List<Order> findAll() {
                return new ArrayList<>(orderDb);
            }

            @Override
            public List<Order> findAll(Connection conn) {
                return findAll();
            }
        };

        orderService = new OrderService(orderRepository, productRepository, customerRepository);
    }

    @Test
    void testPlaceOrder_Success() {
        Customer c = new Customer("John Doe", "john@example.com", "+84123456789");
        customerRepository.save(c);

        Product p = new Product("PRO-1001", "Keyboard", "Electronics", new BigDecimal("50.00"), 10);
        productRepository.save(p);

        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put("PRO-1001", 3);

        Order order = orderService.placeOrder(c.getId(), cart, new NoDiscount());

        assertNotNull(order.getId());
        assertEquals(c.getId(), order.getCustomer().getId());
        assertEquals(7, p.getStockQuantity()); // stock decremented
        assertEquals(new BigDecimal("150.00"), order.getTotalAmount());
        assertEquals(BigDecimal.ZERO, order.getDiscountAmount());
        assertEquals(1, order.getItems().size());
    }

    @Test
    void testPlaceOrder_InsufficientStock_TriggersRollback() {
        Customer c = new Customer("John Doe", "john@example.com", "+84123456789");
        customerRepository.save(c);

        Product p = new Product("PRO-1001", "Keyboard", "Electronics", new BigDecimal("50.00"), 5);
        productRepository.save(p);

        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put("PRO-1001", 6); // requests more than available

        DiscountPolicy discountPolicy = new NoDiscount();
        Long customerId = c.getId();
        assertThrows(InsufficientStockException.class, () ->
                orderService.placeOrder(customerId, cart, discountPolicy)
        );

        // Verify stock is not decremented (rollback behavior simulated)
        assertEquals(5, p.getStockQuantity());
        assertTrue(orderDb.isEmpty());
    }

    @Test
    void testPlaceOrder_CustomerNotFound() {
        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put("PRO-1001", 1);

        DiscountPolicy discountPolicy = new NoDiscount();
        assertThrows(CustomerNotFoundException.class, () ->
                orderService.placeOrder(99L, cart, discountPolicy)
        );
    }

    @Test
    void testPlaceOrder_ProductNotFound() {
        Customer c = new Customer("John Doe", "john@example.com", "+84123456789");
        customerRepository.save(c);

        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put("NON-EXISTENT-SKU", 1);

        DiscountPolicy discountPolicy = new NoDiscount();
        Long customerId = c.getId();
        assertThrows(ProductNotFoundException.class, () ->
                orderService.placeOrder(customerId, cart, discountPolicy)
        );
    }

    @Test
    void testPlaceOrder_PercentageDiscount() {
        Customer c = new Customer("John Doe", "john@example.com", "+84123456789");
        customerRepository.save(c);

        Product p = new Product("PRO-1001", "Keyboard", "Electronics", new BigDecimal("100.00"), 10);
        productRepository.save(p);

        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put("PRO-1001", 2); // 200.00 subtotal

        Order order = orderService.placeOrder(c.getId(), cart, new PercentageDiscount(new BigDecimal("0.10")));

        assertEquals(new BigDecimal("20.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("180.00"), order.getTotalAmount());
    }

    @Test
    void testPlaceOrder_BulkDiscount() {
        Customer c = new Customer("John Doe", "john@example.com", "+84123456789");
        customerRepository.save(c);

        Product p = new Product("PRO-1001", "Keyboard", "Electronics", new BigDecimal("10.00"), 20);
        productRepository.save(p);

        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put("PRO-1001", 10); // meeting threshold 5

        // Bulk discount: threshold 5, 20% discount (0.20)
        Order order = orderService.placeOrder(c.getId(), cart, new BulkDiscount(5, new BigDecimal("0.20")));

        assertEquals(new BigDecimal("20.00"), order.getDiscountAmount()); // 10 * 10 * 0.20 = 20.00
        assertEquals(new BigDecimal("80.00"), order.getTotalAmount());
    }
}
