package com.quizplatform.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to manage JDBC database connections for the application.
 * Reads database credentials dynamically from classpath application.properties.
 * 
 * Strict academic design: Uses standard java.sql.DriverManager without
 * third-party connection pools or ORM frameworks.
 */
public class DatabaseConnection {

    private static final String PROPERTIES_FILE = "application.properties";
    private static final Properties properties = new Properties();
    private static boolean isInitialized = false;

    // Demonstrates explicit JDBC Driver loading using Class.forName
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: MySQL Connector/J Driver not found on classpath: " + e.getMessage());
        }
    }

    // Private constructor enforces Singleton / Utility pattern
    private DatabaseConnection() {
    }

    /**
     * Loads the database configuration properties from application.properties.
     * Demonstrates synchronization for thread-safe initialization.
     */
    private static synchronized void loadProperties() {
        if (isInitialized) {
            return;
        }

        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file '" + PROPERTIES_FILE + "' not found in classpath.");
            }
            properties.load(input);
            isInitialized = true;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load database configuration from " + PROPERTIES_FILE, ex);
        }
    }

    /**
     * Establishes and returns a new JDBC Connection to the MySQL database.
     * 
     * @return an open {@link Connection} to the database.
     * @throws SQLException if a database access error occurs or credentials are invalid.
     */
    public static Connection getConnection() throws SQLException {
        if (!isInitialized) {
            loadProperties();
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        if (url == null || username == null) {
            throw new SQLException("Incomplete database configuration: db.url and db.username must be provided in " + PROPERTIES_FILE);
        }

        return DriverManager.getConnection(url, username, password != null ? password : "");
    }
}
