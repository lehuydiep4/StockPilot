package org.phalkun.concurrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phalkun.dto.FlashSaleResult;
import org.phalkun.exception.InsufficientStockException;
import org.phalkun.model.Order;
import org.phalkun.service.OrderService;
import org.phalkun.model.discount.DiscountPolicy;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlashSaleSimulatorTest {

    private FlashSaleSimulator simulator;
    private DummyOrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new DummyOrderService(5); // Simulate 5 items in stock
        simulator = new FlashSaleSimulator(orderService);
    }

    @Test
    void testSimulateFlashSale_HandlesConcurrency() {
        // Attempt 10 concurrent orders, each requesting 1 item, but we only have 5 in stock.
        FlashSaleResult result = simulator.simulateFlashSale(1L, "SKU_FLASH", 1, 10);

        // We expect exactly 5 to succeed and 5 to fail due to stock (or at least some ratio summing to 10)
        assertEquals(10, result.getSuccessfulOrders() + result.getFailedDueToStock() + result.getFailedDueToOther(), "Total attempts should equal 10");
        
        // Given our dummy service, 5 should succeed and 5 should fail due to InsufficientStockException
        assertEquals(5, result.getSuccessfulOrders(), "Exactly 5 orders should succeed");
        assertEquals(5, result.getFailedDueToStock(), "Exactly 5 orders should fail due to stock limit");
        assertEquals(0, result.getFailedDueToOther(), "No other errors should occur");
    }

    // A thread-safe dummy OrderService
    private static class DummyOrderService extends OrderService {
        private final AtomicInteger stock;

        public DummyOrderService(int initialStock) {
            super(null, null, null); // We don't need repositories for this dummy test
            this.stock = new AtomicInteger(initialStock);
        }

        @Override
        public Order placeOrder(Long customerId, Map<String, Integer> cart, DiscountPolicy discountPolicy) {
            // we assume cart only has 1 sku for flash sale
            int quantity = cart.values().iterator().next();
            // Synchronized block to simulate database row lock or thread safety
            synchronized (this) {
                if (stock.get() >= quantity) {
                    stock.addAndGet(-quantity);
                    return new Order(); // Dummy order
                } else {
                    throw new InsufficientStockException("Not enough stock");
                }
            }
        }
    }
}
