package org.phalkun.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phalkun.model.Customer;
import org.phalkun.model.Order;
import org.phalkun.model.OrderItem;
import org.phalkun.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportServiceTest {
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        OrderService mockOrderService = new OrderService(null, null, null) {
            @Override
            public List<Order> getAllOrders() {
                return getStubbedOrders();
            }
        };

        ProductService mockProductService = new ProductService(null) {
            @Override
            public List<Product> getAllProducts() {
                return getStubbedProducts();
            }
        };

        reportService = new ReportService(mockOrderService, mockProductService);
    }

    private List<Product> getStubbedProducts() {
        return List.of(
            new Product(1L, "APP-0001", "Apple", "Fruit", new BigDecimal("1.0"), 5),
            new Product(2L, "BAN-0002", "Banana", "Fruit", new BigDecimal("0.5"), 20),
            new Product(3L, "MIL-0003", "Milk", "Dairy", new BigDecimal("2.0"), 2)
        );
    }

    private List<Order> getStubbedOrders() {
        List<Product> products = getStubbedProducts();
        
        Order o1 = new Order(1L, new Customer(), LocalDateTime.now().minusDays(1), new ArrayList<>(), new BigDecimal("6.0"), BigDecimal.ZERO);
        o1.addItem(new OrderItem(1L, 1L, products.get(0), 3, new BigDecimal("1.0"))); // Apple x 3 = $3.0
        o1.addItem(new OrderItem(2L, 1L, products.get(1), 6, new BigDecimal("0.5"))); // Banana x 6 = $3.0
        o1.setTotalAmount(new BigDecimal("6.0"));

        Order o2 = new Order(2L, new Customer(), LocalDateTime.now().minusDays(3), new ArrayList<>(), new BigDecimal("4.0"), BigDecimal.ZERO);
        o2.addItem(new OrderItem(3L, 2L, products.get(2), 2, new BigDecimal("2.0"))); // Milk x 2 = $4.0
        o2.setTotalAmount(new BigDecimal("4.0"));
        
        return List.of(o1, o2);
    }

    @Test
    void testGetRevenueAndOrderCountInPeriod() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now();

        // o1 should be included (minus 1 day), o2 should be excluded (minus 3 days)
        ReportService.RevenueStats stats = reportService.getRevenueAndOrderCountInPeriod(start, end);

        assertEquals(1, stats.orderCount());
        assertEquals(new BigDecimal("6.0"), stats.totalRevenue());
    }

    @Test
    void testGetTopNBestSellingProducts() {
        List<Map.Entry<Product, Integer>> top = reportService.getTopNBestSellingProducts(2);
        
        assertEquals(2, top.size());
        assertEquals("Banana", top.get(0).getKey().getName()); // 6 sold
        assertEquals(6, top.get(0).getValue());
        assertEquals("Apple", top.get(1).getKey().getName()); // 3 sold
    }

    @Test
    void testGetRevenueByCategory() {
        Map<String, BigDecimal> revenueByCategory = reportService.getRevenueByCategory();
        
        assertEquals(2, revenueByCategory.size());
        // Note: double check BigDecimal scaling
        assertEquals(0, new BigDecimal("6.0").compareTo(revenueByCategory.get("Fruit"))); 
        assertEquals(0, new BigDecimal("4.0").compareTo(revenueByCategory.get("Dairy"))); 
    }

    @Test
    void testGetLowStockProducts() {
        List<Product> lowStock = reportService.getLowStockProducts(10);
        
        assertEquals(2, lowStock.size());
        // Should be sorted by stock quantity ascending: Milk (2), Apple (5)
        assertEquals("Milk", lowStock.get(0).getName());
        assertEquals("Apple", lowStock.get(1).getName());
    }
}
