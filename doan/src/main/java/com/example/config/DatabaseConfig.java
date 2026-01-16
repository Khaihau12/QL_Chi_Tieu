package com.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database Configuration Class
 * Đọc và quản lý cấu hình database từ file properties
 */
public class DatabaseConfig {
    
    private static DatabaseConfig instance;
    private Properties properties;
    
    private DatabaseConfig() {
        properties = new Properties();
        loadProperties();
    }
    
    public static DatabaseConfig getInstance() {
        if (instance == null) {
            synchronized (DatabaseConfig.class) {
                if (instance == null) {
                    instance = new DatabaseConfig();
                }
            }
        }
        return instance;
    }
    
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                System.err.println("Unable to find database.properties");
                setDefaultProperties();
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Error loading database properties: " + e.getMessage());
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/QLChiTieu?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
    }
    
    public String getUrl() {
        return properties.getProperty("db.url");
    }
    
    public String getUsername() {
        return properties.getProperty("db.username");
    }
    
    public String getPassword() {
        return properties.getProperty("db.password");
    }
    
    public int getInitialPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.initialSize", "5"));
    }
    
    public int getMaxPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.maxActive", "20"));
    }
}
