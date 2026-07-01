package org.phalkun.io;

import org.phalkun.model.Order;
import org.phalkun.model.OrderItem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.phalkun.exception.DataAccessException;

public class DocumentExportService {
    private static final String SEPARATOR_LINE = "========================================\n";

    public String exportInvoice(Order order, String outputDir) {
        try {
            Path dirPath = Paths.get(outputDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String fileName = "invoice_ORDER-" + order.getId() + ".txt";
            Path filePath = dirPath.resolve(fileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                writer.write(SEPARATOR_LINE);
                writer.write("                INVOICE\n");
                writer.write(SEPARATOR_LINE);
                writer.write("Order ID: " + order.getId() + "\n");
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String dateStr = order.getOrderDate() != null ? order.getOrderDate().format(formatter) : "N/A";
                writer.write("Date: " + dateStr + "\n");
                
                if (order.getCustomer() != null) {
                    writer.write("Customer: " + order.getCustomer().getName() + " (" + order.getCustomer().getEmail() + ")\n");
                }
                writer.write("\nItems:\n");
                
                BigDecimal subtotal = BigDecimal.ZERO;
                for (OrderItem item : order.getItems()) {
                    BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    subtotal = subtotal.add(itemTotal);
                    writer.write(String.format("- %dx %s (@ $%.2f each) = $%.2f%n",
                            item.getQuantity(),
                            item.getProduct().getName(),
                            item.getPrice(),
                            itemTotal));
                }

                writer.write("\n----------------------------------------\n");
                writer.write(String.format("Subtotal: $%.2f%n", subtotal));
                writer.write(String.format("Discount: $%.2f%n", order.getDiscountAmount()));
                writer.write(String.format("Total Amount: $%.2f%n", order.getTotalAmount()));
                writer.write(SEPARATOR_LINE);
            }

            return filePath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new DataAccessException("Failed to export invoice", e);
        }
    }

    public String exportSalesReport(List<Order> orders, String outputDir) {
        try {
            Path dirPath = Paths.get(outputDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String fileName = "sales_report_" + System.currentTimeMillis() + ".csv";
            Path filePath = dirPath.resolve(fileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                // Header
                writer.write("OrderID,Date,Customer,TotalAmount,Discount\n");
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (Order order : orders) {
                    String dateStr = order.getOrderDate() != null ? order.getOrderDate().format(formatter) : "N/A";
                    String customerName = order.getCustomer() != null ? order.getCustomer().getName() : "Unknown";
                    // Escape commas in names just in case
                    if (customerName.contains(",")) {
                        customerName = "\"" + customerName + "\"";
                    }
                    
                    writer.write(String.format("%d,%s,%s,%.2f,%.2f%n",
                            order.getId(),
                            dateStr,
                            customerName,
                            order.getTotalAmount(),
                            order.getDiscountAmount()));
                }
            }
            return filePath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new DataAccessException("Failed to export sales report", e);
        }
    }
}
