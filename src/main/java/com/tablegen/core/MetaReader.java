package com.tablegen.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetaReader {

    private final Connection connection;

    public MetaReader(Connection connection) {
        this.connection = connection;
    }

    public List<ColumnMeta> getColumns(String schema, String table) throws SQLException {
        // Handle "schema.table" notation in table parameter
        if (table.contains(".")) {
            String[] parts = table.split("\\.");
            if (parts.length == 2) {
                schema = parts[0];
                table = parts[1];
            }
        }

        String dbProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
        List<ColumnMeta> columns = new ArrayList<>();
        String sql;

        if (dbProductName.contains("oracle")) {
            sql = """
                SELECT 
                    t.COLUMN_NAME, 
                    t.DATA_TYPE as COLUMN_TYPE, 
                    t.NULLABLE as IS_NULLABLE, 
                    '' as COLUMN_KEY, 
                    c.COMMENTS as COLUMN_COMMENT, 
                    t.COLUMN_ID as ORDINAL_POSITION
                FROM ALL_TAB_COLUMNS t
                LEFT JOIN ALL_COL_COMMENTS c 
                    ON t.OWNER = c.OWNER 
                    AND t.TABLE_NAME = c.TABLE_NAME 
                    AND t.COLUMN_NAME = c.COLUMN_NAME
                WHERE t.OWNER = ? AND t.TABLE_NAME = ?
                ORDER BY t.COLUMN_ID
            """;
        } else if (dbProductName.contains("postgresql")) {
            sql = """
                SELECT 
                    column_name as COLUMN_NAME, 
                    udt_name as COLUMN_TYPE, 
                    is_nullable as IS_NULLABLE, 
                    '' as COLUMN_KEY, 
                    '' as COLUMN_COMMENT, 
                    ordinal_position as ORDINAL_POSITION
                FROM information_schema.columns 
                WHERE table_schema = ? AND table_name = ? 
                ORDER BY ordinal_position
            """;
        } else {
            // MySQL, MariaDB (Standard with MySQL extensions like COLUMN_TYPE)
            sql = """
                SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT, ORDINAL_POSITION
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
            """;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, table);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String isNullable = rs.getString("IS_NULLABLE"); // Y/N or YES/NO
                    boolean nullable = "YES".equalsIgnoreCase(isNullable) || "Y".equalsIgnoreCase(isNullable);
                    
                    columns.add(new ColumnMeta(
                        rs.getString("COLUMN_NAME"),
                        rs.getString("COLUMN_TYPE"),
                        nullable,
                        "PRI".equalsIgnoreCase(rs.getString("COLUMN_KEY")), // Oracle doesn't return this easily here, might need adjustment if PK is critical
                        rs.getString("COLUMN_COMMENT"),
                        rs.getInt("ORDINAL_POSITION")
                    ));
                }
            }
        }
        return columns;
    }

    public List<ColumnMeta> getColumnsFromQuery(String userQuery, String defaultSchema) throws SQLException {
        List<ColumnMeta> columns = new ArrayList<>();
        // Wrap query to avoid fetching data
        String wrapperSql = "SELECT * FROM (" + userQuery + ") AS _wrapper_table WHERE 1=0";

        // 1. Parse potential table names from query (Heuristic)
        // Matches "FROM table", "JOIN table", "FROM schema.table", "JOIN schema.table"
        // Also handles simple aliases like "FROM table A" (captures "table")
        java.util.Set<String> potentialTables = new java.util.HashSet<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)(?:FROM|JOIN)\\s+([a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)?)");
        java.util.regex.Matcher matcher = pattern.matcher(userQuery);
        
        while (matcher.find()) {
            potentialTables.add(matcher.group(1));
        }

        // 2. Fetch all comments for these tables
        // Map<ColumnName, Comment> - Last one wins for duplicate column names
        java.util.Map<String, String> columnComments = new java.util.HashMap<>();
        
        if (!potentialTables.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE ");
            
            List<String> conditions = new ArrayList<>();
            List<String> params = new ArrayList<>();
            
            for (String tableRef : potentialTables) {
                String schema = defaultSchema;
                String table = tableRef;
                
                if (tableRef.contains(".")) {
                    String[] parts = tableRef.split("\\.");
                    schema = parts[0];
                    table = parts[1];
                }
                
                if (schema != null && !schema.isEmpty()) {
                    conditions.add("(TABLE_SCHEMA = ? AND TABLE_NAME = ?)");
                    params.add(schema);
                    params.add(table);
                }
            }
            
            if (!conditions.isEmpty()) {
                sb.append(String.join(" OR ", conditions));
                try (PreparedStatement ps = connection.prepareStatement(sb.toString())) {
                    int idx = 1;
                    for (String p : params) {
                        ps.setString(idx++, p);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String c = rs.getString("COLUMN_NAME");
                            String cmt = rs.getString("COLUMN_COMMENT");
                            if (cmt != null && !cmt.isBlank()) {
                                columnComments.put(c, cmt);
                            }
                        }
                    }
                } catch (SQLException ignored) {
                    // Ignore errors in heuristic comment fetching
                }
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(wrapperSql);
             ResultSet rs = ps.executeQuery()) {
            
            java.sql.ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            
            for (int i = 1; i <= count; i++) {
                String label = meta.getColumnLabel(i); // Alias or Name
                String originName = meta.getColumnName(i); // Original Name
                String type = meta.getColumnTypeName(i);
                int nullable = meta.isNullable(i);
                
                String comment = "";
                // If originName is available and we have a comment for it, use it.
                if (originName != null) {
                    // 1. Try direct match
                    if (columnComments.containsKey(originName)) {
                        comment = columnComments.get(originName);
                    } 
                    // 2. If not found, it might be an Alias. Try to reverse-lookup the original name from the query.
                    else {
                        try {
                            // Regex to find ".originalCol AS alias" or ".originalCol alias"
                            // Captures group 1: originalCol
                            String regex = "\\.\\s*([a-zA-Z0-9_]+)\\s+(?:AS\\s+)?\\b" + java.util.regex.Pattern.quote(originName) + "\\b";
                            java.util.regex.Pattern aliasPattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher aliasMatcher = aliasPattern.matcher(userQuery);
                            
                            if (aliasMatcher.find()) {
                                String realOriginName = aliasMatcher.group(1);
                                if (columnComments.containsKey(realOriginName)) {
                                    comment = columnComments.get(realOriginName);
                                }
                            }
                        } catch (Exception ignored) {
                            // Regex parsing failed, fallback to empty comment
                        }
                    }
                }
                
                columns.add(new ColumnMeta(
                    label,
                    type,
                    nullable == java.sql.ResultSetMetaData.columnNullable,
                    false, 
                    comment,    
                    i
                ));
            }
        }
        return columns;
    }

    public List<ColumnMeta> getColumnsFromQuery(String userQuery) throws SQLException {
        return getColumnsFromQuery(userQuery, null);
    }

    /**
     * Loads a map of column names to their comments for the entire schema.
     * If a column name appears in multiple tables (e.g. 'id', 'created_at'), it is excluded (value set to null) to avoid ambiguity.
     */
    public java.util.Map<String, String> getUniqueColumnComments(String schema) throws SQLException {
        java.util.Map<String, String> commentMap = new java.util.HashMap<>();
        java.util.Set<String> duplicateKeys = new java.util.HashSet<>();

        String sql = "SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String comment = rs.getString("COLUMN_COMMENT");
                    
                    if (comment == null || comment.isBlank()) continue;

                    // If we already saw this column name, mark it as duplicate
                    if (commentMap.containsKey(colName) || duplicateKeys.contains(colName)) {
                        duplicateKeys.add(colName);
                        commentMap.remove(colName); // Remove from valid map
                    } else {
                        commentMap.put(colName, comment);
                    }
                }
            }
        }
        return commentMap;
    }
}
