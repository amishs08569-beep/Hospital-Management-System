package com.hospital.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern styled button with customizable color variants (PRIMARY, SUCCESS, DANGER, SECONDARY).
 */
public class ModernButton extends JButton {

    public enum ButtonType {
        PRIMARY(new Color(37, 99, 235), new Color(29, 78, 216), Color.WHITE),
        SUCCESS(new Color(22, 163, 74), new Color(21, 128, 61), Color.WHITE),
        DANGER(new Color(220, 38, 38), new Color(185, 28, 28), Color.WHITE),
        SECONDARY(new Color(241, 245, 249), new Color(226, 232, 240), new Color(51, 65, 85));

        final Color normalColor;
        final Color hoverColor;
        final Color textColor;

        ButtonType(Color normal, Color hover, Color text) {
            this.normalColor = normal;
            this.hoverColor = hover;
            this.textColor = text;
        }
    }

    private final ButtonType type;
    private boolean isHovered = false;

    public ModernButton(String text, ButtonType type) {
        super(text);
        this.type = type != null ? type : ButtonType.PRIMARY;

        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setForeground(this.type.textColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public ModernButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!isEnabled()) {
            g2.setColor(new Color(203, 213, 225));
            setForeground(new Color(148, 163, 184));
        } else if (isHovered) {
            g2.setColor(type.hoverColor);
            setForeground(type.textColor);
        } else {
            g2.setColor(type.normalColor);
            setForeground(type.textColor);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.dispose();
        super.paintComponent(g);
    }
}
