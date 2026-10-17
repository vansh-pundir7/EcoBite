import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// ============================================
// SCREEN 4: AVAILABLE FOOD (HUMAN) + TIMER
// Har second countdown update hota hai.
// Time khatam -> status ANIMAL_REVIEW
// (automatically "animal safe" NAHI bolte -
//  responsible person review karega)
// ============================================
public class FoodListFrame extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private List<Timestamp> expiries = new ArrayList<>();   // har row ka expiry time

    public FoodListFrame() {
        setTitle("EcoBite - Available Food (Human)");
        setSize(680, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("AVAILABLE FOOD (HUMAN)", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "Food", "Qty (KG)", "Time Left", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton reqBtn = new JButton("REQUEST SELECTED FOOD");
        JButton refBtn = new JButton("REFRESH");
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        bottom.add(reqBtn);
        bottom.add(refBtn);
        add(bottom, BorderLayout.SOUTH);

        reqBtn.addActionListener(e -> requestFood());
        refBtn.addActionListener(e -> loadFood());

        loadFood();

        // TIMER: har 1 second
        new Timer(1000, e -> tick()).start();
    }

    private void loadFood() {
        model.setRowCount(0);
        expiries.clear();
        String sql = "SELECT id, food_name, quantity, expiry_time, status FROM food "
                   + "WHERE type='HUMAN' AND status='AVAILABLE' ORDER BY expiry_time";
        try (Connection con = DB.get();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Timestamp exp = rs.getTimestamp("expiry_time");
                expiries.add(exp);
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("food_name"),
                        rs.getDouble("quantity"),
                        timeLeft(exp),
                        rs.getString("status")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load error: " + ex.getMessage());
        }
    }

    // har second call hota hai
    private void tick() {
        // 1. expired human food ko ANIMAL_REVIEW banao
        try (Connection con = DB.get();
             Statement st = con.createStatement()) {
            int changed = st.executeUpdate(
                    "UPDATE food SET status='ANIMAL_REVIEW' "
                    + "WHERE type='HUMAN' AND status='AVAILABLE' AND expiry_time < NOW()");
            if (changed > 0) {
                loadFood();   // list refresh
                return;
            }
        } catch (Exception ignored) {
        }

        // 2. countdown column update karo
        for (int i = 0; i < expiries.size() && i < model.getRowCount(); i++) {
            model.setValueAt(timeLeft(expiries.get(i)), i, 3);
        }
    }

    private String timeLeft(Timestamp exp) {
        long sec = (exp.getTime() - System.currentTimeMillis()) / 1000;
        if (sec <= 0) {
            return "TIME FINISHED";
        }
        return String.format("%02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60);
    }

    private void requestFood() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pehle table mein food select karo!");
            return;
        }
        int foodId = (int) model.getValueAt(row, 0);

        JTextField name  = new JTextField();
        JTextField org   = new JTextField();
        JTextField phone = new JTextField();
        Object[] fields = {"Name:", name, "Organization (NGO):", org, "Phone:", phone};
        int ok = JOptionPane.showConfirmDialog(this, fields,
                "Request Food", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        try (Connection con = DB.get()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO requests (food_id, receiver_name, organization, phone, status) "
                    + "VALUES (?, ?, ?, ?, 'PENDING')");
            ps.setInt(1, foodId);
            ps.setString(2, name.getText().trim());
            ps.setString(3, org.getText().trim());
            ps.setString(4, phone.getText().trim());
            ps.executeUpdate();

            ps = con.prepareStatement("UPDATE food SET status='REQUESTED' WHERE id=?");
            ps.setInt(1, foodId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Request Saved! Status: PENDING");
            loadFood();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Request error: " + ex.getMessage());
        }
    }
}
