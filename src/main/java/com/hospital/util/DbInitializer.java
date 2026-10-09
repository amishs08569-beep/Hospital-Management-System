package com.hospital.util;

import com.hospital.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initializes database schemas and initial data.
 */
public final class DbInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DbInitializer.class);

    private DbInitializer() {}

    public static void initializeDatabase() {
        logger.info("Initializing database...");
        DatabaseConfig.initialize();

        // Check if users table exists and has records
        boolean needsSeed = false;
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // If we are on MySQL, execute schema if not present
            if (!DatabaseConfig.isH2Fallback()) {
                DatabaseConfig.executeSqlScript("sql/schema.sql");
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count == 0) {
                        needsSeed = true;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Users table check encountered issue: {}. Executing schema and sample data...", e.getMessage());
            needsSeed = true;
        }

        if (needsSeed) {
            logger.info("Executing sample seed dataset...");
            try {
                DatabaseConfig.executeSqlScript("sql/sample_data.sql");
            } catch (Exception e) {
                logger.warn("Notice during sample data initialization: {}", e.getMessage());
            }
        }
        logger.info("Database initialization complete.");
    }
}
