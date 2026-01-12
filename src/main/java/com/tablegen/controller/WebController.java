package com.tablegen.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class WebController {

    @Value("${app.title}")
    private String appTitle;

    @Value("${app.logo}")
    private String appLogo;

    private static final String CONFIG_FILE_NAME = "tablegen.properties";

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", appTitle);
        model.addAttribute("logo", appLogo);

        // Load defaults from tablegen.properties
        Properties props = loadProperties();
        
        String dbUrl = props.getProperty("db.url", "");
        model.addAttribute("defaultUser", props.getProperty("db.user", ""));
        model.addAttribute("defaultPass", props.getProperty("db.pass", ""));
        model.addAttribute("defaultSchema", props.getProperty("db.schema", ""));
        model.addAttribute("defaultTemplate", props.getProperty("template", "HTML"));

        // Parse DB URL to pre-fill form fields (type, host, database)
        // Format: jdbc:type://host:port/database
        if (!dbUrl.isBlank()) {
            parseAndAddUrlParts(model, dbUrl);
        }

        return "index";
    }

    private Properties loadProperties() {
        Properties props = new Properties();
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
                props.load(fis);
            } catch (IOException e) {
                // Ignore errors
                System.err.println("Warning: Failed to load config file in WebController: " + e.getMessage());
            }
        }
        return props;
    }

    private void parseAndAddUrlParts(Model model, String url) {
        // Simple Regex for standard JDBC URLs
        // jdbc:(mariadb|mysql|postgresql)://(host:port)/(database)
        // Oracle might be different: jdbc:oracle:thin:@host:port:SID
        
        try {
            if (url.contains("oracle")) {
                model.addAttribute("defaultDbType", "oracle");
                // jdbc:oracle:thin:@localhost:1521:XE
                Pattern pattern = Pattern.compile("jdbc:oracle:thin:@([^:]+:[0-9]+):(.+)");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    model.addAttribute("defaultHost", matcher.group(1));
                    model.addAttribute("defaultDatabase", matcher.group(2));
                } else {
                    // Try alternate format @//host:port/service_name
                    pattern = Pattern.compile("jdbc:oracle:thin:@//([^/]+)/(.+)");
                    matcher = pattern.matcher(url);
                    if (matcher.find()) {
                        model.addAttribute("defaultHost", matcher.group(1));
                        model.addAttribute("defaultDatabase", matcher.group(2));
                    }
                }
            } else {
                // jdbc:type://host/db
                String clean = url.replace("jdbc:", "");
                int firstColon = clean.indexOf(":");
                if (firstColon > 0) {
                    String type = clean.substring(0, firstColon); // mariadb
                    model.addAttribute("defaultDbType", type);
                    
                    String rest = clean.substring(firstColon); // ://localhost:3306/db
                    if (rest.startsWith("://")) {
                        rest = rest.substring(3); // localhost:3306/db or localhost:3306
                        
                        int slashIndex = rest.indexOf("/");
                        if (slashIndex > 0) {
                            String host = rest.substring(0, slashIndex);
                            String db = rest.substring(slashIndex + 1);
                            
                            // Remove query params if any
                            int qMark = db.indexOf("?");
                            if (qMark > 0) {
                                db = db.substring(0, qMark);
                            }
                            
                            model.addAttribute("defaultHost", host);
                            
                            // Only set schema from URL if not already set from properties
                            if (model.getAttribute("defaultSchema") == null || ((String)model.getAttribute("defaultSchema")).isBlank()) {
                                model.addAttribute("defaultSchema", db);
                            }
                        } else {
                            // No database specified (e.g. jdbc:mariadb://localhost:3306)
                            // The whole rest is the host
                            model.addAttribute("defaultHost", rest);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // parsing fail, ignore
            System.err.println("Error parsing DB URL: " + e.getMessage());
        }
    }
}
