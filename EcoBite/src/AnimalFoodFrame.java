import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// ============================================
// SCREEN 6: ANIMAL FOOD
// Yahan 2 tarah ka food dikhta hai:
// 1. ANIMAL + AVAILABLE  -> shelter assign karo
// 2. ANIMAL_REVIEW       -> timer khatam hua human food,
//                           responsible person check karega:
//                           "SAFE FOR ANIMALS" ya "DISCARD"
// ============================================
public class AnimalFoodFrame extends JFrame {

    private DefaultTableModel model;
    private JTable table;

    public AnimalFoodFrame() {
        LegacyUiStyle.prepare();
        setSize(980, 620);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(LegacyUiStyle.createHeader("Animal Food Rescue"), BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "Food", "Qty (KG)", "Type", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton shelterBtn = new JButton("ASSIGN SHELTER");
        JButton safeBtn    = new JButton("SAFE FOR ANIMALS");
        JButton discardBtn = new JButton("DISCARD");
        JButton refreshBtn = new JButton("REFRESH");
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        bottom.add(shelterBtn);
        bottom.add(safeBtn);
        bottom.add(discardBtn);
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        shelterBtn.addActionListener(e -> assignShelter());
        safeBtn.addActionListener(e -> markSafe());
        discardBtn.addActionListener(e -> discard());
        refreshBtn.addActionListener(e -> loadFood());

        loadFood();
        LegacyUiStyle.styleFrame(this);
    }

    private void loadFood() {
        model.setRowCount(0);
        String sql = "SELECT id, food_name, quantity, type, status FROM food "
                   + "WHERE (type='ANIMAL' AND status='AVAILABLE') OR status='ANIMAL_REVIEW' "
                   + "ORDER BY id DESC";
        try (Connection con = DB.get();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("food_name"),
                        rs.getDouble("quantity"),
                        rs.getString("type"),
                        rs.getString("status")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load error: " + ex.getMessage());
        }
    }

    // selected row ka food id aur status nikalne ka helper
    private int selectedFoodId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a food item from the table first.",
                    "No Food Selected", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return (int) model.getValueAt(row, 0);
    }

    private String selectedStatus() {
        return (String) model.getValueAt(table.getSelectedRow(), 4);
    }

    private void assignShelter() {
        int foodId = selectedFoodId();
        if (foodId < 0) {
            return;
        }
        if (!selectedStatus().equals("AVAILABLE")) {
            JOptionPane.showMessageDialog(this,
                    "Only available animal food can be assigned to a shelter.");
            return;
        }

        String shelter = JOptionPane.showInputDialog(this, "Enter the shelter name:", "Assign to Shelter",
                JOptionPane.QUESTION_MESSAGE);
        if (shelter == null || shelter.trim().isEmpty()) {
            return;
        }

        try (Connection con = DB.get()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO requests (food_id, receiver_name, organization, phone, status) "
                    + "VALUES (?, 'SHELTER', ?, '', 'COMPLETED')");
            ps.setInt(1, foodId);
            ps.setString(2, shelter.trim());
            ps.executeUpdate();

            ps = con.prepareStatement("UPDATE food SET status='COMPLETED' WHERE id=?");
            ps.setInt(1, foodId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "The food was assigned to the shelter.",
                    "Shelter Assignment Complete", JOptionPane.INFORMATION_MESSAGE);
            loadFood();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void markSafe() {
        int foodId = selectedFoodId();
        if (foodId < 0) {
            return;
        }
        if (!selectedStatus().equals("ANIMAL_REVIEW")) {
            JOptionPane.showMessageDialog(this,
                    "Only food marked for animal review can be assessed.");
            return;
        }
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE food SET type='ANIMAL', status='AVAILABLE' WHERE id=?")) {
            ps.setInt(1, foodId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "The food is now available in the animal category.");
            loadFood();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void discard() {
        int foodId = selectedFoodId();
        if (foodId < 0) {
            return;
        }
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE food SET status='DISCARDED' WHERE id=?")) {
            ps.setInt(1, foodId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "The food was discarded.");
            loadFood();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
