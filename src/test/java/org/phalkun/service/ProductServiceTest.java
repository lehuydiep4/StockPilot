package org.phalkun.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phalkun.exception.InvalidInputException;
import org.phalkun.exception.ProductNotFoundException;
import org.phalkun.model.Product;
import org.phalkun.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductRepository mockRepository;
    private ProductService productService;
    private List<Product> database;

    @BeforeEach
    void setUp() {
        database = new ArrayList<>();
        // Simple in-memory mock implementation for fast unit testing without DB dependency
        mockRepository = new ProductRepository() {
            private long idSequence = 1L;

            @Override
            public void save(Product product) {
                product.setId(idSequence++);
                database.add(product);
            }

            @Override
            public Optional<Product> findById(Long id) {
                return database.stream().filter(p -> p.getId().equals(id)).findFirst();
            }

            @Override
            public Optional<Product> findBySku(String sku) {
                return database.stream().filter(p -> p.getSku().equalsIgnoreCase(sku)).findFirst();
            }

            @Override
            public List<Product> findAll() {
                return new ArrayList<>(database);
            }

            @Override
            public void update(Product product) {
                deleteById(product.getId());
                database.add(product);
            }

            @Override
            public void deleteById(Long id) {
                database.removeIf(p -> p.getId().equals(id));
            }

            @Override
            public void updateStock(Long id, int newQuantity) {
                findById(id).ifPresent(p -> p.setStockQuantity(newQuantity));
            }
        };

        productService = new ProductService(mockRepository);
    }

    @Test
    void testCreateProduct_Success() {
        Product p = productService.createProduct("PRO-1001", "Laptop", "Electronics", new BigDecimal("999.99"), 10);
        assertNotNull(p.getId());
        assertEquals("PRO-1001", p.getSku());
        assertEquals(1, productService.getAllProducts().size());
    }

    @Test
    void testCreateProduct_DuplicateSkuThrowsException() {
        productService.createProduct("PRO-1001", "Laptop", "Electronics", new BigDecimal("999.99"), 10);
        assertThrows(InvalidInputException.class, () -> 
            productService.createProduct("PRO-1001", "Desktop", "Electronics", new BigDecimal("1200.00"), 5)
        );
    }

    @Test
    void testGetProductById_NotFoundThrowsException() {
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void testAdjustStockQuantity_Success() {
        Product p = productService.createProduct("PRO-1002", "Mouse", "Electronics", new BigDecimal("25.00"), 20);
        productService.adjustStockQuantity(p.getId(), -5);
        assertEquals(15, productService.getProductById(p.getId()).getStockQuantity());
    }
}
