package org.phalkun.concurrent;

import org.phalkun.exception.InsufficientStockException;
import org.phalkun.model.discount.NoDiscount;
import org.phalkun.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleSimulator {
    private static final Logger logger = LoggerFactory.getLogger(FlashSaleSimulator.class);

    private final OrderService orderService;

    public FlashSaleSimulator(OrderService orderService) {
        this.orderService = orderService;
    }

    public org.phalkun.dto.FlashSaleResult simulateFlashSale(Long customerId, String sku, int qtyPerOrder, int numConcurrentOrders) {
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(numConcurrentOrders, 50));
        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedDueToStock = new AtomicInteger(0);
        AtomicInteger failedDueToOther = new AtomicInteger(0);

        for (int i = 0; i < numConcurrentOrders; i++) {
            executor.submit(() -> {
                try {
                    Map<String, Integer> cart = new HashMap<>();
                    cart.put(sku, qtyPerOrder);
                    orderService.placeOrder(customerId, cart, new NoDiscount());
                    successfulOrders.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failedDueToStock.incrementAndGet();
                } catch (Exception e) {
                    failedDueToOther.incrementAndGet();
                    logger.error("Order failed during flash sale", e);
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return new org.phalkun.dto.FlashSaleResult(successfulOrders.get(), failedDueToStock.get(), failedDueToOther.get());
    }
}
