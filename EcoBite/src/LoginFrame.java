import javax.swing.*;
import java.awt.*;
import java.sql.*;

// ============================================
// SCREEN 1: LOGIN
// Simple login - users table se check
// ============================================
public class LoginFrame extends JFrame {

    private JTextField userField = new JTextField(15);
    private JPasswordField passField = new JPasswordField(15);

    public LoginFrame() {
        LegacyUiStyle.prepare();
        setSize(520, 340);
        setMinimumSize(new Dimension(480, 320));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(LegacyUiStyle.createHeader("Sign In"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 12, 16));
        form.setBorder(BorderFactory.createEmptyBorder(24, 48, 24, 48));
        form.add(new JLabel("Username:"));
        form.add(userField);
        form.add(new JLabel("Password:"));
        form.add(passField);
        add(form, BorderLayout.CENTER);

        JButton loginBtn = new JButton("LOGIN");
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        bottom.add(loginBtn);
        add(bottom, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> login());
        passField.addActionListener(e -> login());
        LegacyUiStyle.styleFrame(this);
    }

    private void login() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        String sql = "SELECT id FROM users WHERE username=? AND password=?";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                new DashboardFrame().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "The username or password is incorrect.",
                        "Sign-In Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to connect to the database.\n\n"
                    + "1. Make sure MySQL is running.\n"
                    + "2. Make sure database.sql has been run.\n"
                    + "3. Check the database password in DB.java.\n\n" + ex.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
