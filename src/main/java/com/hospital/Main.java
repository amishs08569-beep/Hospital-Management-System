package com.hospital;

import com.formdev.flatlaf.FlatLightLaf;
import com.hospital.config.AppConfig;
import com.hospital.ui.LoginFrame;
import com.hospital.util.DbInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Main application entry point for the Hospital Management System.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("===============================================================");
        logger.info("Starting {}...", AppConfig.getHospitalName());
        logger.info("===============================================================");

        // Setup Modern FlatLaf Look and Feel
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            logger.info("FlatLaf Modern Look & Feel applied successfully.");
        } catch (Throwable t) {
            logger.warn("Could not load FlatLaf, falling back to system Look and Feel: {}", t.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Initialize Database & seed data
        try {
            DbInitializer.initializeDatabase();
        } catch (Exception e) {
            logger.error("Failed to initialize database: {}", e.getMessage(), e);
        }

        // Launch Login Window on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                logger.info("Hospital Management System Login Portal is now ready.");
            } catch (Exception e) {
                logger.error("Error creating login frame: {}", e.getMessage(), e);
                JOptionPane.showMessageDialog(null,
                        "Failed to launch application UI: " + e.getMessage(),
                        "Fatal Launch Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
