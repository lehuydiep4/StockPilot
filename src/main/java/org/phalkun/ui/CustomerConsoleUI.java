package org.phalkun.ui;

import org.phalkun.exception.CustomerNotFoundException;
import org.phalkun.exception.InvalidInputException;
import org.phalkun.model.Customer;
import org.phalkun.service.CustomerService;

import java.util.List;
import java.util.Scanner;

public class CustomerConsoleUI {
    private final CustomerService customerService;
    private final Scanner scanner;

    public CustomerConsoleUI(CustomerService customerService, Scanner scanner) {
        this.customerService = customerService;
        this.scanner = scanner;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n==========================================");
            System.out.println("       CUSTOMER MANAGEMENT MENU           ");
            System.out.println("==========================================");
            System.out.println("1. List All Customers");
            System.out.println("2. Find Customer by ID");
            System.out.println("3. Find Customer by Email");
            System.out.println("4. Register New Customer");
            System.out.println("5. Update Customer");
            System.out.println("6. Delete Customer");
            System.out.println("0. Back to Main Menu");
            System.out.print("Please enter your choice: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    handleListAllCustomers();
                    break;
                case "2":
                    handleFindCustomerById();
                    break;
                case "3":
                    handleFindCustomerByEmail();
                    break;
                case "4":
                    handleRegisterCustomer();
                    break;
                case "5":
                    handleUpdateCustomer();
                    break;
                case "6":
                    handleDeleteCustomer();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("[Error] Invalid option. Please choose between 0 and 6.");
            }
        }
    }

    private void handleListAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("\n[Info] No customers found in the system.");
                return;
            }
            printCustomerHeader();
            for (Customer c : customers) {
                printCustomerRow(c);
            }
            printCustomerFooter();
        } catch (Exception e) {
            System.out.println("[Error] Failed to retrieve customers: " + e.getMessage());
        }
    }

    private void handleFindCustomerById() {
        try {
            System.out.print("Enter Customer ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Customer customer = customerService.getCustomerById(id);
            printCustomerHeader();
            printCustomerRow(customer);
            printCustomerFooter();
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid Customer ID format. ID must be a number.");
        } catch (CustomerNotFoundException e) {
            System.out.println("[Error] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[Error] An error occurred: " + e.getMessage());
        }
    }

    private void handleFindCustomerByEmail() {
        try {
            System.out.print("Enter Customer Email: ");
            String email = scanner.nextLine().trim();
            Customer customer = customerService.getCustomerByEmail(email);
            printCustomerHeader();
            printCustomerRow(customer);
            printCustomerFooter();
        } catch (CustomerNotFoundException e) {
            System.out.println("[Error] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[Error] An error occurred: " + e.getMessage());
        }
    }

    private void handleRegisterCustomer() {
        try {
            System.out.println("\n--- Register New Customer ---");
            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Email (e.g. john@example.com): ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter Phone (e.g. +84123456789): ");
            String phone = scanner.nextLine().trim();

            Customer registered = customerService.registerCustomer(name, email, phone);
            System.out.println("[Success] Customer registered successfully with ID: " + registered.getId());
        } catch (InvalidInputException e) {
            System.out.println("[Error] Validation failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[Error] Failed to register customer: " + e.getMessage());
        }
    }

    private void handleUpdateCustomer() {
        try {
            System.out.print("Enter Customer ID to update: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            Customer existing = customerService.getCustomerById(id);
            System.out.println("Current Details: " + existing);

            System.out.print("Enter New Name [" + existing.getName() + "]: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = existing.getName();

            System.out.print("Enter New Email [" + existing.getEmail() + "]: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) email = existing.getEmail();

            System.out.print("Enter New Phone [" + existing.getPhone() + "]: ");
            String phone = scanner.nextLine().trim();
            if (phone.isEmpty()) phone = existing.getPhone();

            Customer updated = customerService.updateCustomer(id, name, email, phone);
            System.out.println("[Success] Customer updated successfully.");
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid Customer ID format.");
        } catch (CustomerNotFoundException | InvalidInputException e) {
            System.out.println("[Error] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[Error] Failed to update customer: " + e.getMessage());
        }
    }

    private void handleDeleteCustomer() {
        try {
            System.out.print("Enter Customer ID to delete: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            customerService.deleteCustomer(id);
            System.out.println("[Success] Customer with ID " + id + " was deleted successfully.");
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid Customer ID format.");
        } catch (CustomerNotFoundException e) {
            System.out.println("[Error] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[Error] Failed to delete customer: " + e.getMessage());
        }
    }

    private void printCustomerHeader() {
        System.out.println("\n+-----+------------------------------+------------------------------------+-----------------+");
        System.out.printf("| %-3s | %-28s | %-34s | %-15s |\n", "ID", "NAME", "EMAIL", "PHONE");
        System.out.println("+-----+------------------------------+------------------------------------+-----------------+");
    }

    private void printCustomerRow(Customer c) {
        System.out.printf("| %-3d | %-28s | %-34s | %-15s |\n",
                c.getId(),
                truncate(c.getName(), 28),
                truncate(c.getEmail(), 34),
                truncate(c.getPhone(), 15));
    }

    private void printCustomerFooter() {
        System.out.println("+-----+------------------------------+------------------------------------+-----------------+");
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
