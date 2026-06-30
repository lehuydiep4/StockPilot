package org.phalkun.ui;

import java.util.Scanner;

public class MainMenuUI {
    private final ProductConsoleUI productConsoleUI;
    private final CustomerConsoleUI customerConsoleUI;
    private final Scanner scanner;

    public MainMenuUI(ProductConsoleUI productConsoleUI, CustomerConsoleUI customerConsoleUI, Scanner scanner) {
        this.productConsoleUI = productConsoleUI;
        this.customerConsoleUI = customerConsoleUI;
        this.scanner = scanner;
    }

    public void displayMenu() {
        System.out.println("==================================================");
        System.out.println("     WELCOME TO STOCKPILOT MANAGEMENT SYSTEM      ");
        System.out.println("==================================================");

        boolean exit = false;
        while (!exit) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Product & Inventory Management");
            System.out.println("2. Customer Management");
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
                case "0":
                    exit = true;
                    System.out.println("Thank you for using StockPilot! Goodbye.");
                    break;
                default:
                    System.out.println("[Error] Invalid option. Please select between 0 and 2.");
            }
        }
    }
}
