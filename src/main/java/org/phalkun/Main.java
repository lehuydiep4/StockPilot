package org.phalkun;

import org.phalkun.io.DataImportService;
import org.phalkun.io.DocumentExportService;
import org.phalkun.repository.CustomerRepository;
import org.phalkun.repository.OrderRepository;
import org.phalkun.repository.ProductRepository;
import org.phalkun.service.CustomerService;
import org.phalkun.service.OrderService;
import org.phalkun.service.ProductService;
import org.phalkun.service.ReportService;
import org.phalkun.ui.CustomerConsoleUI;
import org.phalkun.ui.DataIOConsoleUI;
import org.phalkun.ui.MainMenuUI;
import org.phalkun.ui.OrderConsoleUI;
import org.phalkun.ui.ProductConsoleUI;
import org.phalkun.ui.ReportConsoleUI;
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

        OrderRepository orderRepository = new OrderRepository();
        OrderService orderService = new OrderService(orderRepository, productRepository, customerRepository);

        DataImportService dataImportService = new DataImportService(productService);
        DocumentExportService documentExportService = new DocumentExportService();
        org.phalkun.concurrent.FlashSaleSimulator flashSaleSimulator = new org.phalkun.concurrent.FlashSaleSimulator(orderService);
        org.phalkun.concurrent.SalesAutoExporter salesAutoExporter = new org.phalkun.concurrent.SalesAutoExporter(orderService, documentExportService);

        ReportService reportService = new ReportService(orderService, productService);

        // 3. Initialize UI layers & scanner
        Scanner scanner = new Scanner(System.in);
        ProductConsoleUI productConsoleUI = new ProductConsoleUI(productService, scanner);
        CustomerConsoleUI customerConsoleUI = new CustomerConsoleUI(customerService, scanner);
        OrderConsoleUI orderConsoleUI = new OrderConsoleUI(orderService, customerService, productService, scanner);
        DataIOConsoleUI dataIOConsoleUI = new DataIOConsoleUI(dataImportService, documentExportService, orderService, scanner);
        ReportConsoleUI reportConsoleUI = new ReportConsoleUI(reportService, scanner);
        MainMenuUI mainMenuUI = new MainMenuUI(productConsoleUI, customerConsoleUI, orderConsoleUI, dataIOConsoleUI, flashSaleSimulator, salesAutoExporter, reportConsoleUI, scanner);

        // 4. Start application UI loop
        mainMenuUI.displayMenu();

        scanner.close();
    }
}