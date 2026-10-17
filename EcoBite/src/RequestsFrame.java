import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// ============================================
// SCREEN 5: REQUESTS
// PENDING requests dikhti hain.
// APPROVE -> request COMPLETED + food COMPLETED
// REJECT  -> request REJECTED  + food wapas AVAILABLE
// ============================================
public class RequestsFrame extends JFrame {

    private DefaultTableModel model;
    private JTable table;

    public RequestsFrame() {
        LegacyUiStyle.prepare();
        setSize(1040, 620);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(LegacyUiStyle.createHeader("Pending Requests"), BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"Req ID", "Food", "Qty (KG)", "Name", "Organization", "Phone"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton approveBtn = new JButton("APPROVE");
        JButton rejectBtn  = new JButton("REJECT");
        JButton refreshBtn = new JButton("REFRESH");
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        bottom.add(approveBtn);
        bottom.add(rejectBtn);
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> updateStatus("COMPLETED", "COMPLETED"));
        rejectBtn.addActionListener(e -> updateStatus("REJECTED", "AVAILABLE"));
        refreshBtn.addActionListener(e -> loadRequests());

        loadRequests();
        LegacyUiStyle.styleFrame(this);
    }

    private void loadRequests() {
        model.setRowCount(0);
        String sql = "SELECT r.id, f.food_name, f.quantity, r.receiver_name, r.organization, r.phone "
                   + "FROM requests r JOIN food f ON r.food_id = f.id "
                   + "WHERE r.status='PENDING' ORDER BY r.id";
        try (Connection con = DB.get();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getDouble(3),
                        rs.getString(4), rs.getString(5), rs.getString(6)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load error: " + ex.getMessage());
        }
    }

    private void updateStatus(String reqStatus, String foodStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request from the table first.",
                    "No Request Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int reqId = (int) model.getValueAt(row, 0);

        try (Connection con = DB.get()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE requests SET status=? WHERE id=?");
            ps.setString(1, reqStatus);
            ps.setInt(2, reqId);
            ps.executeUpdate();

            // us request ka food ka status bhi update karo
            ps = con.prepareStatement(
                    "UPDATE food f JOIN requests r ON f.id = r.food_id "
                    + "SET f.status=? WHERE r.id=?");
            ps.setString(1, foodStatus);
            ps.setInt(2, reqId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Request " + reqStatus);
            loadRequests();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
