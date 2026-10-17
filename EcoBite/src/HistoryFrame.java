import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// ============================================
// SCREEN 7: HISTORY
// COMPLETED aur DISCARDED food ki list
// ============================================
public class HistoryFrame extends JFrame {

    private DefaultTableModel model;

    public HistoryFrame() {
        LegacyUiStyle.prepare();
        setSize(900, 580);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(LegacyUiStyle.createHeader("Food Rescue History"), BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "Food", "Qty (KG)", "Type", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("REFRESH");
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadHistory());

        loadHistory();
        LegacyUiStyle.styleFrame(this);
    }

    private void loadHistory() {
        model.setRowCount(0);
        String sql = "SELECT id, food_name, quantity, type, status FROM food "
                   + "WHERE status IN ('COMPLETED','DISCARDED') ORDER BY id DESC";
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
}
