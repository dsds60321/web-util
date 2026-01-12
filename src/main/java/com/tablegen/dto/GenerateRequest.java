package com.tablegen.dto;

import com.tablegen.generator.TemplateType;
import java.util.Set;

public class GenerateRequest {
    private String dbUrl;
    private String user;
    private String password;
    private String schema;
    private String table;
    private String sqlQuery; // For Custom SQL (JOINs)
    private TemplateType templateType = TemplateType.HTML; // Default
    private Set<String> exclude;
    private Set<String> only;

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    public Set<String> getExclude() {
        return exclude;
    }

    public void setExclude(Set<String> exclude) {
        this.exclude = exclude;
    }

    public Set<String> getOnly() {
        return only;
    }

    public void setOnly(Set<String> only) {
        this.only = only;
    }
}