package ecobite.ui;

import javax.swing.*;
import java.awt.*;

final class UiStyle {
    private static final Color BACKGROUND = UITheme.BACKGROUND;
    private static final Color SURFACE = UITheme.SURFACE;
    private static final Color BRAND = UITheme.BRAND;
    private static final Color ACCENT = UITheme.BRAND;
    private static final Color MUTED = UITheme.MUTED;
    private static final Color GRID = UITheme.GRID;

    private UiStyle() {
    }

    static void prepare() {
        UITheme.prepare();
    }

    static JPanel createHeader(String section) {
        return createHeader(section, "Food Waste Rescue System");
    }

    static JPanel createHeader(String section, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GRID),
                BorderFactory.createEmptyBorder(14, 22, 12, 22)));
        JPanel brandBlock = new JPanel();
        brandBlock.setOpaque(false);
        brandBlock.setLayout(new BoxLayout(brandBlock, BoxLayout.Y_AXIS));
        JLabel brandLabel = new JLabel("<html>EcoBite - A Two Tier<br>Food Rescue Project</html>");
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        brandLabel.setForeground(BRAND);
        JLabel subtitleLabel = new JLabel("Food Waste Rescue System");
        subtitleLabel.setFont(UITheme.SMALL_FONT);
        subtitleLabel.setForeground(MUTED);
        brandBlock.add(brandLabel);
        brandBlock.add(subtitleLabel);

        JPanel sectionBlock = new JPanel();
        sectionBlock.setOpaque(false);
        sectionBlock.setLayout(new BoxLayout(sectionBlock, BoxLayout.Y_AXIS));
        sectionBlock.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel sectionLabel = new JLabel(section);
        sectionLabel.setFont(UITheme.SECTION_FONT);
        sectionLabel.setForeground(ACCENT);
        sectionLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel descriptionLabel = new JLabel(subtitle);
        descriptionLabel.setFont(UITheme.SMALL_FONT);
        descriptionLabel.setForeground(MUTED);
        descriptionLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        sectionBlock.add(sectionLabel);
        if (subtitle != null && !subtitle.isBlank()) {
            sectionBlock.add(Box.createVerticalStrut(3));
            sectionBlock.add(descriptionLabel);
        }

        header.setBackground(BACKGROUND);
        header.add(brandBlock, BorderLayout.WEST);
        header.add(sectionBlock, BorderLayout.EAST);
        return header;
    }

    static void styleFrame(JFrame frame) {
        frame.getContentPane().setBackground(BACKGROUND);
        styleComponentTree(frame.getContentPane());
    }

    static void styleComponentTree(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button) {
                styleButton(button);
            } else if (component instanceof JTable table) {
                styleTable(table);
            } else if (component instanceof JTextField textField) {
                UITheme.styleTextField(textField);
            } else if (component instanceof JComboBox<?> comboBox) {
                UITheme.styleComboBox(comboBox);
            } else if (component instanceof JPanel panel && panel.isOpaque()) {
                panel.setBackground(panel.getBorder() == null ? BACKGROUND : SURFACE);
            } else if (component instanceof JScrollPane scrollPane) {
                UITheme.styleScrollPane(scrollPane);
            }
            if (component instanceof Container childContainer) {
                styleComponentTree(childContainer);
            }
        }
    }

    private static void styleButton(JButton button) {
        boolean primaryAction = isPrimaryAction(button);
        UITheme.styleButton(button, primaryAction);
        Dimension preferred = button.getPreferredSize();
        button.setPreferredSize(new Dimension(Math.max(126, preferred.width), primaryAction ? 44 : 38));
    }

    private static boolean isPrimaryAction(JButton button) {
        return switch (button.getText()) {
            case "LOGIN", "SAVE FOOD", "REQUEST FOOD", "ASSIGN TO SHELTER", "APPROVE", "COMPLETE" -> true;
            default -> false;
        };
    }

    private static void styleTable(JTable table) {
        UITheme.styleTable(table);
    }
}
