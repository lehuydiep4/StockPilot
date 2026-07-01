package org.phalkun.model.discount;

import org.phalkun.model.Order;
import java.math.BigDecimal;

public class NoDiscount implements DiscountPolicy {
    @Override
    public BigDecimal calculateDiscount(Order order) {
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "No Discount";
    }
}
