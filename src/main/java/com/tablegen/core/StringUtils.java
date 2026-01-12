package com.tablegen.core;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtils {

    public static String humanize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Replace underscores with spaces
        String text = input.replace('_', ' ');

        // Insert space before capital letters in camelCase (e.g. "regDate" -> "reg Date")
        // But avoid doing it for acronyms if possible, though simple regex approach is usually:
        text = text.replaceAll("([a-z])([A-Z]+)", "$1 $2");

        // Capitalize words
        return Arrays.stream(text.split("\\s+"))
                .filter(s -> !s.isEmpty())
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
