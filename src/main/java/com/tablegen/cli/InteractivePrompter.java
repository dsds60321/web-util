package com.tablegen.cli;

import java.util.Scanner;

public class InteractivePrompter {

    public static Config promptForMissingInfo(Config config) {
        Config.Builder builder = new Config.Builder()
                .dbUrl(config.dbUrl())
                .user(config.user())
                .password(config.password())
                .schema(config.schema())
                .table(config.table())
                .templateType(config.templateType())
                .exclude(config.exclude())
                .only(config.only())
                .outFile(config.outFile())
                .copyToClipboard(config.copyToClipboard());

        Scanner scanner = new Scanner(System.in);

        if (isBlank(config.dbUrl())) {
            System.out.print("> Enter DB JDBC URL (e.g., jdbc:mariadb://localhost:3306/db): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) builder.dbUrl(input);
        }

        if (isBlank(config.user())) {
            System.out.print("> Enter DB User: ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) builder.user(input);
        }

        // Password can be empty, but let's ask if user is also empty or just explicitly ask
        if (config.password() == null) { 
             // Don't force password, but maybe user wants to input it? 
             // For simplicity, if not provided via args/config, we ask.
             // If user really has no password, they can just press enter.
             System.out.print("> Enter DB Password (leave empty if none): ");
             String input = scanner.nextLine().trim();
             builder.password(input);
        }

        if (isBlank(config.schema())) {
            System.out.print("> Enter Schema Name: ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) builder.schema(input);
        }

        if (isBlank(config.table())) {
            System.out.print("> Enter Table Name: ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) builder.table(input);
        }

        return builder.build();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
