package org.phalkun.model.discount;

import org.phalkun.model.Order;
import org.phalkun.model.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageDiscount implements DiscountPolicy {
    private final BigDecimal percentage;

    public PercentageDiscount(BigDecimal percentage) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0.0 and 1.0.");
        }
        this.percentage = percentage;
    }

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            BigDecimal itemSubtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            sum = sum.add(itemSubtotal);
        }

        return sum.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Percentage Discount (" + percentage.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%)";
    }
}
