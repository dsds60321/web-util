package com.tablegen.cli;

import com.tablegen.generator.TemplateType;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ArgsParser {

    private static final String CONFIG_FILE_NAME = "tablegen.properties";

    public static Config parse(String[] args) {
        Config.Builder builder = new Config.Builder();
        
        // 1. Load from Config File first (if exists)
        loadFromProperties(builder);

        // 2. Parse CLI Arguments (Overrides config file)
        int startIndex = 0;
        if (args.length > 0 && !args[0].startsWith("-")) {
            // Support "generate" command but it's optional now
            if ("generate".equalsIgnoreCase(args[0])) {
                startIndex = 1;
            }
        }

        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                continue;
            }

            String key = arg.substring(2);
            
            // Handle boolean flags
            if ("copy".equals(key)) {
                builder.copyToClipboard(true);
                continue;
            }

            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for option: " + arg);
            }
            String value = args[i + 1];
            i++; // Advance

            switch (key) {
                case "db":
                    builder.dbUrl(value);
                    break;
                case "user":
                    builder.user(value);
                    break;
                case "pass":
                    builder.password(value);
                    break;
                case "schema":
                    builder.schema(value);
                    break;
                case "table":
                    builder.table(value);
                    break;
                case "template":
                    builder.templateType(TemplateType.fromString(value));
                    break;
                case "exclude":
                    builder.exclude(parseCsv(value));
                    break;
                case "only":
                    builder.only(parseCsv(value));
                    break;
                case "out":
                    builder.outFile(value);
                    break;
                default:
                    System.err.println("Warning: Unknown option ignored: " + key);
            }
        }

        return builder.build();
    }

    private static void loadFromProperties(Config.Builder builder) {
        // Priority: Current Directory -> User Home
        Path localConfig = Paths.get(CONFIG_FILE_NAME);
        Path userConfig = Paths.get(System.getProperty("user.home"), ".tablegen", CONFIG_FILE_NAME);
        Path activeConfig = null;

        if (Files.exists(localConfig)) {
            activeConfig = localConfig;
        } else if (Files.exists(userConfig)) {
            activeConfig = userConfig;
        }

        if (activeConfig != null) {
            try (FileInputStream fis = new FileInputStream(activeConfig.toFile())) {
                Properties props = new Properties();
                props.load(fis);
                
                if (props.containsKey("db.url")) builder.dbUrl(props.getProperty("db.url"));
                if (props.containsKey("db.user")) builder.user(props.getProperty("db.user"));
                if (props.containsKey("db.pass")) builder.password(props.getProperty("db.pass"));
                if (props.containsKey("db.schema")) builder.schema(props.getProperty("db.schema"));
                // We typically don't set 'table' in config as it changes often, but we can support it
                if (props.containsKey("db.table")) builder.table(props.getProperty("db.table"));
                if (props.containsKey("template")) builder.templateType(TemplateType.fromString(props.getProperty("template")));

            } catch (IOException e) {
                System.err.println("Warning: Failed to load config file: " + e.getMessage());
            }
        }
    }

    private static Set<String> parseCsv(String value) {
        if (value == null || value.isBlank()) {
            return new HashSet<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}