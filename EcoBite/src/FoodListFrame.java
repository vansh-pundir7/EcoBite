import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Lists available human food and updates countdowns without repeated database calls. */
public class FoodListFrame extends JFrame {

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Food", "Qty (KG)", "Time Left", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final List<Timestamp> expiries = new ArrayList<>();
    private final JPanel tableArea = new JPanel(new CardLayout());
    private final Timer countdownTimer;

    /** Creates the available-food table and local countdown timer. */
    public FoodListFrame() {
        LegacyUiStyle.prepare();
        setSize(980, 620);
        setMinimumSize(new Dimension(820, 520));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(LegacyUiStyle.createHeader("Available Human Food"), BorderLayout.NORTH);
        tableArea.add(new JScrollPane(table), "table");
        JLabel empty = new JLabel("No food available", SwingConstants.CENTER);
        empty.setForeground(LegacyUiStyle.MUTED);
        LegacyUiStyle.preserveLabelStyle(empty);
        tableArea.add(empty, "empty");
        add(tableArea, BorderLayout.CENTER);

        JButton request = new JButton("REQUEST SELECTED FOOD");
        LegacyUiStyle.setButtonStyle(request, "primary");
        JButton refresh = new JButton("REFRESH");
        JPanel bottom = new JPanel();
        bottom.add(request);
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);
        request.addActionListener(event -> requestFood());
        refresh.addActionListener(event -> loadFood());

        countdownTimer = new Timer(1000, event -> updateCountdowns());
        countdownTimer.start();
        addWindowListener(new WindowAdapter() {
            /** Stops this window's local timer after it closes. */
            @Override
            public void windowClosed(WindowEvent event) {
                countdownTimer.stop();
            }
        });
        loadFood();
        LegacyUiStyle.styleFrame(this);
    }

    /** Reloads the list of human food that is currently available. */
    private void loadFood() {
        model.setRowCount(0);
        expiries.clear();
        String sql = "SELECT id, food_name, quantity, expiry_time, status FROM food "
                + "WHERE type='HUMAN' AND status='AVAILABLE' ORDER BY expiry_time";
        try (Connection connection = DB.get();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Timestamp expiry = results.getTimestamp("expiry_time");
                expiries.add(expiry);
                model.addRow(new Object[]{results.getInt("id"), results.getString("food_name"),
                        results.getDouble("quantity"), formatTimeLeft(expiry, System.currentTimeMillis()),
                        results.getString("status")});
            }
            ((CardLayout) tableArea.getLayout()).show(tableArea,
                    model.getRowCount() == 0 ? "empty" : "table");
        } catch (Exception ex) {
            System.err.println("EcoBite food list load failed: " + ex.getMessage());
            showDatabaseError();
        }
    }

    /** Refreshes only the countdown text using timestamps already loaded in memory. */
    private void updateCountdowns() {
        long now = System.currentTimeMillis();
        for (int row = 0; row < expiries.size(); row++) {
            model.setValueAt(formatTimeLeft(expiries.get(row), now), row, 3);
        }
    }

    /** Formats the time remaining until an item expires. */
    static String formatTimeLeft(Timestamp expiry, long now) {
        long seconds = (expiry.getTime() - now) / 1000;
        if (seconds <= 0) {
            return "TIME FINISHED";
        }
        return String.format("%02d:%02d:%02d",
                seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    /** Collects NGO details and saves the request and food status atomically. */
    private void requestFood() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a food item first.",
                    "No Food Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int foodId = (int) model.getValueAt(table.convertRowIndexToModel(row), 0);
        JTextField name = new JTextField(18);
        JTextField organization = new JTextField(18);
        JTextField phone = new JTextField(18);
        JPanel fields = new JPanel(new GridLayout(3, 2, 8, 8));
        fields.add(new JLabel("Receiver name"));
        fields.add(name);
        fields.add(new JLabel("Organization"));
        fields.add(organization);
        fields.add(new JLabel("Phone (10 digits)"));
        fields.add(phone);
        int answer = JOptionPane.showConfirmDialog(this, fields, "Request Food",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        String error = Validator.required(name.getText(), "Name");
        if (error == null) {
            error = Validator.required(organization.getText(), "Organization");
        }
        if (error == null) {
            error = Validator.phone(phone.getText().trim());
        }
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Check Details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        saveRequest(foodId, name.getText().trim(), organization.getText().trim(), phone.getText().trim());
    }

    /** Inserts a request and marks its food requested in one transaction. */
    private void saveRequest(int foodId, String name, String organization, String phone) {
        try (Connection connection = DB.get()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO requests (food_id, receiver_name, organization, phone, status) "
                            + "VALUES (?, ?, ?, ?, 'PENDING')");
                 PreparedStatement update = connection.prepareStatement(
                         "UPDATE food SET status='REQUESTED' "
                                 + "WHERE id=? AND type='HUMAN' AND status='AVAILABLE'")) {
                update.setInt(1, foodId);
                if (update.executeUpdate() != 1) {
                    throw new IllegalStateException("This food is no longer available.");
                }
                insert.setInt(1, foodId);
                insert.setString(2, name);
                insert.setString(3, organization);
                insert.setString(4, phone);
                insert.executeUpdate();
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
            JOptionPane.showMessageDialog(this, "Request saved with status PENDING.");
            loadFood();
        } catch (Exception ex) {
            System.err.println("EcoBite request could not be saved: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Unable to save the request. " + ex.getMessage(),
                    "Request Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Shows a friendly database connection message. */
    private void showDatabaseError() {
        JOptionPane.showMessageDialog(this,
                "Unable to load food. Check that MySQL is running and db.properties is correct.",
                "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
    }
}
