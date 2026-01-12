package com.tablegen.generator;

public enum TemplateType {
    THYMELEAF,
    MUSTACHE,
    HTML;

    public static TemplateType fromString(String value) {
        if (value == null) return HTML;
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown template type: " + value);
        }
    }
}
