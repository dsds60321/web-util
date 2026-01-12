package com.tablegen.dto;

public class GenerateResponse {
    private String tableName;
    private String generatedCode;
    private int columnCount;

    public GenerateResponse(String tableName, String generatedCode, int columnCount) {
        this.tableName = tableName;
        this.generatedCode = generatedCode;
        this.columnCount = columnCount;
    }

    public String getTableName() {
        return tableName;
    }

    public String getGeneratedCode() {
        return generatedCode;
    }

    public int getColumnCount() {
        return columnCount;
    }
}
