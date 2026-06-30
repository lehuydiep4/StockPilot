package org.phalkun;

import org.phalkun.repository.CustomerRepository;
import org.phalkun.repository.ProductRepository;
import org.phalkun.service.CustomerService;
import org.phalkun.service.ProductService;
import org.phalkun.ui.CustomerConsoleUI;
import org.phalkun.ui.MainMenuUI;
import org.phalkun.ui.ProductConsoleUI;
import org.phalkun.util.DbUtil;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize database schema
        DbUtil.initDatabase();

        // 2. Initialize repositories & services
        ProductRepository productRepository = new ProductRepository();
        ProductService productService = new ProductService(productRepository);

        CustomerRepository customerRepository = new CustomerRepository();
        CustomerService customerService = new CustomerService(customerRepository);

        // 3. Initialize UI layers & scanner
        Scanner scanner = new Scanner(System.in);
        ProductConsoleUI productConsoleUI = new ProductConsoleUI(productService, scanner);
        CustomerConsoleUI customerConsoleUI = new CustomerConsoleUI(customerService, scanner);
        MainMenuUI mainMenuUI = new MainMenuUI(productConsoleUI, customerConsoleUI, scanner);

        // 4. Start application UI loop
        mainMenuUI.displayMenu();

        scanner.close();
    }
}