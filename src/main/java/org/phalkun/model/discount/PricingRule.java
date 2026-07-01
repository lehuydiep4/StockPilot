package org.phalkun.model.discount;

import org.phalkun.model.Order;
import java.math.BigDecimal;

@FunctionalInterface
public interface PricingRule {
    BigDecimal calculateDiscount(Order order);
}
