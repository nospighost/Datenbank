package de.Main.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final String CONFIG_FOLDER = "config/";

    public static void createConfig(String fileName, String defaultConfig) {
        try {
            File folder = new File(CONFIG_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(CONFIG_FOLDER + fileName);
            if (!file.exists()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(defaultConfig);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadConfig(String fileName) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(CONFIG_FOLDER + fileName));
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveConfig(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FOLDER + fileName))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void deleteConfig(String fileName) {
        File file = new File(CONFIG_FOLDER + fileName);
        if (file.exists()) {
            if (!file.delete()) {
                System.err.println("Failed to delete config file: " + fileName);
            }
        } else {
            System.err.println("Config file does not exist: " + fileName);
        }
    }
    public static Map<String, String> getConfig(String fileName) {
        Map<String, String> configMap = new HashMap<>();
        String content = loadConfig(fileName);

        if (content == null) return configMap;

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                configMap.put(key, value);
            }
        }
        return configMap;
    }
}
