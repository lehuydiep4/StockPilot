package org.phalkun.dto;

import java.util.ArrayList;
import java.util.List;

public class DataImportResult {
    private int successfulImports = 0;
    private final List<String> errors = new ArrayList<>();

    public int getSuccessfulImports() {
        return successfulImports;
    }

    public void incrementSuccessfulImports() {
        this.successfulImports++;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
