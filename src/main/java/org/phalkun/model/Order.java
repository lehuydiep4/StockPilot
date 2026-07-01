package org.phalkun.model;

import org.phalkun.exception.InvalidInputException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private Long id;
    private Customer customer;
    private LocalDateTime orderDate;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;

    public Order() {
        this.orderDate = LocalDateTime.now(ZoneId.systemDefault());
    }

    public Order(Long id, Customer customer, LocalDateTime orderDate, List<OrderItem> items, BigDecimal totalAmount, BigDecimal discountAmount) {
        setId(id);
        setCustomer(customer);
        setOrderDate(orderDate != null ? orderDate : LocalDateTime.now(ZoneId.systemDefault()));
        setItems(items != null ? items : new ArrayList<>());
        setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        setDiscountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    public Order(Customer customer) {
        this(null, customer, LocalDateTime.now(ZoneId.systemDefault()), new ArrayList<>(), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidInputException("Customer for the order cannot be null.");
        }
        this.customer = customer;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new InvalidInputException("Cannot add a null order item.");
        }
        item.setOrderId(this.id);
        this.items.add(item);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null) {
            throw new InvalidInputException("Order total amount cannot be null.");
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Order total amount cannot be negative.");
        }
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        if (discountAmount == null) {
            throw new InvalidInputException("Order discount amount cannot be null.");
        }
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Order discount amount cannot be negative.");
        }
        this.discountAmount = discountAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer=" + (customer != null ? customer.getName() : null) +
                ", orderDate=" + orderDate +
                ", itemsCount=" + items.size() +
                ", totalAmount=" + totalAmount +
                ", discountAmount=" + discountAmount +
                '}';
    }
}
