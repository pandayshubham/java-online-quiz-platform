package com.quizplatform;

import com.quizplatform.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC Connection Verification test utility.
 * Validates connection establishment, schema name, driver details,
 * and test query execution using standard JDBC with try-with-resources.
 * 
 * Complies with strict dependency rules (no JUnit or third-party test libraries).
 */
public class DatabaseVerificationTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("    JDBC DATABASE CONNECTION VERIFICATION");
        System.out.println("==================================================");

        boolean driverLoaded = false;
        boolean connectionEstablished = false;
        boolean catalogVerified = false;
        boolean queryVerified = false;
        String catalogName = null;

        // Verify driver class presence
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
            System.out.println("[PASS] 1. MySQL JDBC Driver found and loaded.");
        } catch (ClassNotFoundException e) {
            System.err.println("[FAIL] 1. MySQL JDBC Driver class not found: " + e.getMessage());
        }

        // Test connection and test query execution with try-with-resources
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                connectionEstablished = true;
                System.out.println("[PASS] 2. Connection successfully established.");

                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("       Database Product: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
                System.out.println("       Driver Name:      " + metaData.getDriverName() + " " + metaData.getDriverVersion());

                catalogName = conn.getCatalog();
                System.out.println("       Active Database:  " + catalogName);

                if ("quiz_platform".equalsIgnoreCase(catalogName)) {
                    catalogVerified = true;
                    System.out.println("[PASS] 3. Target database name matches 'quiz_platform'.");
                } else {
                    System.err.println("[WARN] 3. Database name is '" + catalogName + "' (expected: 'quiz_platform').");
                }

                // Execute test query: SELECT 1
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1 AS test_val")) {
                    if (rs.next() && rs.getInt("test_val") == 1) {
                        queryVerified = true;
                        System.out.println("[PASS] 4. Test query 'SELECT 1' executed successfully.");
                    } else {
                        System.err.println("[FAIL] 4. Test query 'SELECT 1' did not return expected value.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[FAIL] JDBC Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("==================================================");
        boolean allPassed = driverLoaded && connectionEstablished && catalogVerified && queryVerified;
        if (allPassed) {
            System.out.println("VERIFICATION SUMMARY: ALL CHECKS PASSED SUCCESSFULLY!");
        } else {
            System.err.println("VERIFICATION SUMMARY: ONE OR MORE CHECKS FAILED!");
            System.exit(1);
        }
        System.out.println("==================================================");
    }
}
