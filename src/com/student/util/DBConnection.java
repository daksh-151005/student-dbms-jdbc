package com.student.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to manage JDBC Database Connection for Oracle Database.
 * Reads database parameters from db.properties file.
 */
public class DBConnection {

    private static String driver = "oracle.jdbc.OracleDriver";
    private static String url = "jdbc:oracle:thin:@localhost:1521:xe";
    private static String user = "system";
    private static String password = "oracle";
    private static boolean driverLoaded = false;
    private static String lastError = null;

    static {
        loadProperties();
        loadDriver();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        try {
            if (propFile.exists()) {
                try (InputStream in = new FileInputStream(propFile)) {
                    props.load(in);
                }
            } else {
                // Try reading from classpath
                InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
                if (in != null) {
                    props.load(in);
                    in.close();
                }
            }

            if (props.getProperty("db.driver") != null) {
                driver = props.getProperty("db.driver").trim();
            }
            if (props.getProperty("db.url") != null) {
                url = props.getProperty("db.url").trim();
            }
            if (props.getProperty("db.user") != null) {
                user = props.getProperty("db.user").trim();
            }
            if (props.getProperty("db.password") != null) {
                password = props.getProperty("db.password").trim();
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Note: Using default Oracle DB configuration (" + e.getMessage() + ")");
        }
    }

    private static void loadDriver() {
        try {
            Class.forName(driver);
            driverLoaded = true;
            System.out.println("[DBConnection] Oracle JDBC Driver loaded successfully: " + driver);
        } catch (ClassNotFoundException e) {
            driverLoaded = false;
            lastError = "Oracle JDBC Driver class not found: " + driver + ". Ensure ojdbc.jar is in classpath.";
            System.err.println("[DBConnection] ERROR: " + lastError);
        }
    }

    /**
     * Establishes and returns a new JDBC Connection to Oracle Database.
     * @return Connection object
     * @throws SQLException if database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            lastError = null;
            return conn;
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw e;
        }
    }

    /**
     * Tests if Oracle database connection is working.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getLastError() {
        return lastError;
    }
}
