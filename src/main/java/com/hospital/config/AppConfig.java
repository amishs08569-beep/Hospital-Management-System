package com.hospital.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Loads and provides access to application configuration settings.
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
                logger.info("Loaded application.properties successfully");
            } else {
                logger.warn("application.properties not found on classpath, using defaults");
            }
        } catch (IOException e) {
            logger.error("Failed to read application.properties: {}", e.getMessage());
        }
    }

    public static String getProperty(String key, String defaultValue) {
        // Environment variables take highest precedence
        String envKey = key.toUpperCase().replace('.', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }

        // System properties take second precedence
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.trim().isEmpty()) {
            return sysVal.trim();
        }

        return properties.getProperty(key, defaultValue);
    }

    public static String getDbDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/hospital_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true");
    }

    public static String getDbUser() {
        return getProperty("db.user", "root");
    }

    public static String getDbPassword() {
        return getProperty("db.password", "root");
    }

    public static String getDbMode() {
        return getProperty("db.mode", "auto");
    }

    public static String getH2Url() {
        return getProperty("db.h2.url", "jdbc:h2:mem:hospital_management;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
    }

    public static String getH2User() {
        return getProperty("db.h2.user", "sa");
    }

    public static String getH2Password() {
        return getProperty("db.h2.password", "");
    }

    public static String getHospitalName() {
        return getProperty("hospital.name", "Apex Care Multi-Specialty Hospital");
    }

    public static String getHospitalTagline() {
        return getProperty("hospital.tagline", "Excellence in Healthcare & Patient Care");
    }

    public static String getHospitalAddress() {
        return getProperty("hospital.address", "742 Evergreen Terrace, Medical District, Suite 100");
    }

    public static String getHospitalPhone() {
        return getProperty("hospital.phone", "+1 (555) 234-5678");
    }

    public static String getHospitalEmail() {
        return getProperty("hospital.email", "contact@apexcarehospital.com");
    }

    public static BigDecimal getTaxRate() {
        try {
            return new BigDecimal(getProperty("hospital.taxRate", "5.00"));
        } catch (Exception e) {
            return new BigDecimal("5.00");
        }
    }

    public static String getCurrencySymbol() {
        return getProperty("hospital.currencySymbol", "$");
    }
}
