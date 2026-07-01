package org.phalkun.ui;

import org.phalkun.exception.DataAccessException;
import org.phalkun.io.DataImportService;
import org.phalkun.io.DocumentExportService;
import org.phalkun.model.Order;
import org.phalkun.service.OrderService;

import java.util.List;
import java.util.Scanner;

public class DataIOConsoleUI {
    private static final String ERROR_PREFIX = "[Error] ";
    
    private final DataImportService dataImportService;
    private final DocumentExportService documentExportService;
    private final OrderService orderService;
    private final Scanner scanner;

    public DataIOConsoleUI(DataImportService dataImportService, 
                           DocumentExportService documentExportService, 
                           OrderService orderService, 
                           Scanner scanner) {
        this.dataImportService = dataImportService;
        this.documentExportService = documentExportService;
        this.orderService = orderService;
        this.scanner = scanner;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n==========================================");
            System.out.println("   DATA IMPORT & DOCUMENT EXPORT MENU");
            System.out.println("==========================================");
            System.out.println("1. Import Product Catalog from CSV");
            System.out.println("2. Export Order Invoice (Text)");
            System.out.println("3. Export General Sales Report (CSV)");
            System.out.println("0. Back to Main Menu");
            System.out.print("Please enter your choice: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    handleImportCsv();
                    break;
                case "2":
                    handleExportInvoice();
                    break;
                case "3":
                    handleExportSalesReport();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("[Error] Invalid option. Please choose between 0 and 3.");
            }
        }
    }

    private void handleImportCsv() {
        System.out.print("Enter path to products CSV file (e.g. products.csv): ");
        String path = scanner.nextLine().trim();
        System.out.println("Starting import from: " + path);
        try {
            org.phalkun.dto.DataImportResult result = dataImportService.importProductsFromCsv(path);
            System.out.println("[Success] Imported " + result.getSuccessfulImports() + " products successfully.");
            if (!result.getErrors().isEmpty()) {
                System.out.println("Encountered the following errors during import:");
                for (String error : result.getErrors()) {
                    System.out.println(ERROR_PREFIX + error);
                }
            }
        } catch (DataAccessException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        }
    }

    private void handleExportInvoice() {
        try {
            System.out.print("Enter Order ID to export invoice: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Order order = orderService.getOrderById(id);
            
            System.out.print("Enter output directory (default: output): ");
            String outputDir = scanner.nextLine().trim();
            if (outputDir.isEmpty()) {
                outputDir = "output";
            }

            String filePath = documentExportService.exportInvoice(order, outputDir);
            System.out.println("[Success] Invoice exported to: " + filePath);
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid Order ID format. ID must be a number.");
        } catch (DataAccessException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "An error occurred while exporting invoice: " + e.getMessage());
        }
    }

    private void handleExportSalesReport() {
        try {
            List<Order> orders = orderService.getAllOrders();
            if (orders.isEmpty()) {
                System.out.println("[Info] No orders available to export.");
                return;
            }

            System.out.print("Enter output directory (default: output): ");
            String outputDir = scanner.nextLine().trim();
            if (outputDir.isEmpty()) {
                outputDir = "output";
            }

            String filePath = documentExportService.exportSalesReport(orders, outputDir);
            System.out.println("[Success] Sales report exported to: " + filePath);
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "An error occurred while exporting sales report: " + e.getMessage());
        }
    }
}
