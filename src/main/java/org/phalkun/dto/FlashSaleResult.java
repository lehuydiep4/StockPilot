package org.phalkun.dto;

public class FlashSaleResult {
    private final int successfulOrders;
    private final int failedDueToStock;
    private final int failedDueToOther;

    public FlashSaleResult(int successfulOrders, int failedDueToStock, int failedDueToOther) {
        this.successfulOrders = successfulOrders;
        this.failedDueToStock = failedDueToStock;
        this.failedDueToOther = failedDueToOther;
    }

    public int getSuccessfulOrders() {
        return successfulOrders;
    }

    public int getFailedDueToStock() {
        return failedDueToStock;
    }

    public int getFailedDueToOther() {
        return failedDueToOther;
    }
}
