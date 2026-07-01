package org.phalkun.service;

import org.phalkun.model.Order;
import org.phalkun.model.OrderItem;
import org.phalkun.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    private final OrderService orderService;
    private final ProductService productService;

    public ReportService(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    public record RevenueStats(long orderCount, BigDecimal totalRevenue) {}

    /**
     * Total revenue and number of orders in a period.
     */
    public RevenueStats getRevenueAndOrderCountInPeriod(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderService.getAllOrders();

        List<Order> filteredOrders = orders.stream()
                .filter(o -> !o.getOrderDate().isBefore(start) && !o.getOrderDate().isAfter(end))
                .toList();

        long count = filteredOrders.size();
        
        BigDecimal revenue = filteredOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RevenueStats(count, revenue);
    }

    /**
     * Top-N best-selling products.
     */
    public List<Map.Entry<Product, Integer>> getTopNBestSellingProducts(int n) {
        return orderService.getAllOrders().stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getProduct, Collectors.summingInt(OrderItem::getQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(n)
                .toList();
    }

    /**
     * Revenue by category.
     */
    public Map<String, BigDecimal> getRevenueByCategory() {
        return orderService.getAllOrders().stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getCategory(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                                BigDecimal::add
                        )
                ));
    }

    /**
     * Low-stock products needing reorder.
     */
    public List<Product> getLowStockProducts(int threshold) {
        return productService.getAllProducts().stream()
                .filter(p -> p.getStockQuantity() <= threshold)
                .sorted(Comparator.comparingInt(Product::getStockQuantity))
                .toList();
    }
}
