package com.tablegen.cli;

import com.tablegen.generator.TemplateType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public record Config(
    String dbUrl,
    String user,
    String password,
    String schema,
    String table,
    TemplateType templateType,
    Set<String> exclude,
    Set<String> only,
    String outFile,
    boolean copyToClipboard
) {
    public static class Builder {
        private String dbUrl;
        private String user;
        private String password;
        private String schema;
        private String table;
        private TemplateType templateType = TemplateType.HTML;
        private Set<String> exclude = new HashSet<>();
        private Set<String> only = new HashSet<>();
        private String outFile;
        private boolean copyToClipboard = false;

        public Builder dbUrl(String val) { dbUrl = val; return this; }
        public Builder user(String val) { user = val; return this; }
        public Builder password(String val) { password = val; return this; }
        public Builder schema(String val) { schema = val; return this; }
        public Builder table(String val) { table = val; return this; }
        public Builder templateType(TemplateType val) { templateType = val; return this; }
        public Builder exclude(Set<String> val) { exclude = val; return this; }
        public Builder only(Set<String> val) { only = val; return this; }
        public Builder outFile(String val) { outFile = val; return this; }
        public Builder copyToClipboard(boolean val) { copyToClipboard = val; return this; }

        public Config build() {
            return new Config(dbUrl, user, password, schema, table, templateType, exclude, only, outFile, copyToClipboard);
        }
    }
}
