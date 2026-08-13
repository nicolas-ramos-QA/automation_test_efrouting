package com.calculos_de_rutas.models;

/**
 * Una fila del reporte: Backend → redondeo → Frontend.
 */
public class FinancialCheck {

    public enum Status {
        PASS("OK"),
        FAIL("FALLÓ"),
        WARN("AVISO"),
        SKIP("OMITIDO");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final String phase;
    private final String scope;
    private final String field;
    private final String formula;
    private final String backendValue;
    private final String roundedValue;
    private final String frontendValue;
    private final String difference;
    private final Status status;
    private final String detail;

    private FinancialCheck(Builder builder) {
        this.phase = builder.phase;
        this.scope = builder.scope;
        this.field = builder.field;
        this.formula = builder.formula;
        this.backendValue = builder.backendValue;
        this.roundedValue = builder.roundedValue;
        this.frontendValue = builder.frontendValue;
        this.difference = builder.difference;
        this.status = builder.status;
        this.detail = builder.detail;
    }

    public String getPhase() {
        return phase;
    }

    public String getScope() {
        return scope;
    }

    public String getField() {
        return field;
    }

    public String getFormula() {
        return formula == null ? "" : formula;
    }

    public String getBackendValue() {
        return backendValue == null ? "—" : backendValue;
    }

    public String getRoundedValue() {
        return roundedValue == null || roundedValue.isBlank() ? "—" : roundedValue;
    }

    public String getFrontendValue() {
        return frontendValue == null ? "—" : frontendValue;
    }

    public String getDifference() {
        return difference == null ? "—" : difference;
    }

    public Status getStatus() {
        return status;
    }

    public String getDetail() {
        return detail == null ? "" : detail;
    }

    public boolean isFailure() {
        return status == Status.FAIL;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String phase = "General";
        private String scope = "-";
        private String field = "-";
        private String formula;
        private String backendValue;
        private String roundedValue;
        private String frontendValue;
        private String difference;
        private Status status = Status.PASS;
        private String detail;

        public Builder phase(String phase) {
            this.phase = phase;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder formula(String formula) {
            this.formula = formula;
            return this;
        }

        public Builder backendValue(String backendValue) {
            this.backendValue = backendValue;
            return this;
        }

        public Builder roundedValue(String roundedValue) {
            this.roundedValue = roundedValue;
            return this;
        }

        public Builder frontendValue(String frontendValue) {
            this.frontendValue = frontendValue;
            return this;
        }

        public Builder difference(String difference) {
            this.difference = difference;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public FinancialCheck build() {
            return new FinancialCheck(this);
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | Backend=%s | Redondeo=%s | Frontend=%s",
                status.getLabel(), scope, field, getBackendValue(), getRoundedValue(), getFrontendValue());
    }
}
