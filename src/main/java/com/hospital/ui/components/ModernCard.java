package com.hospital.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Modern card panel with rounded corners, subtle border, title, value and color accents.
 */
public class ModernCard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subTextLabel;
    private Color accentColor = new Color(59, 130, 246);

    public ModernCard(String title, String initialValue, String subText, Color accentColor) {
        this.accentColor = accentColor != null ? accentColor : new Color(59, 130, 246);
        setLayout(new BorderLayout(8, 6));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(100, 116, 139));

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(15, 23, 42));

        subTextLabel = new JLabel(subText != null ? subText : "");
        subTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subTextLabel.setForeground(new Color(148, 163, 184));

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(valueLabel);
        if (subText != null && !subText.isEmpty()) {
            contentPanel.add(Box.createVerticalStrut(4));
            contentPanel.add(subTextLabel);
        }

        add(contentPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
        repaint();
    }

    public void setSubText(String subText) {
        subTextLabel.setText(subText);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // White card background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        // Subtle outer border
        g2.setColor(new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        // Left accent bar
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);

        g2.dispose();
        super.paintComponent(g);
    }
}
