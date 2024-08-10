package com.nova.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Settings.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalArgumentException("Sorry, unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load configuration", ex);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

}
