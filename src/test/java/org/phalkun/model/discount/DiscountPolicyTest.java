package org.phalkun.model.discount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phalkun.model.Customer;
import org.phalkun.model.Order;
import org.phalkun.model.OrderItem;
import org.phalkun.model.Product;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscountPolicyTest {

    private Order order;
    private Product p1;
    private Product p2;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer("Test", "test@test.com", "+84123456789");
        order = new Order(customer);
        p1 = new Product("ABC-0001", "Prod1", "Cat1", new BigDecimal("100.00"), 10);
        p2 = new Product("ABC-0002", "Prod2", "Cat2", new BigDecimal("50.00"), 10);
    }

    @Test
    void testNoDiscount() {
        DiscountPolicy policy = new NoDiscount();
        order.getItems().add(new OrderItem(p1, 1, p1.getPrice()));
        
        BigDecimal discount = policy.calculateDiscount(order);
        assertEquals(0, BigDecimal.ZERO.compareTo(discount));
    }

    @Test
    void testPercentageDiscount() {
        DiscountPolicy policy = new PercentageDiscount(new BigDecimal("0.10"));
        
        // Total = 250 -> discount should be 25.00
        order.getItems().add(new OrderItem(p1, 2, p1.getPrice())); // 200
        order.getItems().add(new OrderItem(p2, 1, p2.getPrice())); // 50
        
        BigDecimal discount = policy.calculateDiscount(order);
        assertEquals(0, new BigDecimal("25.00").compareTo(discount));
    }

    @Test
    void testBulkDiscount_AppliesWhenItemQtyOverThreshold() {
        DiscountPolicy policy = new BulkDiscount(5, new BigDecimal("0.20")); // 20% off items with qty >= 5
        
        // p1: qty 5 -> price 500 -> discount 100
        // p2: qty 2 -> price 100 -> no discount
        order.getItems().add(new OrderItem(p1, 5, p1.getPrice()));
        order.getItems().add(new OrderItem(p2, 2, p2.getPrice()));
        
        BigDecimal discount = policy.calculateDiscount(order);
        assertEquals(0, new BigDecimal("100.00").compareTo(discount));
    }

    @Test
    void testBulkDiscount_DoesNotApplyWhenItemQtyUnderThreshold() {
        DiscountPolicy policy = new BulkDiscount(5, new BigDecimal("0.20"));
        
        // p1: qty 4 -> price 400 -> no discount
        order.getItems().add(new OrderItem(p1, 4, p1.getPrice()));
        
        BigDecimal discount = policy.calculateDiscount(order);
        assertEquals(0, BigDecimal.ZERO.compareTo(discount));
    }
}
