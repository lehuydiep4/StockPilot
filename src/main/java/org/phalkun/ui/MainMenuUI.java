package org.phalkun.ui;

import org.phalkun.concurrent.FlashSaleSimulator;
import org.phalkun.concurrent.SalesAutoExporter;

import java.util.Scanner;

public class MainMenuUI {
    private final ProductConsoleUI productConsoleUI;
    private final CustomerConsoleUI customerConsoleUI;
    private final OrderConsoleUI orderConsoleUI;
    private final DataIOConsoleUI dataIOConsoleUI;
    private final FlashSaleSimulator flashSaleSimulator;
    private final SalesAutoExporter salesAutoExporter;
    private final ReportConsoleUI reportConsoleUI;
    private final Scanner scanner;

    public MainMenuUI(ProductConsoleUI productConsoleUI, 
                      CustomerConsoleUI customerConsoleUI, 
                      OrderConsoleUI orderConsoleUI, 
                      DataIOConsoleUI dataIOConsoleUI,
                      FlashSaleSimulator flashSaleSimulator,
                      SalesAutoExporter salesAutoExporter,
                      ReportConsoleUI reportConsoleUI,
                      Scanner scanner) {
        this.productConsoleUI = productConsoleUI;
        this.customerConsoleUI = customerConsoleUI;
        this.orderConsoleUI = orderConsoleUI;
        this.dataIOConsoleUI = dataIOConsoleUI;
        this.flashSaleSimulator = flashSaleSimulator;
        this.salesAutoExporter = salesAutoExporter;
        this.reportConsoleUI = reportConsoleUI;
        this.scanner = scanner;
    }

    public void displayMenu() {
        System.out.println("==================================================");
        System.out.println("     WELCOME TO STOCKPILOT MANAGEMENT SYSTEM      ");
        System.out.println("==================================================");

        // Start the background auto-exporter (e.g. every 5 minutes)
        salesAutoExporter.start(5, "output/");

        boolean exit = false;
        while (!exit) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Product & Inventory Management");
            System.out.println("2. Customer Management");
            System.out.println("3. Order Processing & Checkout");
            System.out.println("4. Data Import & Document Export");
            System.out.println("5. Simulate Flash Sale");
            System.out.println("6. Sales Reports & Analytics");
            System.out.println("0. Exit Application");
            System.out.print("Please select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    productConsoleUI.displayMenu();
                    break;
                case "2":
                    customerConsoleUI.displayMenu();
                    break;
                case "3":
                    orderConsoleUI.displayMenu();
                    break;
                case "4":
                    dataIOConsoleUI.displayMenu();
                    break;
                case "5":
                    handleFlashSale();
                    break;
                case "6":
                    reportConsoleUI.displayMenu();
                    break;
                case "0":
                    exit = true;
                    System.out.println("Shutting down background tasks...");
                    salesAutoExporter.shutdown();
                    System.out.println("Thank you for using StockPilot! Goodbye.");
                    break;
                default:
                    System.out.println("[Error] Invalid option. Please select between 0 and 6.");
            }
        }
    }

    private void handleFlashSale() {
        System.out.println("\n--- FLASH SALE SIMULATOR ---");
        try {
            System.out.print("Enter Customer ID for orders: ");
            Long customerId = Long.parseLong(scanner.nextLine().trim());

            System.out.print("Enter Product SKU to order: ");
            String sku = scanner.nextLine().trim();

            System.out.print("Enter Quantity per order: ");
            int qtyPerOrder = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Number of concurrent orders (threads): ");
            int numConcurrentOrders = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Starting Flash Sale Simulation...");
            System.out.printf("Product SKU: %s | Qty per order: %d | Concurrent orders: %d%n", sku, qtyPerOrder, numConcurrentOrders);
            
            org.phalkun.dto.FlashSaleResult result = flashSaleSimulator.simulateFlashSale(customerId, sku, qtyPerOrder, numConcurrentOrders);
            
            System.out.println("\n--- Flash Sale Results ---");
            System.out.println("Successful orders: " + result.getSuccessfulOrders());
            System.out.println("Failed (insufficient stock): " + result.getFailedDueToStock());
            System.out.println("Failed (other errors): " + result.getFailedDueToOther());
            System.out.println("------------------------------------");
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid number format.");
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }
}
