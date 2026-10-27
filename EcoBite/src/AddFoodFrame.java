import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/** Collects and saves a new food donation. */
public class AddFoodFrame extends JFrame {

    private final JTextField nameField = new JTextField(20);
    private final JTextField quantityField = new JTextField(20);
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"HUMAN", "ANIMAL"});
    private final JTextField hoursField = new JTextField("4", 20);

    /** Creates the aligned food form and its short guidance hints. */
    public AddFoodFrame() {
        LegacyUiStyle.prepare();
        setSize(680, 540);
        setMinimumSize(new Dimension(580, 480));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(LegacyUiStyle.createHeader("Add Food"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 36, 14, 36));
        addField(form, 0, "Food name", nameField, "Up to 50 characters.");
        addField(form, 1, "Quantity (KG)", quantityField, "Enter a value greater than 0.");
        addField(form, 2, "Food type", typeBox, "Choose human or animal food.");
        addField(form, 3, "Expiry (hours)", hoursField, "Human food is reviewed after expiry.");
        JPanel center = new JPanel(new BorderLayout());
        center.add(LegacyUiStyle.createIntro(
                "Create a food listing", "Add clear details to help food reach the right place."),
                BorderLayout.NORTH);
        center.add(form, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JButton save = new JButton("SAVE FOOD");
        LegacyUiStyle.setButtonStyle(save, "primary");
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        bottom.add(save);
        add(bottom, BorderLayout.SOUTH);
        save.addActionListener(event -> saveFood());
        LegacyUiStyle.styleFrame(this);
    }

    /** Adds one aligned editor row with a short hint below it. */
    private void addField(JPanel form, int row, String title, JComponent field, String hint) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 3, 6);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = row * 2;
        form.add(new JLabel(title + ":"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(field, c);
        JLabel hintLabel = new JLabel(hint);
        hintLabel.setForeground(LegacyUiStyle.MUTED);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        LegacyUiStyle.preserveLabelStyle(hintLabel);
        c.gridy++;
        form.add(hintLabel, c);
    }

    /** Validates and inserts the new donation. */
    private void saveFood() {
        String name = nameField.getText().trim();
        String error = Validator.foodName(name);
        if (error == null) {
            error = Validator.quantity(quantityField.getText());
        }
        if (error != null) {
            showMessage(error, JOptionPane.WARNING_MESSAGE);
            return;
        }
        double quantity = Double.parseDouble(quantityField.getText().trim());
        double hours;
        try {
            hours = Double.parseDouble(hoursField.getText().trim());
        } catch (NumberFormatException ex) {
            showMessage("Expiry time must be a number of hours.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Double.isFinite(hours) || hours <= 0 || hours > 8760) {
            showMessage("Expiry time must be greater than 0 and no more than 8,760 hours.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO food (food_name, quantity, type, prepared_time, expiry_time, status) "
                + "VALUES (?, ?, ?, ?, ?, 'AVAILABLE')";
        try (Connection connection = DB.get();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            statement.setString(1, name);
            statement.setDouble(2, quantity);
            statement.setString(3, (String) typeBox.getSelectedItem());
            statement.setTimestamp(4, now);
            statement.setTimestamp(5, new Timestamp(now.getTime() + (long) (hours * 3_600_000)));
            statement.executeUpdate();
            showMessage("Food was added successfully.", JOptionPane.INFORMATION_MESSAGE);
            nameField.setText("");
            quantityField.setText("");
        } catch (Exception ex) {
            System.err.println("EcoBite could not save food: " + ex.getMessage());
            showMessage("Unable to save food. Check MySQL and the database setup.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Shows a concise validation or save result. */
    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(this, message, "EcoBite", type);
    }
}
