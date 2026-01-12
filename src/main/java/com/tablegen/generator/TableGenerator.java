package com.tablegen.generator;

import com.tablegen.core.ColumnMeta;
import java.util.List;
import java.util.Set;

public interface TableGenerator {
    
    Set<String> MONEY_KEYWORDS = Set.of("amt", "amount", "fee", "price");

    String generate(List<ColumnMeta> columns);

    default boolean isMoneyColumn(String columnName) {
        if (columnName == null) return false;
        String lower = columnName.toLowerCase();
        for (String keyword : MONEY_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
