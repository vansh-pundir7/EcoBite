package ecobite.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public final class UITheme {
    public static final Color BACKGROUND = new Color(13, 24, 21);
    public static final Color BACKGROUND_RAISED = new Color(18, 34, 29);
    public static final Color SURFACE = new Color(24, 43, 36);
    public static final Color SURFACE_RAISED = new Color(31, 54, 45);
    public static final Color INPUT = new Color(17, 32, 27);
    public static final Color BRAND = new Color(56, 210, 137);
    public static final Color BRAND_HOVER = new Color(78, 226, 155);
    public static final Color BRAND_DARK = new Color(25, 137, 91);
    public static final Color TEXT = new Color(239, 247, 242);
    public static final Color MUTED = new Color(157, 181, 168);
    public static final Color BORDER = new Color(58, 83, 70);
    public static final Color GRID = new Color(43, 67, 55);
    public static final Color ERROR = new Color(255, 137, 132);
    public static final Color DANGER = new Color(255, 128, 120);
    public static final Color DANGER_HOVER = new Color(255, 151, 143);
    public static final Color SUCCESS = new Color(91, 220, 157);

    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BODY_BOLD_FONT = new Font("SansSerif", Font.BOLD, 14);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 38);
    public static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 22);

    public static final int SPACE_XS = 6;
    public static final int SPACE_SM = 12;
    public static final int SPACE_MD = 20;
    public static final int SPACE_LG = 32;
    public static final int CONTROL_HEIGHT = 46;
    public static final int BUTTON_HEIGHT = 48;
    public static final int CORNER_RADIUS = 18;

    private UITheme() {
    }

    public static void prepare() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.foreground", TEXT);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", BODY_FONT);
        UIManager.put("OptionPane.buttonFont", BODY_BOLD_FONT);
        UIManager.put("Label.font", BODY_FONT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.font", BODY_BOLD_FONT);
        UIManager.put("Button.background", BRAND);
        UIManager.put("Button.foreground", BACKGROUND);
        UIManager.put("TextField.font", BODY_FONT);
        UIManager.put("TextField.background", INPUT);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", BRAND);
        UIManager.put("PasswordField.font", BODY_FONT);
        UIManager.put("PasswordField.background", INPUT);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("PasswordField.caretForeground", BRAND);
        UIManager.put("ComboBox.font", BODY_FONT);
        UIManager.put("ComboBox.background", INPUT);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", BRAND_DARK);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("Table.font", BODY_FONT);
        UIManager.put("TableHeader.font", BODY_BOLD_FONT);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", BRAND_DARK);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", SURFACE_RAISED);
        UIManager.put("TableHeader.foreground", BRAND);
        UIManager.put("ScrollPane.background", BACKGROUND);
        UIManager.put("Viewport.background", SURFACE);
        UIManager.put("ScrollBar.background", SURFACE);
        UIManager.put("ScrollBar.thumb", BORDER);
        UIManager.put("ScrollBar.track", SURFACE);
        UIManager.put("TitledBorder.titleColor", BRAND);
        UIManager.put("Separator.foreground", GRID);
        UIManager.put("Button.select", BRAND_HOVER);
    }

    public static JPanel createCardPanel() {
        return new RoundedPanel(SURFACE, CORNER_RADIUS, BORDER);
    }

    public static void styleTextField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setForeground(TEXT);
        field.setBackground(INPUT);
        field.setCaretColor(BRAND);
        field.setSelectionColor(BRAND_DARK);
        field.setSelectedTextColor(Color.WHITE);
        field.setPreferredSize(new Dimension(Math.max(150, field.getPreferredSize().width), CONTROL_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTROL_HEIGHT));
        field.setBorder(new RoundedBorder(BORDER, 10, 1, 12, 12));
        if (field.getClientProperty("ecobite.focusStyle") == null) {
            field.putClientProperty("ecobite.focusStyle", Boolean.TRUE);
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    field.setBorder(new RoundedBorder(BRAND, 10, 2, 12, 12));
                }

                @Override
                public void focusLost(FocusEvent event) {
                    field.setBorder(new RoundedBorder(BORDER, 10, 1, 12, 12));
                }
            });
        }
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(BODY_FONT);
        comboBox.setForeground(TEXT);
        comboBox.setBackground(INPUT);
        comboBox.setOpaque(true);
        comboBox.setFocusable(true);
        comboBox.setPreferredSize(new Dimension(Math.max(170, comboBox.getPreferredSize().width), CONTROL_HEIGHT));
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTROL_HEIGHT));
        comboBox.setBorder(new RoundedBorder(BORDER, 10, 1, 10, 8));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setBackground(isSelected ? BRAND_DARK : INPUT);
                label.setForeground(isSelected ? Color.WHITE : TEXT);
                label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                label.setFont(BODY_FONT);
                return label;
            }
        });
        if (comboBox.getClientProperty("ecobite.focusStyle") == null) {
            comboBox.putClientProperty("ecobite.focusStyle", Boolean.TRUE);
            comboBox.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    comboBox.setBorder(new RoundedBorder(BRAND, 10, 2, 9, 7));
                }

                @Override
                public void focusLost(FocusEvent event) {
                    comboBox.setBorder(new RoundedBorder(BORDER, 10, 1, 10, 8));
                }
            });
        }
    }

    public static void styleButton(JButton button, boolean primary) {
        boolean danger = "REJECT".equals(button.getText());
        Color base = danger ? new Color(77, 43, 42) : primary ? BRAND : SURFACE_RAISED;
        Color hover = danger ? new Color(103, 53, 51) : primary ? BRAND_HOVER : new Color(43, 70, 57);
        button.setFont(BODY_BOLD_FONT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setForeground(danger ? DANGER : primary ? BACKGROUND : TEXT);
        button.setBackground(base);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new RoundedBorder(danger ? DANGER : primary ? BRAND : BORDER, 10, 1, 10, 14));
        if (button.getClientProperty("ecobite.buttonStyle") == null) {
            button.putClientProperty("ecobite.buttonStyle", Boolean.TRUE);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    if (button.isEnabled()) {
                        button.setBackground(hover);
                    }
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    button.setBackground(base);
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    if (button.isEnabled()) {
                        button.setBackground(danger ? DANGER : primary ? BRAND_DARK : BACKGROUND_RAISED);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (button.isEnabled()) {
                        button.setBackground(button.contains(event.getPoint()) ? hover : base);
                    }
                }
            });
            button.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    button.setBorder(new RoundedBorder(danger ? DANGER : BRAND, 10, 2, 9, 13));
                }

                @Override
                public void focusLost(FocusEvent event) {
                    button.setBorder(new RoundedBorder(danger ? DANGER : primary ? BRAND : BORDER, 10, 1, 10, 14));
                }
            });
        }
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setSelectionBackground(BRAND_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(GRID);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setColumnSelectionAllowed(false);
        table.setDefaultRenderer(Object.class, new DashboardCellRenderer());
        table.setDefaultRenderer(Number.class, new DashboardCellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(BODY_BOLD_FONT);
        header.setForeground(BRAND);
        header.setBackground(SURFACE_RAISED);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                                                          boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                label.setFont(BODY_BOLD_FONT);
                label.setForeground(BRAND);
                label.setBackground(SURFACE_RAISED);
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                return label;
            }
        });
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(GRID));
        scrollPane.setBackground(SURFACE);
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    }

    private static final class DashboardCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                      boolean focused, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            label.setHorizontalAlignment(value instanceof Number ? SwingConstants.CENTER : SwingConstants.LEFT);

            if (selected) {
                label.setBackground(BRAND_DARK);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(row % 2 == 0 ? SURFACE : BACKGROUND_RAISED);
                label.setForeground(statusColor(value));
            }
            return label;
        }

        private Color statusColor(Object value) {
            if (value == null) {
                return TEXT;
            }
            return switch (value.toString()) {
                case "AVAILABLE", "APPROVED", "COMPLETED" -> SUCCESS;
                case "PENDING", "ANIMAL_REVIEW" -> BRAND;
                case "REJECTED", "TIME_FINISHED" -> DANGER;
                default -> TEXT;
            };
        }
    }

    private static final class RoundedPanel extends JPanel {
        private final Color fill;
        private final Color outline;
        private final int radius;

        private RoundedPanel(Color fill, int radius, Color outline) {
            this.fill = fill;
            this.radius = radius;
            this.outline = outline;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(outline);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final int thickness;
        private final int horizontalInset;
        private final int verticalInset;

        private RoundedBorder(Color color, int radius, int thickness, int horizontalInset, int verticalInset) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
            this.horizontalInset = horizontalInset;
            this.verticalInset = verticalInset;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.setStroke(new BasicStroke(thickness));
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(verticalInset, horizontalInset, verticalInset, horizontalInset);
        }
    }
}
