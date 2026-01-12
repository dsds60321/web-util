package com.tablegen.generator;

import com.tablegen.core.ColumnMeta;
import com.tablegen.core.StringUtils;
import java.util.List;

public class HtmlGenerator implements TableGenerator {

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
        sb.append("    <tr>\n");
        
        for (ColumnMeta col : columns) {
            sb.append("      <td><!-- ").append(col.name()).append(" --></td>\n");
        }
        
        sb.append("    </tr>\n");
        sb.append("  </tbody>\n");
        sb.append("</table>");
        
        return sb.toString();
    }
}
