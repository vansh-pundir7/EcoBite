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
        setTitle("EcoBite - Animal Food");
        setSize(680, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("ANIMAL FOOD", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        add(title, BorderLayout.NORTH);

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
            JOptionPane.showMessageDialog(this, "Pehle table mein food select karo!");
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
                    "Sirf AVAILABLE animal food shelter ko assign ho sakta hai!");
            return;
        }

        String shelter = JOptionPane.showInputDialog(this, "Shelter ka naam:");
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

            JOptionPane.showMessageDialog(this, "Shelter Assigned! Status: COMPLETED");
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
                    "Sirf ANIMAL_REVIEW wala food check kar sakte ho!");
            return;
        }
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE food SET type='ANIMAL', status='AVAILABLE' WHERE id=?")) {
            ps.setInt(1, foodId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Food ab ANIMAL category mein AVAILABLE hai!");
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
            JOptionPane.showMessageDialog(this, "Food DISCARDED!");
            loadFood();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
