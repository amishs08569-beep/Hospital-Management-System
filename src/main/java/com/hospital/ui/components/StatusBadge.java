package com.hospital.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Modern pill badge displaying statuses (e.g. ACTIVE, SCHEDULED, PAID, CANCELLED).
 */
public class StatusBadge extends JLabel {

    private Color bgColor;
    private Color fgColor;

    public StatusBadge(String text) {
        super(text != null ? text.toUpperCase() : "");
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        setOpaque(false);
        updateColors(text);
    }

    public void setStatus(String text) {
        setText(text != null ? text.toUpperCase() : "");
        updateColors(text);
        repaint();
    }

    private void updateColors(String status) {
        if (status == null) status = "";
        String s = status.toUpperCase();

        if (s.contains("ACTIVE") || s.contains("PAID") || s.contains("COMPLETED") || s.contains("DISCHARGED") || s.contains("AVAILABLE")) {
            bgColor = new Color(220, 245, 230);
            fgColor = new Color(20, 120, 60);
        } else if (s.contains("CANCEL") || s.contains("UNPAID") || s.contains("EXPIRED") || s.contains("INACTIVE") || s.contains("OCCUPIED") || s.contains("TERMINATED") || s.contains("ABNORMAL")) {
            bgColor = new Color(254, 226, 226);
            fgColor = new Color(185, 28, 28);
        } else if (s.contains("SCHEDULED") || s.contains("PROGRESS") || s.contains("CHECKED") || s.contains("PARTIAL") || s.contains("PENDING") || s.contains("REQUESTED") || s.contains("LOW")) {
            bgColor = new Color(254, 243, 199);
            fgColor = new Color(180, 83, 9);
        } else {
            bgColor = new Color(241, 245, 249);
            fgColor = new Color(71, 85, 105);
        }
        setForeground(fgColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor != null ? bgColor : new Color(240, 240, 240));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}
