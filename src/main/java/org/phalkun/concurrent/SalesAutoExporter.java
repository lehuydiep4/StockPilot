package org.phalkun.concurrent;

import org.phalkun.io.DocumentExportService;
import org.phalkun.model.Order;
import org.phalkun.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SalesAutoExporter {
    private static final Logger logger = LoggerFactory.getLogger(SalesAutoExporter.class);

    private final OrderService orderService;
    private final DocumentExportService exportService;
    private ScheduledExecutorService scheduler;

    public SalesAutoExporter(OrderService orderService, DocumentExportService exportService) {
        this.orderService = orderService;
        this.exportService = exportService;
    }

    public void start(int intervalMinutes, String outputDir) {
        if (scheduler != null && !scheduler.isShutdown()) {
            logger.warn("SalesAutoExporter is already running.");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("Auto-exporting sales snapshot...");
                List<Order> orders = orderService.getAllOrders();
                String path = exportService.exportSalesReport(orders, outputDir);
                logger.info("Sales snapshot successfully exported to: {}", path);
            } catch (org.phalkun.exception.DataAccessException e) {
                logger.error("Failed to auto-export sales snapshot", e);
            } catch (Exception e) {
                logger.error("Unexpected error during auto-export", e);
            }
        }, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
