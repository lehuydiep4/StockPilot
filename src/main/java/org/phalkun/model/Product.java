package org.phalkun.model;

import org.phalkun.exception.InvalidInputException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

public class Product {
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z]{3}-\\d{4}$");

    private Long id;
    private String sku;
    private String name;
    private String category;
    private BigDecimal price;
    private int stockQuantity;

    public Product() {}

    public Product(Long id, String sku, String name, String category, BigDecimal price, int stockQuantity) {
        setId(id);
        setSku(sku);
        setName(name);
        setCategory(category);
        setPrice(price);
        setStockQuantity(stockQuantity);
    }

    public Product(String sku, String name, String category, BigDecimal price, int stockQuantity) {
        this(null, sku, name, category, price, stockQuantity);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new InvalidInputException("SKU cannot be empty.");
        }
        if (!SKU_PATTERN.matcher(sku).matches()) {
            throw new InvalidInputException("SKU format must be 3 uppercase letters followed by a hyphen and 4 digits (e.g., ABC-1234).");
        }
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("Product name cannot be empty.");
        }
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new InvalidInputException("Product category cannot be empty.");
        }
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null) {
            throw new InvalidInputException("Product price cannot be null.");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Product price cannot be negative.");
        }
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new InvalidInputException("Stock quantity cannot be negative.");
        }
        this.stockQuantity = stockQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) && Objects.equals(sku, product.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sku);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}
