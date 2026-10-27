import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Reviews expired food and assigns safe animal food to shelters. */
public class AnimalFoodFrame extends JFrame {

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Food", "Qty (KG)", "Type", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JPanel center = new JPanel(new CardLayout());

    /** Creates the animal food review screen. */
    public AnimalFoodFrame() {
        LegacyUiStyle.prepare();
        setSize(980, 620);
        setMinimumSize(new Dimension(820, 520));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(LegacyUiStyle.createHeader("Animal Food Rescue"), BorderLayout.NORTH);
        center.add(new JScrollPane(table), "table");
        JLabel empty = new JLabel("No food awaiting animal review or assignment", SwingConstants.CENTER);
        empty.setForeground(LegacyUiStyle.MUTED);
        LegacyUiStyle.preserveLabelStyle(empty);
        center.add(empty, "empty");
        add(center, BorderLayout.CENTER);

        JButton shelter = new JButton("ASSIGN SHELTER");
        JButton safe = new JButton("SAFE FOR ANIMALS");
        LegacyUiStyle.setButtonStyle(safe, "primary");
        JButton discard = new JButton("DISCARD");
        LegacyUiStyle.setButtonStyle(discard, "danger");
        JButton refresh = new JButton("REFRESH");
        JPanel bottom = new JPanel();
        bottom.add(shelter);
        bottom.add(safe);
        bottom.add(discard);
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);
        shelter.addActionListener(event -> assignShelter());
        safe.addActionListener(event -> markSafe());
        discard.addActionListener(event -> discard());
        refresh.addActionListener(event -> loadFood());
        loadFood();
        LegacyUiStyle.styleFrame(this);
    }

    /** Loads animal food and human food awaiting a safety review. */
    private void loadFood() {
        model.setRowCount(0);
        String sql = "SELECT id, food_name, quantity, type, status FROM food "
                + "WHERE (type='ANIMAL' AND status='AVAILABLE') OR status='ANIMAL_REVIEW' "
                + "ORDER BY id DESC";
        try (Connection connection = DB.get();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                model.addRow(new Object[]{results.getInt("id"), results.getString("food_name"),
                        results.getDouble("quantity"), results.getString("type"),
                        results.getString("status")});
            }
            ((CardLayout) center.getLayout()).show(center,
                    model.getRowCount() == 0 ? "empty" : "table");
        } catch (Exception ex) {
            System.err.println("EcoBite animal list load failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Unable to load animal food. Check MySQL and the database setup.",
                    "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Returns the selected food row or displays a selection message. */
    private int selectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a food item first.",
                    "No Food Selected", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }

    /** Changes reviewed human food to animal food only after a user confirms it is safe. */
    private void markSafe() {
        int row = selectedRow();
        if (row < 0) {
            return;
        }
        if (!"ANIMAL_REVIEW".equals(model.getValueAt(row, 4))) {
            JOptionPane.showMessageDialog(this, "Only food awaiting review can be marked safe.");
            return;
        }
        updateFood((int) model.getValueAt(row, 0),
                "UPDATE food SET type='ANIMAL', status='AVAILABLE' "
                        + "WHERE id=? AND status='ANIMAL_REVIEW'",
                "Food is now available in the animal category.");
    }

    /** Collects a shelter name and assigns available animal food in one transaction. */
    private void assignShelter() {
        int row = selectedRow();
        if (row < 0) {
            return;
        }
        if (!"AVAILABLE".equals(model.getValueAt(row, 4))
                || !"ANIMAL".equals(model.getValueAt(row, 3))) {
            JOptionPane.showMessageDialog(this, "Only available animal food can be assigned.");
            return;
        }
        String shelter = JOptionPane.showInputDialog(this, "Enter the shelter name:",
                "Assign to Shelter", JOptionPane.QUESTION_MESSAGE);
        if (shelter == null) {
            return;
        }
        String error = Validator.required(shelter, "Shelter name");
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Check Details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int foodId = (int) model.getValueAt(row, 0);
        try (Connection connection = DB.get()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO requests (food_id, receiver_name, organization, phone, status) "
                            + "VALUES (?, 'SHELTER', ?, '', 'COMPLETED')");
                 PreparedStatement update = connection.prepareStatement(
                         "UPDATE food SET status='COMPLETED' "
                                 + "WHERE id=? AND type='ANIMAL' AND status='AVAILABLE'")) {
                update.setInt(1, foodId);
                if (update.executeUpdate() != 1) {
                    throw new IllegalStateException("This animal food is no longer available.");
                }
                insert.setInt(1, foodId);
                insert.setString(2, shelter.trim());
                insert.executeUpdate();
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
            loadFood();
            JOptionPane.showMessageDialog(this, "Food was assigned to the shelter.");
        } catch (Exception ex) {
            System.err.println("EcoBite shelter assignment failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Unable to assign food. " + ex.getMessage(),
                    "Assignment Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Confirms and marks the selected food as discarded. */
    private void discard() {
        int row = selectedRow();
        if (row < 0) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Discard this food item? This cannot be undone.",
                "Confirm Discard", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            updateFood((int) model.getValueAt(row, 0),
                    "UPDATE food SET status='DISCARDED' WHERE id=? AND status<>'DISCARDED'",
                    "Food was discarded.");
        }
    }

    /** Runs a single food status update and reports failures clearly. */
    private void updateFood(int foodId, String sql, String success) {
        try (Connection connection = DB.get();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, foodId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException("The food status has changed. Refresh and try again.");
            }
            loadFood();
            JOptionPane.showMessageDialog(this, success);
        } catch (Exception ex) {
            System.err.println("EcoBite animal food update failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Unable to update food. " + ex.getMessage(),
                    "Food Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
