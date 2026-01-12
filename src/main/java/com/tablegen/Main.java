package com.tablegen;

import com.tablegen.cli.ArgsParser;
import com.tablegen.cli.Config;
import com.tablegen.cli.InteractivePrompter;
import com.tablegen.core.ColumnMeta;
import com.tablegen.core.MetaReader;
import com.tablegen.generator.HtmlGenerator;
import com.tablegen.generator.MustacheGenerator;
import com.tablegen.generator.TableGenerator;
import com.tablegen.generator.ThymeleafGenerator;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        try {
            // 1. Parse Arguments & Load Config
            Config config = ArgsParser.parse(args);

            // 2. Interactive Mode if missing required fields
            if (!isValid(config)) {
                System.out.println("--- Interactive Mode ---");
                config = InteractivePrompter.promptForMissingInfo(config);
            }

            // Final Validation
            validate(config);

            // 3. Connect to DB
            try (Connection conn = DriverManager.getConnection(config.dbUrl(), config.user(), config.password())) {
                
                // 4. Read Metadata
                MetaReader reader = new MetaReader(conn);
                List<ColumnMeta> allColumns = reader.getColumns(config.schema(), config.table());

                if (allColumns.isEmpty()) {
                    System.err.println("Error: No columns found for table " + config.schema() + "." + config.table());
                    System.exit(1);
                }
                
                System.out.println("Loaded " + allColumns.size() + " columns.");

                // 5. Filter Columns
                List<ColumnMeta> filteredColumns = filterColumns(allColumns, config);
                System.out.println("Using " + filteredColumns.size() + " columns after filtering.");

                // 6. Select Generator
                TableGenerator generator = switch (config.templateType()) {
                    case THYMELEAF -> new ThymeleafGenerator();
                    case MUSTACHE -> new MustacheGenerator();
                    case HTML -> new HtmlGenerator();
                };

                // 7. Generate
                String output = generator.generate(filteredColumns);

                // 8. Output handling
                boolean outputHandled = false;
                
                String targetFile = config.outFile();
                
                // If no output file specified, auto-generate from table name
                if (targetFile == null || targetFile.isBlank()) {
                    String extension = ".html"; // Default
                    // You could customize extension based on template type if needed
                    targetFile = config.table() + extension;
                }

                if (targetFile != null && !targetFile.isBlank()) {
                    Path path = Paths.get(targetFile);
                    Files.writeString(path, output);
                    System.out.println("Saved to " + path.toAbsolutePath());
                    outputHandled = true;
                }
                
                if (config.copyToClipboard()) {
                    try {
                        StringSelection selection = new StringSelection(output);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                        System.out.println("Output copied to clipboard!");
                        outputHandled = true;
                    } catch (java.awt.HeadlessException e) {
                        System.err.println("Warning: Cannot copy to clipboard (Headless environment).");
                    }
                }

                if (!outputHandled) {
                    System.out.println("--- Generated Output ---");
                    System.out.println(output);
                }

            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            // e.printStackTrace(); 
            System.exit(1);
        }
    }

    private static boolean isValid(Config config) {
        return config.dbUrl() != null && !config.dbUrl().isBlank() &&
               config.user() != null && !config.user().isBlank() &&
               config.schema() != null && !config.schema().isBlank() &&
               config.table() != null && !config.table().isBlank();
    }

    private static void validate(Config config) {
        if (config.dbUrl() == null || config.dbUrl().isBlank()) throw new IllegalArgumentException("Missing required option: --db (or use interactive mode)");
        if (config.user() == null || config.user().isBlank()) throw new IllegalArgumentException("Missing required option: --user");
        if (config.schema() == null || config.schema().isBlank()) throw new IllegalArgumentException("Missing required option: --schema");
        if (config.table() == null || config.table().isBlank()) throw new IllegalArgumentException("Missing required option: --table");
    }

    private static List<ColumnMeta> filterColumns(List<ColumnMeta> columns, Config config) {
        return columns.stream()
                .filter(col -> {
                    // "only" takes precedence
                    if (!config.only().isEmpty()) {
                        return config.only().contains(col.name());
                    }
                    // otherwise check "exclude"
                    if (config.exclude().contains(col.name())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}