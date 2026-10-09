package ecobite.ui;

import ecobite.model.User;
import ecobite.service.UserService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {
    private final UserService userService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame() {
        UITheme.prepare();
        this.userService = new UserService();
        ensureDefaultAdminExists();

        setTitle("EcoBite | Food Waste Rescue System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        GradientPanel background = new GradientPanel();
        background.setLayout(new GridBagLayout());
        background.setBorder(BorderFactory.createEmptyBorder(44, 56, 44, 56));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints column = new GridBagConstraints();
        column.gridy = 0;
        column.anchor = GridBagConstraints.CENTER;
        column.fill = GridBagConstraints.BOTH;
        column.weighty = 1;
        column.gridx = 0;
        column.weightx = 1.15;
        column.insets = new Insets(0, 0, 0, 54);
        content.add(createBrandPanel(), column);

        column.gridx = 1;
        column.weightx = 0.95;
        column.insets = new Insets(0, 0, 0, 0);
        content.add(createLoginCard(), column);
        background.add(content);
        setContentPane(background);
    }

    private JPanel createBrandPanel() {
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("COMMUNITY FOOD RESCUE");
        eyebrow.setFont(UITheme.BODY_BOLD_FONT);
        eyebrow.setForeground(UITheme.BRAND);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel("ECOBITE");
        brand.setFont(UITheme.TITLE_FONT);
        brand.setForeground(UITheme.TEXT);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel product = new JLabel("Food Waste Rescue System");
        product.setFont(UITheme.SECTION_FONT);
        product.setForeground(UITheme.MUTED);
        product.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel accentLine = new JPanel();
        accentLine.setBackground(UITheme.BRAND);
        accentLine.setMaximumSize(new Dimension(58, 3));
        accentLine.setPreferredSize(new Dimension(58, 3));
        accentLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tagline = new JPanel();
        tagline.setOpaque(false);
        tagline.setLayout(new BoxLayout(tagline, BoxLayout.Y_AXIS));
        JLabel taglineFirstLine = new JLabel("Rescue food. Reduce waste.");
        taglineFirstLine.setFont(new Font("SansSerif", Font.PLAIN, 20));
        taglineFirstLine.setForeground(UITheme.TEXT);
        taglineFirstLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel taglineSecondLine = new JLabel("Feed communities.");
        taglineSecondLine.setFont(new Font("SansSerif", Font.PLAIN, 20));
        taglineSecondLine.setForeground(UITheme.TEXT);
        taglineSecondLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagline.add(taglineFirstLine);
        tagline.add(Box.createVerticalStrut(4));
        tagline.add(taglineSecondLine);

        JPanel supportingText = new JPanel();
        supportingText.setOpaque(false);
        supportingText.setLayout(new BoxLayout(supportingText, BoxLayout.Y_AXIS));
        JLabel supportingFirstLine = new JLabel("Connecting surplus food with the people");
        supportingFirstLine.setFont(UITheme.BODY_FONT);
        supportingFirstLine.setForeground(UITheme.MUTED);
        supportingFirstLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel supportingSecondLine = new JLabel("and communities who need it most.");
        supportingSecondLine.setFont(UITheme.BODY_FONT);
        supportingSecondLine.setForeground(UITheme.MUTED);
        supportingSecondLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        supportingText.add(supportingFirstLine);
        supportingText.add(supportingSecondLine);

        brandPanel.add(eyebrow);
        brandPanel.add(Box.createVerticalStrut(UITheme.SPACE_SM));
        brandPanel.add(brand);
        brandPanel.add(Box.createVerticalStrut(UITheme.SPACE_XS));
        brandPanel.add(product);
        brandPanel.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        brandPanel.add(accentLine);
        brandPanel.add(Box.createVerticalStrut(UITheme.SPACE_LG));
        brandPanel.add(tagline);
        brandPanel.add(Box.createVerticalStrut(UITheme.SPACE_SM));
        brandPanel.add(supportingText);
        brandPanel.add(Box.createVerticalGlue());
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return brandPanel;
    }

    private JPanel createLoginCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(1, 1, 1, 1),
                BorderFactory.createEmptyBorder(34, 34, 32, 34)));
        card.setPreferredSize(new Dimension(390, 438));

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(new Font("SansSerif", Font.BOLD, 25));
        heading.setForeground(UITheme.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel intro = new JLabel("Sign in to continue to EcoBite.");
        intro.setFont(UITheme.BODY_FONT);
        intro.setForeground(UITheme.MUTED);
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel usernameLabel = createFieldLabel("USERNAME");
        usernameField = new JTextField(20);
        usernameField.setToolTipText("Enter your username");
        UITheme.styleTextField(usernameField);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passwordLabel = createFieldLabel("PASSWORD");
        passwordField = new JPasswordField(20);
        passwordField.setToolTipText("Enter your password");
        UITheme.styleTextField(passwordField);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(UITheme.SMALL_FONT);
        errorLabel.setForeground(UITheme.ERROR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setPreferredSize(new Dimension(300, 32));
        errorLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JButton loginButton = createPrimaryButton();
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(event -> handleLogin());
        passwordField.addActionListener(event -> handleLogin());

        card.add(heading);
        card.add(Box.createVerticalStrut(UITheme.SPACE_XS));
        card.add(intro);
        card.add(Box.createVerticalStrut(UITheme.SPACE_LG));
        card.add(usernameLabel);
        card.add(Box.createVerticalStrut(UITheme.SPACE_XS));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(UITheme.SPACE_XS));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(UITheme.SPACE_XS));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(UITheme.SPACE_SM));
        card.add(loginButton);

        DocumentListener clearErrorListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                clearError();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                clearError();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                clearError();
            }
        };
        usernameField.getDocument().addDocumentListener(clearErrorListener);
        passwordField.getDocument().addDocumentListener(clearErrorListener);
        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(UITheme.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton createPrimaryButton() {
        JButton button = new JButton("LOGIN") {
            private boolean hovered;

            {
                setFont(new Font("SansSerif", Font.BOLD, 14));
                setForeground(UITheme.BACKGROUND);
                setFocusPainted(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder(13, 18, 13, 18));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent event) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent event) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(hovered && isEnabled() ? UITheme.BRAND_HOVER : UITheme.BRAND);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setPreferredSize(new Dimension(300, UITheme.BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.BUTTON_HEIGHT));
        return button;
    }

    private void clearError() {
        errorLabel.setText(" ");
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void ensureDefaultAdminExists() {
        if (userService.getAllUsers().isEmpty()) {
            userService.addUser(new User("admin", "admin123", "System Admin"));
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        try {
            if (username.isEmpty()) {
                showError("Enter your username to continue.");
                usernameField.requestFocusInWindow();
                return;
            }
            if (password.isEmpty()) {
                showError("Enter your password to continue.");
                passwordField.requestFocusInWindow();
                return;
            }
            if (userService.validateLogin(username, password)) {
                DashboardFrame dashboardFrame = new DashboardFrame();
                dashboardFrame.setVisible(true);
                dispose();
            } else {
                showError("Username or password is incorrect. Try again.");
                passwordField.setText("");
                passwordField.requestFocusInWindow();
            }
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    private static final class GradientPanel extends JPanel {
        private GradientPanel() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setPaint(new GradientPaint(0, 0, UITheme.BACKGROUND, getWidth(), getHeight(),
                    UITheme.BACKGROUND_RAISED));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }
}
