package com.hospital.config;

import com.hospital.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages JDBC connection pooling with HikariCP.
 * Supports MySQL as primary database with automated H2 fallback for zero-config testing and demo mode.
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static volatile HikariDataSource dataSource;
    private static volatile boolean isUsingH2 = false;

    private DatabaseConfig() {}

    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        String mode = AppConfig.getDbMode();
        boolean tryMySQL = !"h2".equalsIgnoreCase(mode);

        if (tryMySQL) {
            try {
                logger.info("Attempting connection to MySQL database at {} with user {}", AppConfig.getDbUrl(), AppConfig.getDbUser());
                HikariConfig config = new HikariConfig();
                config.setDriverClassName(AppConfig.getDbDriver());
                config.setJdbcUrl(AppConfig.getDbUrl());
                config.setUsername(AppConfig.getDbUser());
                config.setPassword(AppConfig.getDbPassword());
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(6000); // 6 seconds timeout before falling back
                config.setPoolName("Hospital-MySQL-Pool");

                HikariDataSource ds = new HikariDataSource(config);
                // Test connection
                try (Connection conn = ds.getConnection()) {
                    logger.info("Successfully connected to MySQL database: {}", conn.getMetaData().getDatabaseProductName());
                }
                dataSource = ds;
                isUsingH2 = false;
                return;
            } catch (Exception e) {
                logger.warn("Could not connect to MySQL ({}). Checking fallback mode...", e.getMessage());
                if (!"auto".equalsIgnoreCase(mode)) {
                    throw new DatabaseException("Failed to connect to MySQL database in strict mode: " + e.getMessage(), e);
                }
            }
        }

        // Initialize Embedded H2 in MySQL Compatibility Mode
        initializeH2();
    }

    public static synchronized void initializeH2() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
            logger.info("Initializing in-memory database (H2 MySQL mode) for automated tests / zero-config demo...");
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.h2.Driver");
            config.setJdbcUrl(AppConfig.getH2Url());
            config.setUsername(AppConfig.getH2User());
            config.setPassword(AppConfig.getH2Password());
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setPoolName("Hospital-H2-Pool");

            dataSource = new HikariDataSource(config);
            isUsingH2 = true;
            logger.info("In-memory database initialized successfully.");

            // Run schema and sample data on H2
            executeSqlScript("sql/schema.sql");
            executeSqlScript("sql/sample_data.sql");
        } catch (Exception e) {
            logger.error("Failed to initialize database: {}", e.getMessage(), e);
            throw new DatabaseException("Database initialization failure: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public static boolean isH2Fallback() {
        return isUsingH2;
    }

    public static synchronized void reconfigure(String url, String user, String password) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(AppConfig.getDbDriver());
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(8000);
        config.setPoolName("Hospital-Custom-Pool");

        HikariDataSource testDs = new HikariDataSource(config);
        try (Connection conn = testDs.getConnection()) {
            logger.info("Verified connection to: {}", conn.getMetaData().getDatabaseProductName());
        }

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        dataSource = testDs;
        isUsingH2 = url.contains(":h2:");
    }

    /**
     * Executes an SQL script from classpath.
     */
    public static void executeSqlScript(String resourcePath) {
        logger.info("Executing database script: {}", resourcePath);
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                logger.error("Script not found on classpath: {}", resourcePath);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                 Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                StringBuilder currentStmt = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    // Strip inline comments
                    String effectiveLine = line;
                    int commentIndex = effectiveLine.indexOf("--");
                    if (commentIndex >= 0) {
                        effectiveLine = effectiveLine.substring(0, commentIndex);
                    }
                    int slashCommentIndex = effectiveLine.indexOf("//");
                    if (slashCommentIndex >= 0) {
                        effectiveLine = effectiveLine.substring(0, slashCommentIndex);
                    }
                    String trimmed = effectiveLine.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    // Skip database creation and USE statements for H2
                    if (isUsingH2 && (trimmed.toUpperCase().startsWith("CREATE DATABASE") || trimmed.toUpperCase().startsWith("USE "))) {
                        continue;
                    }
                    currentStmt.append(effectiveLine).append("\n");
                    if (trimmed.endsWith(";")) {
                        String sql = currentStmt.toString().trim();
                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length() - 1).trim();
                        }
                        if (isUsingH2) {
                            sql = sql.replaceAll("(?i)\\s*ENGINE\\s*=\\s*[A-Za-z0-9_]+", "");
                            sql = sql.replaceAll("(?i)\\s*ON\\s+UPDATE\\s+CURRENT_TIMESTAMP", "");
                        }
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (SQLException ex) {
                                // Ignore harmless "already exists" or duplicate key in seed script
                                if (!ex.getMessage().toLowerCase().contains("already exists") &&
                                    !ex.getMessage().toLowerCase().contains("duplicate")) {
                                    logger.warn("Script execution notice [{}]: {}", sql, ex.getMessage());
                                }
                            }
                        }
                        currentStmt.setLength(0);
                    }
                }
                logger.info("Successfully finished executing {}", resourcePath);
            }
        } catch (Exception e) {
            logger.error("Error executing script {}: {}", resourcePath, e.getMessage());
            throw new DatabaseException("Script execution error for " + resourcePath + ": " + e.getMessage(), e);
        }
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing database connection pool...");
            dataSource.close();
            dataSource = null;
        }
    }
}
