package com.quizplatform.util;

import com.quizplatform.config.DatabaseConnection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Utility to run standard SQL script files against the database using JDBC.
 */
public class DatabaseScriptRunner {

    public static void runScript(String filePath) {
        try (Connection conn = DatabaseConnection.getConnection();
             BufferedReader reader = new BufferedReader(new FileReader(filePath));
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("//") || line.startsWith("#")) {
                    continue;
                }
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    String sql = sb.toString().trim();
                    sql = sql.substring(0, sql.length() - 1).trim(); // Remove semicolon
                    if (!sql.isEmpty() && !sql.toLowerCase().startsWith("use ")) {
                        stmt.execute(sql);
                    }
                    sb.setLength(0);
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing script " + filePath + ": " + e.getMessage());
            throw new RuntimeException("Database script execution failed", e);
        }
    }

    public static void main(String[] args) {
        String script = args.length > 0 ? args[0] : "database_seed.sql";
        System.out.println("Executing SQL script: " + script);
        runScript(script);
        System.out.println("SQL script executed successfully!");
    }
}
