package org.phalkun.model.discount;

import org.phalkun.model.Order;
import org.phalkun.model.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BulkDiscount implements DiscountPolicy {
    private final int thresholdQuantity;
    private final BigDecimal discountPercentage;

    public BulkDiscount(int thresholdQuantity, BigDecimal discountPercentage) {
        if (thresholdQuantity <= 0) {
            throw new IllegalArgumentException("Threshold quantity must be greater than 0.");
        }
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) < 0 || discountPercentage.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0.0 and 1.0.");
        }
        this.thresholdQuantity = thresholdQuantity;
        this.discountPercentage = discountPercentage;
    }

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getQuantity() >= thresholdQuantity) {
                BigDecimal itemSubtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal itemDiscount = itemSubtotal.multiply(discountPercentage);
                totalDiscount = totalDiscount.add(itemDiscount);
            }
        }
        return totalDiscount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Bulk Discount (Qty >= " + thresholdQuantity + ", " + 
               discountPercentage.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "% off bulk items)";
    }
}
