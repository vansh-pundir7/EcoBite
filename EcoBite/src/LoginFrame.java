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
        setTitle("EcoBite - Login");
        setSize(360, 230);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("ECOBITE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 5, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        form.add(new JLabel("Username:"));
        form.add(userField);
        form.add(new JLabel("Password:"));
        form.add(passField);
        add(form, BorderLayout.CENTER);

        JButton loginBtn = new JButton("LOGIN");
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        bottom.add(loginBtn);
        add(bottom, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> login());
        passField.addActionListener(e -> login());   // Enter dabane par bhi login
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
                JOptionPane.showMessageDialog(this, "Galat username ya password!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Database se connect nahi ho paya!\n\n"
                    + "1. MySQL chal raha hai?\n"
                    + "2. database.sql run kiya?\n"
                    + "3. DB.java mein password sahi hai?\n\n" + ex.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
