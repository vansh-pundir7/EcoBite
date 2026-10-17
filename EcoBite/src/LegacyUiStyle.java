import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

final class LegacyUiStyle {
    static final String APP_NAME = "EcoBite - A Two Tier Food Rescue Project";

    private static final Color BACKGROUND = new Color(13, 24, 21);
    private static final Color SURFACE = new Color(24, 43, 36);
    private static final Color SURFACE_RAISED = new Color(31, 54, 45);
    private static final Color BRAND = new Color(56, 210, 137);
    private static final Color TEXT = new Color(239, 247, 242);
    private static final Color BORDER = new Color(58, 83, 70);
    private static final Color GRID = new Color(43, 67, 55);
    private static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 14);

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
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GRID),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel brand = new JLabel("<html>EcoBite - A Two Tier<br>Food Rescue Project</html>");
        brand.setFont(new Font("SansSerif", Font.BOLD, 16));
        brand.setForeground(BRAND);
        brand.putClientProperty("ecobite.brandLabel", Boolean.TRUE);

        JLabel page = new JLabel(section);
        page.setFont(new Font("SansSerif", Font.BOLD, 17));
        page.setForeground(TEXT);
        page.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(brand, BorderLayout.WEST);
        header.add(page, BorderLayout.EAST);
        return header;
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
                    && label.getClientProperty("ecobite.brandLabel") == null) {
                label.setFont(BODY);
                label.setForeground(TEXT);
            } else if (component instanceof JButton button) {
                button.setFont(BODY_BOLD);
                button.setForeground(BACKGROUND);
                button.setBackground(BRAND);
                button.setFocusPainted(false);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
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
        Border cellBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                                                          boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                label.setBorder(cellBorder);
                label.setBackground(selected ? new Color(25, 137, 91)
                        : row % 2 == 0 ? SURFACE : BACKGROUND);
                label.setForeground(selected ? Color.WHITE : TEXT);
                return label;
            }
        });
    }
}
