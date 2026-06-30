package org.phalkun.ui;

import org.phalkun.exception.InvalidInputException;
import org.phalkun.exception.ProductNotFoundException;
import org.phalkun.model.Product;
import org.phalkun.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ProductConsoleUI {
    private static final String ERROR_PREFIX = "[Error] ";
    private final ProductService productService;
    private final Scanner scanner;

    public ProductConsoleUI(ProductService productService, Scanner scanner) {
        this.productService = productService;
        this.scanner = scanner;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n==========================================");
            System.out.println("   PRODUCT & INVENTORY MANAGEMENT MENU");
            System.out.println("==========================================");
            System.out.println("1. List All Products");
            System.out.println("2. Find Product by ID");
            System.out.println("3. Find Product by SKU");
            System.out.println("4. Create New Product");
            System.out.println("5. Update Product");
            System.out.println("6. Delete Product");
            System.out.println("7. Adjust Stock Quantity");
            System.out.println("0. Back to Main Menu");
            System.out.print("Please enter your choice: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    handleListAllProducts();
                    break;
                case "2":
                    handleFindProductById();
                    break;
                case "3":
                    handleFindProductBySku();
                    break;
                case "4":
                    handleCreateProduct();
                    break;
                case "5":
                    handleUpdateProduct();
                    break;
                case "6":
                    handleDeleteProduct();
                    break;
                case "7":
                    handleAdjustStock();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("[Error] Invalid option. Please choose between 0 and 7.");
            }
        }
    }

    private void handleListAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            if (products.isEmpty()) {
                System.out.println("\n[Info] No products found in the inventory.");
                return;
            }
            printProductHeader();
            for (Product p : products) {
                printProductRow(p);
            }
            printProductFooter();
        } catch (Exception e) {
            System.out.println("[Error] Failed to retrieve products: " + e.getMessage());
        }
    }

    private void handleFindProductById() {
        try {
            System.out.print("Enter Product ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Product product = productService.getProductById(id);
            printProductHeader();
            printProductRow(product);
            printProductFooter();
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid Product ID format. ID must be a number.");
        } catch (ProductNotFoundException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "An error occurred: " + e.getMessage());
        }
    }

    private void handleFindProductBySku() {
        try {
            System.out.print("Enter Product SKU (e.g. ABC-1234): ");
            String sku = scanner.nextLine().trim();
            Product product = productService.getProductBySku(sku);
            printProductHeader();
            printProductRow(product);
            printProductFooter();
        } catch (ProductNotFoundException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "An error occurred: " + e.getMessage());
        }
    }

    private void handleCreateProduct() {
        try {
            System.out.println("\n--- Create New Product ---");
            System.out.print("Enter SKU (format: ABC-1234): ");
            String sku = scanner.nextLine().trim();

            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Category: ");
            String category = scanner.nextLine().trim();

            System.out.print("Enter Price (e.g. 29.99): ");
            BigDecimal price = new BigDecimal(scanner.nextLine().trim());

            System.out.print("Enter Initial Stock Quantity: ");
            int stock = Integer.parseInt(scanner.nextLine().trim());

            Product created = productService.createProduct(sku, name, category, price, stock);
            System.out.println("[Success] Product created successfully with ID: " + created.getId());
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid input for price or stock quantity. Please enter numeric values.");
        } catch (InvalidInputException e) {
            System.out.println(ERROR_PREFIX + "Validation failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to create product: " + e.getMessage());
        }
    }

    private void handleUpdateProduct() {
        try {
            System.out.print("Enter Product ID to update: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            Product existing = productService.getProductById(id);
            System.out.println("Current Details: " + existing);

            System.out.print("Enter New SKU [" + existing.getSku() + "]: ");
            String sku = scanner.nextLine().trim();
            if (sku.isEmpty()) sku = existing.getSku();

            System.out.print("Enter New Name [" + existing.getName() + "]: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = existing.getName();

            System.out.print("Enter New Category [" + existing.getCategory() + "]: ");
            String category = scanner.nextLine().trim();
            if (category.isEmpty()) category = existing.getCategory();

            System.out.print("Enter New Price [" + existing.getPrice() + "]: ");
            String priceStr = scanner.nextLine().trim();
            BigDecimal price = priceStr.isEmpty() ? existing.getPrice() : new BigDecimal(priceStr);

            System.out.print("Enter New Stock Quantity [" + existing.getStockQuantity() + "]: ");
            String stockStr = scanner.nextLine().trim();
            int stock = stockStr.isEmpty() ? existing.getStockQuantity() : Integer.parseInt(stockStr);

            productService.updateProduct(id, sku, name, category, price, stock);
            System.out.println("[Success] Product updated successfully.");
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid number input. Please try again.");
        } catch (ProductNotFoundException | InvalidInputException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to update product: " + e.getMessage());
        }
    }

    private void handleDeleteProduct() {
        try {
            System.out.print("Enter Product ID to delete: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            productService.deleteProduct(id);
            System.out.println("[Success] Product with ID " + id + " was deleted successfully.");
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid Product ID format.");
        } catch (ProductNotFoundException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to delete product: " + e.getMessage());
        }
    }

    private void handleAdjustStock() {
        try {
            System.out.print("Enter Product ID for stock adjustment: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            Product product = productService.getProductById(id);
            System.out.println("Current Stock for '" + product.getName() + "': " + product.getStockQuantity());

            System.out.println("Choose adjustment mode:");
            System.out.println("1. Set absolute stock quantity");
            System.out.println("2. Add/Subtract delta to current stock");
            System.out.print("Choice (1 or 2): ");
            String mode = scanner.nextLine().trim();

            if ("1".equals(mode)) {
                System.out.print("Enter new absolute stock quantity: ");
                int newStock = Integer.parseInt(scanner.nextLine().trim());
                productService.updateStockQuantity(id, newStock);
                System.out.println("[Success] Stock updated to " + newStock);
            } else if ("2".equals(mode)) {
                System.out.print("Enter quantity delta (positive to add, negative to reduce): ");
                int delta = Integer.parseInt(scanner.nextLine().trim());
                Product updated = productService.adjustStockQuantity(id, delta);
                System.out.println("[Success] Stock adjusted. New stock: " + updated.getStockQuantity());
            } else {
                System.out.println(ERROR_PREFIX + "Invalid mode chosen.");
            }
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid numeric input.");
        } catch (ProductNotFoundException | InvalidInputException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to adjust stock: " + e.getMessage());
        }
    }

    private void printProductHeader() {
        System.out.println("\n+-----+----------+------------------------------+--------------------+------------+-------+");
        System.out.printf("| %-3s | %-8s | %-28s | %-18s | %-10s | %-5s |%n", "ID", "SKU", "NAME", "CATEGORY", "PRICE ($)", "STOCK");
        System.out.println("+-----+----------+------------------------------+--------------------+------------+-------+");
    }

    private void printProductRow(Product p) {
        System.out.printf("| %-3d | %-8s | %-28s | %-18s | %-10.2f | %-5d |%n",
                p.getId(),
                p.getSku(),
                truncate(p.getName(), 28),
                truncate(p.getCategory(), 18),
                p.getPrice(),
                p.getStockQuantity());
    }

    private void printProductFooter() {
        System.out.println("+-----+----------+------------------------------+--------------------+------------+-------+");
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
