package org.example.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtils {

    private static final String CONFIG_FILE = "src/main/resources/application.properties";

    public static Properties loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
        return properties;
    }

    public static String getDbUrl() {
        return loadProperties().getProperty("datasource.url");
    }

    public static String getDbUser() {
        return loadProperties().getProperty("datasource.username");
    }

    public static String getDbPassword() {
        return loadProperties().getProperty("datasource.password");
    }
}
