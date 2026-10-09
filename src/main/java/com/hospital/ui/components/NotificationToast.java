package com.hospital.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Toast dialog for user feedback messages (success, info, warning, error).
 */
public final class NotificationToast {

    private NotificationToast() {}

    public static void showSuccess(Component parent, String message) {
        showMessage(parent, "Success", message, JOptionPane.INFORMATION_MESSAGE, new Color(22, 163, 74));
    }

    public static void showError(Component parent, String message) {
        showMessage(parent, "Error", message, JOptionPane.ERROR_MESSAGE, new Color(220, 38, 38));
    }

    public static void showWarning(Component parent, String message) {
        showMessage(parent, "Warning", message, JOptionPane.WARNING_MESSAGE, new Color(217, 119, 6));
    }

    public static void showInfo(Component parent, String message) {
        showMessage(parent, "Information", message, JOptionPane.INFORMATION_MESSAGE, new Color(37, 99, 235));
    }

    public static boolean confirm(Component parent, String title, String message) {
        int opt = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return opt == JOptionPane.YES_OPTION;
    }

    private static void showMessage(Component parent, String title, String message, int messageType, Color color) {
        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }
}
