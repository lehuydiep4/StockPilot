package org.phalkun.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.phalkun.model.Customer;
import org.phalkun.model.Order;
import org.phalkun.model.OrderItem;
import org.phalkun.model.Product;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentExportServiceTest {

    private DocumentExportService exportService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exportService = new DocumentExportService();
    }

    @Test
    void testExportInvoice_CreatesFile() throws IOException {
        Customer customer = new Customer("John Doe", "john@test.com", "+84123456789");
        Order order = new Order(1L, customer, null, null, new BigDecimal("100.00"), BigDecimal.ZERO);
        Product product = new Product("ABC-0001", "Item 1", "Cat 1", new BigDecimal("50.00"), 10);
        order.getItems().add(new OrderItem(product, 2, new BigDecimal("50.00")));

        String resultPath = exportService.exportInvoice(order, tempDir.toString());

        Path exportedFilePath = Paths.get(resultPath);
        assertTrue(Files.exists(exportedFilePath), "Exported file should exist");
        
        String content = Files.readString(exportedFilePath);
        assertTrue(content.contains("INVOICE"));
        assertTrue(content.contains("John Doe"));
        assertTrue(content.contains("Item 1"));
    }

    @Test
    void testExportSalesReport_CreatesFile() throws IOException {
        Customer customer1 = new Customer("John Doe", "john@test.com", "+84123456789");
        Order order1 = new Order(1L, customer1, null, null, new BigDecimal("100.00"), BigDecimal.ZERO);
        
        Customer customer2 = new Customer("Jane Smith", "jane@test.com", "+84987654321");
        Order order2 = new Order(2L, customer2, null, null, new BigDecimal("200.00"), new BigDecimal("20.00"));

        List<Order> orders = Arrays.asList(order1, order2);

        String resultPath = exportService.exportSalesReport(orders, tempDir.toString());

        Path exportedFilePath = Paths.get(resultPath);
        assertTrue(Files.exists(exportedFilePath), "Exported CSV file should exist");

        String content = Files.readString(exportedFilePath);
        assertTrue(content.contains("OrderID,Date,Customer,TotalAmount,Discount"));
        assertTrue(content.contains("John Doe"));
        assertTrue(content.contains("Jane Smith"));
    }
}
