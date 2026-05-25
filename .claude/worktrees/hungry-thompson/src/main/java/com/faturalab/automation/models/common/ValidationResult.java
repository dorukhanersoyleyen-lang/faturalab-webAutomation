package com.faturalab.automation.models.common;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    
    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    // Getters and Setters
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    
    // Helper methods
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    public int getWarningCount() {
        return warnings.size();
    }
    
    public String getErrorSummary() {
        if (errors.isEmpty()) {
            return "No errors";
        }
        return String.join("; ", errors);
    }
    
    public String getWarningSummary() {
        if (warnings.isEmpty()) {
            return "No warnings";
        }
        return String.join("; ", warnings);
    }
    
    public String getFullSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Validation Result: ").append(valid ? "VALID" : "INVALID").append("\n");
        
        if (hasErrors()) {
            summary.append("Errors (").append(getErrorCount()).append("):\n");
            for (int i = 0; i < errors.size(); i++) {
                summary.append("  ").append(i + 1).append(". ").append(errors.get(i)).append("\n");
            }
        }
        
        if (hasWarnings()) {
            summary.append("Warnings (").append(getWarningCount()).append("):\n");
            for (int i = 0; i < warnings.size(); i++) {
                summary.append("  ").append(i + 1).append(". ").append(warnings.get(i)).append("\n");
            }
        }
        
        if (!hasErrors() && !hasWarnings()) {
            summary.append("No issues found.");
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errors=" + getErrorCount() +
                ", warnings=" + getWarningCount() +
                '}';
    }
} 