package com.tablegen.generator;

import com.tablegen.core.ColumnMeta;
import com.tablegen.core.StringUtils;
import java.util.List;

public class ThymeleafGenerator implements TableGenerator {

    @Override
    public String generate(List<ColumnMeta> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n");
        
        // THEAD
        sb.append("  <thead>\n");
        sb.append("    <tr>\n");
        for (ColumnMeta col : columns) {
            String headerText = col.comment();
            if (headerText == null || headerText.isBlank()) {
                headerText = StringUtils.humanize(col.name());
            }
            sb.append("      <th>").append(headerText).append("</th>\n");
        }
        sb.append("    </tr>\n");
        sb.append("  </thead>\n");

        // TBODY
        sb.append("  <tbody>\n");
        sb.append("    <tr th:each=\"row : ${rows}\">\n");
        
        for (ColumnMeta col : columns) {
            String colName = col.name();
            if (isMoneyColumn(colName)) {
                // Money format
                sb.append("      <td class=\"text-end\" th:text=\"${#numbers.formatInteger(row.")
                  .append(colName)
                  .append(", 0, 'COMMA')}\"></td>\n");
            } else {
                // Default
                sb.append("      <td th:text=\"${row.")
                  .append(colName)
                  .append("}\"></td>\n");
            }
        }
        
        sb.append("    </tr>\n");
        sb.append("  </tbody>\n");
        sb.append("</table>");
        
        return sb.toString();
    }
}
