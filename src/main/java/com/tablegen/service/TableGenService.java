package com.tablegen.service;

import com.tablegen.core.ColumnMeta;
import com.tablegen.core.MetaReader;
import com.tablegen.dto.GenerateRequest;
import com.tablegen.dto.GenerateResponse;
import com.tablegen.generator.HtmlGenerator;
import com.tablegen.generator.MustacheGenerator;
import com.tablegen.generator.TableGenerator;
import com.tablegen.generator.ThymeleafGenerator;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TableGenService {

    public GenerateResponse generate(GenerateRequest req) throws Exception {
        // 1. Validation
        boolean isQueryMode = req.getSqlQuery() != null && !req.getSqlQuery().isBlank();

        if (req.getDbUrl() == null || req.getUser() == null) {
            throw new IllegalArgumentException("Missing required fields: dbUrl, user");
        }
        
        if (!isQueryMode && req.getTable() == null) {
             throw new IllegalArgumentException("Missing required fields for Table Mode: table");
        }

        // 2. Connect
        try (Connection conn = DriverManager.getConnection(req.getDbUrl(), req.getUser(), req.getPassword())) {
            
            // 3. Read Metadata
            MetaReader reader = new MetaReader(conn);
            List<ColumnMeta> allColumns;
            
            if (isQueryMode) {
                allColumns = reader.getColumnsFromQuery(req.getSqlQuery(), req.getSchema());
                
                // In Query Mode, MetaReader now tries to fetch comments from origin tables.
                // If comment is present, it means it's from the DB.
                // If comment is empty, we fallback to using the Name (Alias) as the effective comment/header.
                allColumns = allColumns.stream()
                    .map(c -> {
                        String effectiveComment = c.comment();
                        
                        if (effectiveComment == null || effectiveComment.isBlank()) {
                            effectiveComment = c.name(); // Fallback to Alias
                        }
                        
                        return new ColumnMeta(
                            c.name(), 
                            c.type(), 
                            c.isNullable(), 
                            c.isKey(), 
                            effectiveComment, 
                            c.ordinalPosition()
                        );
                    })
                    .collect(Collectors.toList());
            } else {
                allColumns = reader.getColumns(req.getSchema(), req.getTable());
            }

            if (allColumns.isEmpty()) {
                throw new RuntimeException("No columns found.");
            }

            // 4. Filter
            List<ColumnMeta> filteredColumns = filterColumns(allColumns, req.getExclude(), req.getOnly());

            // 5. Generate
            TableGenerator generator = switch (req.getTemplateType()) {
                case THYMELEAF -> new ThymeleafGenerator();
                case MUSTACHE -> new MustacheGenerator();
                case HTML -> new HtmlGenerator();
                default -> new HtmlGenerator();
            };

            String output = generator.generate(filteredColumns);
            
            String tableName = req.getTable();
            if (tableName == null || tableName.isBlank()) {
                tableName = "custom_query_result";
            }
            
            return new GenerateResponse(tableName, output, filteredColumns.size());
        }
    }

    private List<ColumnMeta> filterColumns(List<ColumnMeta> columns, Set<String> exclude, Set<String> only) {
        return columns.stream()
                .filter(col -> {
                    // "only" takes precedence
                    if (only != null && !only.isEmpty()) {
                        return only.contains(col.name());
                    }
                    // otherwise check "exclude"
                    if (exclude != null && exclude.contains(col.name())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}
