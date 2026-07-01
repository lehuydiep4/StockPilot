package org.phalkun.ui;

import org.phalkun.model.Product;
import org.phalkun.service.ReportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ReportConsoleUI {
    private static final String ERROR_PREFIX = "[Error] ";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ReportService reportService;
    private final Scanner scanner;

    public ReportConsoleUI(ReportService reportService, Scanner scanner) {
        this.reportService = reportService;
        this.scanner = scanner;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n==========================================");
            System.out.println("       SALES REPORTS & ANALYTICS          ");
            System.out.println("==========================================");
            System.out.println("1. Total Revenue & Orders in a Period");
            System.out.println("2. Top-N Best-Selling Products");
            System.out.println("3. Revenue by Category");
            System.out.println("4. Low-Stock Products Report");
            System.out.println("0. Back to Main Menu");
            System.out.print("Please enter your choice: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    handleRevenueInPeriod();
                    break;
                case "2":
                    handleTopNProducts();
                    break;
                case "3":
                    handleRevenueByCategory();
                    break;
                case "4":
                    handleLowStockProducts();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(ERROR_PREFIX + "Invalid option. Please choose between 0 and 4.");
            }
        }
    }

    private void handleRevenueInPeriod() {
        try {
            System.out.print("Enter Start Date (yyyy-MM-dd): ");
            LocalDate startDate = LocalDate.parse(scanner.nextLine().trim(), DATE_FORMATTER);
            System.out.print("Enter End Date (yyyy-MM-dd): ");
            LocalDate endDate = LocalDate.parse(scanner.nextLine().trim(), DATE_FORMATTER);

            if (startDate.isAfter(endDate)) {
                System.out.println(ERROR_PREFIX + "Start date cannot be after end date.");
                return;
            }

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);

            ReportService.RevenueStats stats = reportService.getRevenueAndOrderCountInPeriod(start, end);

            System.out.println("\n--- Revenue & Orders Report ---");
            System.out.println("Period: " + startDate + " to " + endDate);
            System.out.println("Total Orders: " + stats.orderCount());
            System.out.printf("Total Revenue: $%.2f%n", stats.totalRevenue());
            System.out.println("-------------------------------");

        } catch (DateTimeParseException e) {
            System.out.println(ERROR_PREFIX + "Invalid date format. Please use yyyy-MM-dd.");
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to generate report: " + e.getMessage());
        }
    }

    private void handleTopNProducts() {
        try {
            System.out.print("Enter the number of top products to display (N): ");
            int n = Integer.parseInt(scanner.nextLine().trim());
            if (n <= 0) {
                System.out.println(ERROR_PREFIX + "N must be greater than 0.");
                return;
            }

            List<Map.Entry<Product, Integer>> topProducts = reportService.getTopNBestSellingProducts(n);

            System.out.println("\n--- Top " + n + " Best-Selling Products ---");
            if (topProducts.isEmpty()) {
                System.out.println("No sales data available.");
            } else {
                System.out.printf("%-10s | %-25s | %-10s%n", "SKU", "PRODUCT NAME", "QTY SOLD");
                System.out.println("----------------------------------------------------");
                for (Map.Entry<Product, Integer> entry : topProducts) {
                    Product p = entry.getKey();
                    System.out.printf("%-10s | %-25s | %-10d%n", p.getSku(), truncate(p.getName(), 25), entry.getValue());
                }
            }
            System.out.println("----------------------------------------------------");

        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid number format.");
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to generate report: " + e.getMessage());
        }
    }

    private void handleRevenueByCategory() {
        try {
            Map<String, BigDecimal> revenueByCategory = reportService.getRevenueByCategory();

            System.out.println("\n--- Revenue by Category ---");
            if (revenueByCategory.isEmpty()) {
                System.out.println("No sales data available.");
            } else {
                System.out.printf("%-20s | %-15s%n", "CATEGORY", "REVENUE");
                System.out.println("--------------------------------------");
                revenueByCategory.entrySet().stream()
                        .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                        .forEach(entry -> System.out.printf("%-20s | $%-14.2f%n", truncate(entry.getKey(), 20), entry.getValue()));
            }
            System.out.println("--------------------------------------");

        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to generate report: " + e.getMessage());
        }
    }

    private void handleLowStockProducts() {
        try {
            System.out.print("Enter low stock threshold (e.g. 10): ");
            int threshold = Integer.parseInt(scanner.nextLine().trim());
            if (threshold < 0) {
                System.out.println(ERROR_PREFIX + "Threshold cannot be negative.");
                return;
            }

            List<Product> lowStockProducts = reportService.getLowStockProducts(threshold);

            System.out.println("\n--- Low-Stock Products Report (Threshold: " + threshold + ") ---");
            if (lowStockProducts.isEmpty()) {
                System.out.println("No products are below the stock threshold.");
            } else {
                System.out.printf("%-10s | %-25s | %-10s%n", "SKU", "PRODUCT NAME", "STOCK QTY");
                System.out.println("----------------------------------------------------");
                for (Product p : lowStockProducts) {
                    System.out.printf("%-10s | %-25s | %-10d%n", p.getSku(), truncate(p.getName(), 25), p.getStockQuantity());
                }
            }
            System.out.println("----------------------------------------------------");

        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid number format.");
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to generate report: " + e.getMessage());
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
