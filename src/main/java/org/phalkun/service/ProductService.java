package org.phalkun.service;

import org.phalkun.exception.InvalidInputException;
import org.phalkun.exception.ProductNotFoundException;
import org.phalkun.model.Product;
import org.phalkun.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(String sku, String name, String category, BigDecimal price, int stockQuantity) {
        if (productRepository.findBySku(sku).isPresent()) {
            throw new InvalidInputException("Product with SKU '" + sku + "' already exists.");
        }
        Product product = new Product(sku, name, category, price, stockQuantity);
        productRepository.save(product);
        return product;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
    }

    public Product updateProduct(Long id, String sku, String name, String category, BigDecimal price, int stockQuantity) {
        Product existingProduct = getProductById(id);
        
        if (!existingProduct.getSku().equalsIgnoreCase(sku)) {
            Optional<Product> productWithSameSku = productRepository.findBySku(sku);
            if (productWithSameSku.isPresent() && !productWithSameSku.get().getId().equals(id)) {
                throw new InvalidInputException("Product with SKU '" + sku + "' already exists.");
            }
        }

        existingProduct.setSku(sku);
        existingProduct.setName(name);
        existingProduct.setCategory(category);
        existingProduct.setPrice(price);
        existingProduct.setStockQuantity(stockQuantity);

        productRepository.update(existingProduct);
        return existingProduct;
    }

    public void deleteProduct(Long id) {
        getProductById(id); // Ensures product exists
        productRepository.deleteById(id);
    }

    public Product updateStockQuantity(Long id, int newQuantity) {
        Product product = getProductById(id);
        product.setStockQuantity(newQuantity); // Validates non-negative
        productRepository.updateStock(id, newQuantity);
        return product;
    }

    public Product adjustStockQuantity(Long id, int delta) {
        Product product = getProductById(id);
        int newQuantity = product.getStockQuantity() + delta;
        product.setStockQuantity(newQuantity); // Validates non-negative
        productRepository.updateStock(id, newQuantity);
        return product;
    }
}
