import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Shows sign in and opens the dashboard after a successful login. */
public class LoginFrame extends JFrame {

    private final JTextField userField = new JTextField(18);
    private final JPasswordField passField = new JPasswordField(18);
    private final JCheckBox showPassword = new JCheckBox("Show password");

    /** Creates the centered sign-in form. */
    public LoginFrame() {
        LegacyUiStyle.prepare();
        setSize(580, 450);
        setMinimumSize(new Dimension(500, 410));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(LegacyUiStyle.createHeader("Sign In"), BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LegacyUiStyle.BORDER),
                BorderFactory.createEmptyBorder(22, 30, 22, 30)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel title = new JLabel("Welcome to EcoBite", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(LegacyUiStyle.BRAND);
        LegacyUiStyle.preserveLabelStyle(title);
        card.add(title, c);
        c.gridy++;
        card.add(new JLabel("Sign in to manage rescued food"), c);
        c.gridy++;
        c.gridwidth = 1;
        card.add(new JLabel("Username"), c);
        c.gridx = 1;
        card.add(userField, c);
        c.gridx = 0;
        c.gridy++;
        card.add(new JLabel("Password"), c);
        c.gridx = 1;
        card.add(passField, c);
        c.gridy++;
        card.add(showPassword, c);

        JButton loginButton = new JButton("SIGN IN");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        card.add(loginButton, c);
        JPanel center = new JPanel(new GridBagLayout());
        center.add(card);
        add(center, BorderLayout.CENTER);

        char echo = passField.getEchoChar();
        showPassword.addActionListener(event ->
                passField.setEchoChar(showPassword.isSelected() ? (char) 0 : echo));
        loginButton.addActionListener(event -> login());
        passField.addActionListener(event -> login());
        userField.addActionListener(event -> passField.requestFocusInWindow());
        getRootPane().setDefaultButton(loginButton);
        LegacyUiStyle.styleFrame(this);
    }

    /** Checks the account and upgrades a valid legacy password to a salted hash. */
    private void login() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both your username and password.",
                    "Sign-In Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection connection = DB.get();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT id, password FROM users WHERE username=?")) {
            query.setString(1, username);
            int userId;
            String stored;
            try (ResultSet results = query.executeQuery()) {
                if (!results.next() || !PasswordHasher.verify(
                        password, results.getString("password"))) {
                    JOptionPane.showMessageDialog(this, "The username or password is incorrect.",
                            "Sign-In Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                userId = results.getInt("id");
                stored = results.getString("password");
            }
            if (!stored.startsWith("sha256:")) {
                PasswordHasher.updatePassword(connection, userId, PasswordHasher.hash(password));
            }
            new DashboardFrame(username).setVisible(true);
            dispose();
        } catch (Exception ex) {
            System.err.println("EcoBite sign-in failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "EcoBite could not connect to MySQL.\n\nCheck that MySQL is running, "
                            + "database.sql has been run, and db.properties has the correct values.",
                    "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
        }
    }
}
