package com.hospital.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Common modern styling utilities for Swing UI components.
 */
public final class UIHelper {

    public static final Color PRIMARY_COLOR = new Color(37, 99, 235);     // Royal Blue
    public static final Color PRIMARY_DARK = new Color(30, 64, 175);      // Deep Blue
    public static final Color PRIMARY_LIGHT = new Color(239, 246, 255);    // Soft Blue tint
    public static final Color BACKGROUND = new Color(248, 250, 252);       // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);           // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);       // Slate 500
    public static final Color BORDER_COLOR = new Color(226, 232, 240);     // Slate 200
    public static final Color SUCCESS_COLOR = new Color(22, 163, 74);
    public static final Color DANGER_COLOR = new Color(220, 38, 38);
    public static final Color WARNING_COLOR = new Color(217, 119, 6);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private UIHelper() {}

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Alternate row colors renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(TEXT_DARK);
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });
    }

    public static JPanel createHeaderPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_SUBTITLE);
        lblSub.setForeground(TEXT_MUTED);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblSub);

        panel.add(textPanel, BorderLayout.WEST);
        return panel;
    }

    public static JScrollPane createModernScrollPane(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    public static JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_DARK);
        label.setPreferredSize(new Dimension(140, 32));

        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }
}
