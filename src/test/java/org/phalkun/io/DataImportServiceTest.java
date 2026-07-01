package org.phalkun.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.phalkun.dto.DataImportResult;
import org.phalkun.model.Product;
import org.phalkun.service.ProductService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataImportServiceTest {

    private DataImportService importService;
    private DummyProductService productService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        productService = new DummyProductService();
        importService = new DataImportService(productService);
    }

    @Test
    void testImportProductsFromCsv_ValidAndInvalidLines() throws IOException {
        Path csvFile = tempDir.resolve("test_products.csv");
        String csvContent = """
                sku,name,category,price,stock\n
                ABC-0001,Laptop,Electronics,999.99,10\n
                ABC-0002,Mouse,Electronics,badprice,10\n
                ABC-0003,Keyboard,Electronics,49.99\n
                ABC-0004,Monitor,Electronics,199.99,5\n
                \n
                """;
        Files.writeString(csvFile, csvContent);

        DataImportResult result = importService.importProductsFromCsv(csvFile.toString());

        assertEquals(2, result.getSuccessfulImports(), "Should import 2 products successfully");
        assertEquals(2, result.getErrors().size(), "Should have 2 errors for invalid lines");
        assertTrue(result.getErrors().get(0).contains("Invalid numeric format"));
        assertTrue(result.getErrors().get(1).contains("Malformed CSV line"));
        
        // Check dummy service to see if methods were called
        assertEquals(2, productService.createCount);
    }

    // A dummy ProductService to track calls
    private static class DummyProductService extends ProductService {
        int createCount = 0;

        public DummyProductService() {
            super(null); // No need for actual repository
        }

        @Override
        public Product createProduct(String sku, String name, String category, BigDecimal price, int stock) {
            createCount++;
            return new Product(sku, name, category, price, stock);
        }
    }
}
