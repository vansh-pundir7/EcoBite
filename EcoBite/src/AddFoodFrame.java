import javax.swing.*;
import java.awt.*;
import java.sql.*;

// ============================================
// SCREEN 3: ADD FOOD Hoga
// Prepared time = abhi ka time (automatic)
// Expiry = kitne ghante baad kharab hoga
// ============================================
public class AddFoodFrame extends JFrame {

    private JTextField nameField = new JTextField(15);
    private JTextField qtyField  = new JTextField(15);
    private JComboBox<String> typeBox = new JComboBox<>(new String[]{"HUMAN", "ANIMAL"});
    private JTextField hoursField = new JTextField("4", 15);

    public AddFoodFrame() {
        LegacyUiStyle.prepare();
        setSize(620, 460);
        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(LegacyUiStyle.createHeader("Add Food"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 12, 18));
        form.setBorder(BorderFactory.createEmptyBorder(24, 48, 24, 48));
        form.add(new JLabel("Food Name:"));       form.add(nameField);
        form.add(new JLabel("Quantity (KG):"));   form.add(qtyField);
        form.add(new JLabel("Food Type:"));       form.add(typeBox);
        form.add(new JLabel("Expiry (hours):"));  form.add(hoursField);
        add(form, BorderLayout.CENTER);

        JButton saveBtn = new JButton("SAVE FOOD");
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        bottom.add(saveBtn);
        add(bottom, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> saveFood());
        LegacyUiStyle.styleFrame(this);
    }

    private void saveFood() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            msg("Enter a food name.");
            return;
        }

        double qty, hours;
        try {
            qty = Double.parseDouble(qtyField.getText().trim());
        } catch (NumberFormatException ex) {
            msg("Enter the quantity as a number (for example, 10).");
            return;
        }
        try {
            hours = Double.parseDouble(hoursField.getText().trim());
        } catch (NumberFormatException ex) {
            msg("Enter the expiry time in hours as a number (for example, 4).");
            return;
        }
        if (qty <= 0 || hours <= 0) {
            msg("Quantity and expiry time must be greater than zero.");
            return;
        }

        String sql = "INSERT INTO food (food_name, quantity, type, prepared_time, expiry_time, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'AVAILABLE')";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {

            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp expiry = new Timestamp(now.getTime() + (long) (hours * 3600 * 1000));

            ps.setString(1, name);
            ps.setDouble(2, qty);
            ps.setString(3, (String) typeBox.getSelectedItem());
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, expiry);
            ps.executeUpdate();

            msg("Food was added successfully.");
            nameField.setText("");
            qtyField.setText("");
        } catch (Exception ex) {
            msg("Unable to save the food: " + ex.getMessage());
        }
    }

    private void msg(String m) {
        JOptionPane.showMessageDialog(this, m);
    }
}
