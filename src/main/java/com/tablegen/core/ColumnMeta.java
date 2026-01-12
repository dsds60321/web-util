package com.tablegen.core;

public record ColumnMeta(
    String name,
    String type,
    boolean isNullable,
    boolean isKey,
    String comment,
    int ordinalPosition
) {}
