package com.faturalab.automation.utils.parity;

/** Eski ↔ yeni karşılaştırmasında tek bir bulgu. */
public final class ParityFinding {

    /** FAIL = fonksiyon/erişim kaybı (senaryo düşer), WARN = incelenmeli, INFO = bilgi (yeni öğe vb.). */
    public enum Severity { FAIL, WARN, INFO }

    private final Severity severity;
    private final String category;
    private final String message;

    public ParityFinding(Severity severity, String category, String message) {
        this.severity = severity;
        this.category = category;
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "[" + severity + "][" + category + "] " + message;
    }
}
