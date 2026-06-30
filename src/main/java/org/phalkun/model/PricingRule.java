package org.phalkun.model;

import java.math.BigDecimal;

@FunctionalInterface
public interface PricingRule {
    BigDecimal calculateDiscount(Order order);
}
