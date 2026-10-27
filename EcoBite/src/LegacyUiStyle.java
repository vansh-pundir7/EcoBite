import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class LegacyUiStyle {
    static final String APP_NAME = "EcoBite - A Two Tier Food Rescue Project";

    static final Color BACKGROUND = new Color(13, 24, 21);
    static final Color SURFACE = new Color(24, 43, 36);
    static final Color SURFACE_RAISED = new Color(31, 54, 45);
    static final Color BRAND = new Color(56, 210, 137);
    static final Color TEXT = new Color(239, 247, 242);
    static final Color MUTED = new Color(163, 184, 171);
    static final Color BORDER = new Color(58, 83, 70);
    static final Color GRID = new Color(43, 67, 55);
    static final Color DANGER = new Color(220, 105, 96);
    static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 14);
    static final Font HEADING = new Font("SansSerif", Font.BOLD, 22);

    private LegacyUiStyle() {
    }

    static void prepare() {
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY_BOLD);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.font", BODY);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.font", BODY_BOLD);
        UIManager.put("Button.background", BRAND);
        UIManager.put("Button.foreground", BACKGROUND);
        UIManager.put("TextField.font", BODY);
        UIManager.put("TextField.background", new Color(17, 32, 27));
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("PasswordField.font", BODY);
        UIManager.put("PasswordField.background", new Color(17, 32, 27));
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("Table.font", BODY);
        UIManager.put("TableHeader.font", BODY_BOLD);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", new Color(25, 137, 91));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", SURFACE_RAISED);
        UIManager.put("TableHeader.foreground", BRAND);
    }

    static JPanel createHeader(String section) {
        return createHeader(section, null);
    }

    /** Creates the shared header with an optional signed-in username. */
    static JPanel createHeader(String section, String username) {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(16, 22, 16, 22)));

        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel("E", SwingConstants.CENTER);
        mark.setFont(new Font("SansSerif", Font.BOLD, 22));
        mark.setForeground(BACKGROUND);
        mark.setOpaque(true);
        mark.setBackground(BRAND);
        mark.setPreferredSize(new Dimension(40, 40));
        preserveLabelStyle(mark);
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("EcoBite");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT);
        preserveLabelStyle(title);
        JLabel tagline = new JLabel("FOOD RESCUE | LESS WASTE");
        tagline.setFont(new Font("SansSerif", Font.BOLD, 10));
        tagline.setForeground(BRAND);
        preserveLabelStyle(tagline);
        brandText.add(title);
        brandText.add(tagline);
        brand.add(mark, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);

        JPanel pageBlock = new JPanel();
        pageBlock.setOpaque(false);
        pageBlock.setLayout(new BoxLayout(pageBlock, BoxLayout.Y_AXIS));
        JLabel page = new JLabel(section);
        page.setFont(new Font("SansSerif", Font.BOLD, 17));
        page.setForeground(TEXT);
        page.setAlignmentX(Component.RIGHT_ALIGNMENT);
        preserveLabelStyle(page);
        pageBlock.add(page);
        if (username != null && !username.isBlank()) {
            JLabel welcome = new JLabel("Welcome, " + username);
            welcome.setFont(BODY);
            welcome.setForeground(BRAND);
            welcome.setAlignmentX(Component.RIGHT_ALIGNMENT);
            preserveLabelStyle(welcome);
            pageBlock.add(welcome);
        }

        header.add(brand, BorderLayout.WEST);
        header.add(pageBlock, BorderLayout.EAST);
        return header;
    }

    /** Creates a shared title and short description for page content. */
    static JPanel createIntro(String title, String subtitle) {
        JPanel intro = new JPanel(new BorderLayout(0, 5));
        intro.setOpaque(false);
        intro.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        JLabel heading = new JLabel(title);
        heading.setFont(HEADING);
        heading.setForeground(TEXT);
        preserveLabelStyle(heading);
        JLabel detail = new JLabel(subtitle);
        detail.setFont(BODY);
        detail.setForeground(MUTED);
        preserveLabelStyle(detail);
        intro.add(heading, BorderLayout.NORTH);
        intro.add(detail, BorderLayout.CENTER);
        return intro;
    }

    /** Keeps a label's chosen font and color when the frame is styled. */
    static void preserveLabelStyle(JLabel label) {
        label.putClientProperty("ecobite.keepStyle", Boolean.TRUE);
    }

    /** Marks a button as primary or destructive before shared styling is applied. */
    static void setButtonStyle(JButton button, String variant) {
        button.putClientProperty("ecobite.buttonStyle", variant);
    }

    static void styleFrame(JFrame frame) {
        frame.setTitle(APP_NAME);
        frame.getContentPane().setBackground(BACKGROUND);
        styleComponentTree(frame.getContentPane());
    }

    private static void styleComponentTree(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel panel && panel.isOpaque()) {
                panel.setBackground(SURFACE);
            } else if (component instanceof JLabel label
                    && label.getClientProperty("ecobite.keepStyle") == null) {
                label.setFont(BODY);
                label.setForeground(TEXT);
            } else if (component instanceof JButton button) {
                styleButton(button);
            } else if (component instanceof JTable table) {
                styleTable(table);
            } else if (component instanceof JTextField field) {
                field.setFont(BODY);
                field.setForeground(TEXT);
                field.setBackground(new Color(17, 32, 27));
                field.setCaretColor(BRAND);
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(7, 9, 7, 9)));
            } else if (component instanceof JComboBox<?> comboBox) {
                comboBox.setFont(BODY);
                comboBox.setForeground(TEXT);
                comboBox.setBackground(new Color(17, 32, 27));
            } else if (component instanceof JScrollPane scrollPane) {
                scrollPane.setBorder(BorderFactory.createLineBorder(GRID));
                scrollPane.getViewport().setBackground(SURFACE);
            }
            if (component instanceof Container childContainer) {
                styleComponentTree(childContainer);
            }
        }
    }

    /** Applies shared sizing, color, and hover behavior to a button. */
    private static void styleButton(JButton button) {
        String variant = (String) button.getClientProperty("ecobite.buttonStyle");
        Color background = "danger".equals(variant) ? new Color(86, 43, 40)
                : "primary".equals(variant) ? BRAND : SURFACE_RAISED;
        Color foreground = "danger".equals(variant) ? new Color(255, 175, 165)
                : "primary".equals(variant) ? BACKGROUND : TEXT;
        Color hover = "danger".equals(variant) ? new Color(112, 52, 48)
                : "primary".equals(variant) ? new Color(112, 229, 164) : new Color(43, 72, 57);
        button.setFont(BODY_BOLD);
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(166, 42));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(background);
            }
        });
    }

    private static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setGridColor(GRID);
        table.setRowHeight(34);
        table.setSelectionBackground(new Color(25, 137, 91));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        JTableHeader header = table.getTableHeader();
        header.setFont(BODY_BOLD);
        header.setForeground(BRAND);
        header.setBackground(SURFACE_RAISED);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                                                          boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                label.setFont(BODY_BOLD);
                label.setForeground(BRAND);
                label.setBackground(SURFACE_RAISED);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return label;
            }
        });
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                                                          boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                label.setBackground(selected ? new Color(25, 137, 91)
                        : row % 2 == 0 ? SURFACE : BACKGROUND);
                Color status = statusColor(value);
                label.setForeground(selected ? Color.WHITE
                        : "status".equalsIgnoreCase(source.getColumnName(column)) && status != null
                        ? status : TEXT);
                label.setHorizontalAlignment(value instanceof Number
                        ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return label;
            }
        });
        for (int column = 0; column < table.getColumnModel().getColumnCount(); column++) {
            String name = table.getColumnName(column).toLowerCase();
            int width = name.equals("id") || name.equals("req id") ? 60
                    : name.contains("qty") ? 95
                    : name.contains("phone") ? 125
                    : name.equals("status") ? 140
                    : name.contains("time") ? 145 : 125;
            table.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }

    /** Returns a readable color for the known food and request statuses. */
    private static Color statusColor(Object value) {
        if (value == null) {
            return null;
        }
        return switch (value.toString().toUpperCase()) {
            case "AVAILABLE" -> new Color(90, 215, 130);
            case "REQUESTED", "PENDING" -> new Color(100, 175, 245);
            case "ANIMAL_REVIEW" -> new Color(245, 175, 80);
            case "COMPLETED" -> new Color(70, 205, 175);
            case "DISCARDED", "REJECTED" -> DANGER;
            default -> null;
        };
    }
}
