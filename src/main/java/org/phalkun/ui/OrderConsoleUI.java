package org.phalkun.ui;

import org.phalkun.exception.CustomerNotFoundException;
import org.phalkun.exception.InsufficientStockException;
import org.phalkun.exception.ProductNotFoundException;
import org.phalkun.model.*;
import org.phalkun.service.CustomerService;
import org.phalkun.service.OrderService;
import org.phalkun.service.ProductService;

import java.math.BigDecimal;
import java.util.*;

public class OrderConsoleUI {
    private static final String ERROR_PREFIX = "[Error] ";

    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final Scanner scanner;

    public OrderConsoleUI(OrderService orderService, CustomerService customerService, ProductService productService, Scanner scanner) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.productService = productService;
        this.scanner = scanner;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n==========================================");
            System.out.println("         ORDER PROCESSING MENU            ");
            System.out.println("==========================================");
            System.out.println("1. Create New Order (Checkout)");
            System.out.println("2. List All Orders");
            System.out.println("3. View Order Details");
            System.out.println("0. Back to Main Menu");
            System.out.print("Please enter your choice: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    handleCreateOrder();
                    break;
                case "2":
                    handleListAllOrders();
                    break;
                case "3":
                    handleViewOrderDetails();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(ERROR_PREFIX + "Invalid option. Please choose between 0 and 3.");
            }
        }
    }

    private void handleCreateOrder() {
        try {
            System.out.println("\n--- Create New Order ---");

            // 1. Pick a Customer
            List<Customer> customers = customerService.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println(ERROR_PREFIX + "No customers found. Please register a customer first.");
                return;
            }

            System.out.println("\nSelect a Customer:");
            for (Customer c : customers) {
                System.out.printf("ID: %-3d | Name: %-20s | Email: %s%n", c.getId(), c.getName(), c.getEmail());
            }

            System.out.print("Enter Customer ID: ");
            Long customerId = Long.parseLong(scanner.nextLine().trim());
            // Pre-validate customer existence
            customerService.getCustomerById(customerId);

            // 2. Build the Cart
            Map<String, Integer> cart = new LinkedHashMap<>();
            boolean cartLoop = true;

            while (cartLoop) {
                System.out.print("\nEnter Product SKU to add to cart: ");
                String sku = scanner.nextLine().trim().toUpperCase();

                Product product;
                try {
                    product = productService.getProductBySku(sku);
                } catch (ProductNotFoundException e) {
                    System.out.println(ERROR_PREFIX + "Product with SKU '" + sku + "' not found.");
                    continue;
                }

                System.out.print("Enter Quantity (Available: " + product.getStockQuantity() + "): ");
                int qty;
                try {
                    qty = Integer.parseInt(scanner.nextLine().trim());
                    if (qty <= 0) {
                        System.out.println(ERROR_PREFIX + "Quantity must be greater than 0.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ERROR_PREFIX + "Invalid quantity format.");
                    continue;
                }

                int existingQty = cart.getOrDefault(sku, 0);
                if (product.getStockQuantity() < (existingQty + qty)) {
                    System.out.println(ERROR_PREFIX + "Insufficient stock. Only " + product.getStockQuantity() + " available.");
                    continue;
                }

                cart.put(sku, existingQty + qty);
                System.out.println("[Success] Added to cart: " + product.getName() + " x" + qty);

                printCurrentCart(cart);

                System.out.println("\nCart Options:");
                System.out.println("1. Add/Update Item");
                System.out.println("2. Remove Item");
                System.out.println("3. Proceed to Checkout");
                System.out.print("Select choice: ");
                String cartChoice = scanner.nextLine().trim();

                if ("2".equals(cartChoice)) {
                    System.out.print("Enter Product SKU to remove: ");
                    String removeSku = scanner.nextLine().trim().toUpperCase();
                    if (cart.containsKey(removeSku)) {
                        cart.remove(removeSku);
                        System.out.println("[Success] Removed from cart.");
                    } else {
                        System.out.println(ERROR_PREFIX + "SKU not in cart.");
                    }
                    printCurrentCart(cart);
                } else if ("3".equals(cartChoice)) {
                    if (cart.isEmpty()) {
                        System.out.println(ERROR_PREFIX + "Cannot checkout with an empty cart.");
                    } else {
                        cartLoop = false;
                    }
                }
            }

            // 3. Select Discount Policy
            System.out.println("\nSelect Discount Policy:");
            System.out.println("1. No Discount");
            System.out.println("2. Percentage Discount");
            System.out.println("3. Bulk Discount");
            System.out.print("Select choice: ");
            String policyChoice = scanner.nextLine().trim();

            DiscountPolicy discountPolicy = new NoDiscount();
            if ("2".equals(policyChoice)) {
                System.out.print("Enter Discount Percentage (e.g. 10 for 10%): ");
                double pct = Double.parseDouble(scanner.nextLine().trim());
                discountPolicy = new PercentageDiscount(BigDecimal.valueOf(pct / 100.0));
            } else if ("3".equals(policyChoice)) {
                System.out.print("Enter Bulk Minimum Quantity Threshold: ");
                int threshold = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Enter Discount Percentage for bulk items (e.g. 15 for 15%): ");
                double bulkPct = Double.parseDouble(scanner.nextLine().trim());
                discountPolicy = new BulkDiscount(threshold, BigDecimal.valueOf(bulkPct / 100.0));
            }

            // 4. Place Order
            System.out.print("\nPlace this order? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!"yes".equals(confirm) && !"y".equals(confirm)) {
                System.out.println("[Info] Checkout cancelled.");
                return;
            }

            Order placedOrder = orderService.placeOrder(customerId, cart, discountPolicy);
            System.out.println("[Success] Order placed successfully!");
            printInvoice(placedOrder);

        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid number format.");
        } catch (CustomerNotFoundException | ProductNotFoundException | InsufficientStockException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to process checkout: " + e.getMessage());
        }
    }

    private void printCurrentCart(Map<String, Integer> cart) {
        System.out.println("\n--- Current Cart ---");
        if (cart.isEmpty()) {
            System.out.println("(Empty)");
            return;
        }
        System.out.printf("%-10s | %-25s | %-8s | %-12s | %-12s%n", "SKU", "NAME", "QTY", "PRICE", "SUBTOTAL");
        System.out.println("----------------------------------------------------------------------");
        BigDecimal cartTotal = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product p = productService.getProductBySku(entry.getKey());
            int qty = entry.getValue();
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(qty));
            cartTotal = cartTotal.add(subtotal);
            System.out.printf("%-10s | %-25s | %-8d | $%-11.2f | $%-11.2f%n", p.getSku(), truncate(p.getName(), 25), qty, p.getPrice(), subtotal);
        }
        System.out.println("----------------------------------------------------------------------");
        System.out.printf("Cart Subtotal: $%.2f%n", cartTotal);
    }

    private void handleListAllOrders() {
        try {
            System.out.println("\nSort Orders by:");
            System.out.println("1. Order Date (Newest first)");
            System.out.println("2. Customer Name (A-Z)");
            System.out.println("3. Total Amount (Highest first)");
            System.out.print("Select sorting choice: ");
            String sortChoice = scanner.nextLine().trim();

            Comparator<Order> comparator;
            switch (sortChoice) {
                case "2":
                    comparator = Comparator.comparing(o -> o.getCustomer().getName().toLowerCase());
                    break;
                case "3":
                    comparator = (o1, o2) -> o2.getTotalAmount().compareTo(o1.getTotalAmount());
                    break;
                case "1":
                default:
                    comparator = (o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate());
                    break;
            }

            List<Order> orders = orderService.getAllOrders(comparator);
            if (orders.isEmpty()) {
                System.out.println("\n[Info] No orders found in the system.");
                return;
            }

            System.out.println("\n=================================================================================");
            System.out.printf("| %-8s | %-20s | %-20s | %-12s | %-10s |%n", "ORDER ID", "CUSTOMER", "DATE & TIME", "TOTAL AMOUNT", "ITEMS COUNT");
            System.out.println("=================================================================================");
            for (Order o : orders) {
                System.out.printf("| %-8d | %-20s | %-20s | $%-11.2f | %-11d |%n",
                        o.getId(),
                        truncate(o.getCustomer().getName(), 20),
                        o.getOrderDate().toString().replace('T', ' ').substring(0, 19),
                        o.getTotalAmount(),
                        o.getItems().size());
            }
            System.out.println("=================================================================================");
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "Failed to load orders: " + e.getMessage());
        }
    }

    private void handleViewOrderDetails() {
        try {
            System.out.print("Enter Order ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<Order> orderOpt = orderService.getAllOrders().stream()
                    .filter(o -> o.getId().equals(id))
                    .findFirst();

            if (orderOpt.isPresent()) {
                printInvoice(orderOpt.get());
            } else {
                System.out.println(ERROR_PREFIX + "Order not found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Invalid Order ID format.");
        } catch (Exception e) {
            System.out.println(ERROR_PREFIX + "An error occurred: " + e.getMessage());
        }
    }

    private void printInvoice(Order order) {
        System.out.println("\n======================================================================");
        System.out.println("                          ORDER INVOICE                               ");
        System.out.println("======================================================================");
        System.out.printf("Order ID:      %-10d | Date: %s%n", order.getId(), order.getOrderDate().toString().replace('T', ' ').substring(0, 19));
        System.out.printf("Customer Name: %-25s | Phone: %s%n", order.getCustomer().getName(), order.getCustomer().getPhone());
        System.out.printf("Customer Email:%-25s |%n", order.getCustomer().getEmail());
        System.out.println("----------------------------------------------------------------------");
        System.out.printf("%-10s | %-25s | %-8s | %-12s | %-12s%n", "SKU", "PRODUCT NAME", "QTY", "PRICE", "SUBTOTAL");
        System.out.println("----------------------------------------------------------------------");
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            BigDecimal itemSubtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
            System.out.printf("%-10s | %-25s | %-8d | $%-11.2f | $%-11.2f%n",
                    item.getProduct().getSku(),
                    truncate(item.getProduct().getName(), 25),
                    item.getQuantity(),
                    item.getPrice(),
                    itemSubtotal);
        }
        System.out.println("----------------------------------------------------------------------");
        System.out.printf("Subtotal:                                                 $%-11.2f%n", subtotal);
        System.out.printf("Discount Applied:                                         $%-11.2f%n", order.getDiscountAmount());
        System.out.printf("Grand Total:                                              $%-11.2f%n", order.getTotalAmount());
        System.out.println("======================================================================");
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
