package org.phalkun.io;

import org.phalkun.exception.DataAccessException;
import org.phalkun.exception.InvalidInputException;
import org.phalkun.dto.DataImportResult;
import org.phalkun.service.ProductService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

public class DataImportService {
    private static final String LINE_PREFIX = "Line ";
    private final ProductService productService;

    public DataImportService(ProductService productService) {
        this.productService = productService;
    }

    public DataImportResult importProductsFromCsv(String filePath) {
        DataImportResult result = new DataImportResult();
        int lineNum = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (!line.trim().isEmpty() && !line.toLowerCase().startsWith("sku,name")) {
                    String[] parts = line.split(",");
                    if (parts.length < 5) {
                        result.addError(LINE_PREFIX + lineNum + ": Malformed CSV line, not enough columns.");
                    } else {
                        processCsvLine(parts, lineNum, result);
                    }
                }
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to read CSV file: " + filePath, e);
        }

        return result;
    }

    private void processCsvLine(String[] parts, int lineNum, DataImportResult result) {
        try {
            String sku = parts[0].trim();
            String name = parts[1].trim();
            String category = parts[2].trim();
            BigDecimal price = new BigDecimal(parts[3].trim());
            int stock = Integer.parseInt(parts[4].trim());

            productService.createProduct(sku, name, category, price, stock);
            result.incrementSuccessfulImports();
        } catch (NumberFormatException e) {
            result.addError(LINE_PREFIX + lineNum + ": Invalid numeric format for price or stock (" + e.getMessage() + ").");
        } catch (InvalidInputException e) {
            result.addError(LINE_PREFIX + lineNum + ": Failed to import product - " + e.getMessage());
        } catch (Exception e) {
            result.addError(LINE_PREFIX + lineNum + ": Unexpected error - " + e.getMessage());
        }
    }
}
