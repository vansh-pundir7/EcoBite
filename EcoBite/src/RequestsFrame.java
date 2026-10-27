import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Shows pending food requests and lets the user approve or reject them. */
public class RequestsFrame extends JFrame {

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Req ID", "Food", "Qty (KG)", "Name", "Organization", "Phone"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JPanel center = new JPanel(new CardLayout());

    /** Creates the requests table and its action buttons. */
    public RequestsFrame() {
        LegacyUiStyle.prepare();
        setSize(1040, 620);
        setMinimumSize(new Dimension(860, 520));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(LegacyUiStyle.createHeader("Pending Requests"), BorderLayout.NORTH);
        center.add(new JScrollPane(table), "table");
        JLabel empty = new JLabel("No pending requests", SwingConstants.CENTER);
        empty.setForeground(LegacyUiStyle.MUTED);
        LegacyUiStyle.preserveLabelStyle(empty);
        center.add(empty, "empty");
        add(center, BorderLayout.CENTER);

        JButton approve = new JButton("APPROVE");
        LegacyUiStyle.setButtonStyle(approve, "primary");
        JButton reject = new JButton("REJECT");
        LegacyUiStyle.setButtonStyle(reject, "danger");
        JButton refresh = new JButton("REFRESH");
        JPanel bottom = new JPanel();
        bottom.add(approve);
        bottom.add(reject);
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);
        approve.addActionListener(event -> updateStatus("COMPLETED", "COMPLETED"));
        reject.addActionListener(event -> confirmReject());
        refresh.addActionListener(event -> loadRequests());
        loadRequests();
        LegacyUiStyle.styleFrame(this);
    }

    /** Loads pending requests and displays a friendly empty message if none exist. */
    private void loadRequests() {
        model.setRowCount(0);
        String sql = "SELECT r.id, f.food_name, f.quantity, r.receiver_name, r.organization, r.phone "
                + "FROM requests r JOIN food f ON r.food_id=f.id "
                + "WHERE r.status='PENDING' ORDER BY r.id";
        try (Connection connection = DB.get();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                model.addRow(new Object[]{results.getInt(1), results.getString(2),
                        results.getDouble(3), results.getString(4), results.getString(5),
                        results.getString(6)});
            }
            ((CardLayout) center.getLayout()).show(center,
                    model.getRowCount() == 0 ? "empty" : "table");
        } catch (Exception ex) {
            System.err.println("EcoBite request list load failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Unable to load requests. Check MySQL and the database setup.",
                    "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Confirms that the user intends to reject the selected request. */
    private void confirmReject() {
        int answer = JOptionPane.showConfirmDialog(this, "Reject this food request?",
                "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            updateStatus("REJECTED", "AVAILABLE");
        }
    }

    /** Updates the request and its food row together, rolling back either on failure. */
    private void updateStatus(String requestStatus, String foodStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first.",
                    "No Request Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int requestId = (int) model.getValueAt(table.convertRowIndexToModel(row), 0);
        try (Connection connection = DB.get()) {
            connection.setAutoCommit(false);
            try {
                int foodId;
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT food_id FROM requests WHERE id=? AND status='PENDING'")) {
                    select.setInt(1, requestId);
                    try (ResultSet result = select.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalStateException("This request is no longer pending.");
                        }
                        foodId = result.getInt(1);
                    }
                }
                try (PreparedStatement updateRequest = connection.prepareStatement(
                        "UPDATE requests SET status=? WHERE id=? AND status='PENDING'");
                     PreparedStatement updateFood = connection.prepareStatement(
                             "UPDATE food SET status=? WHERE id=?")) {
                    updateRequest.setString(1, requestStatus);
                    updateRequest.setInt(2, requestId);
                    if (updateRequest.executeUpdate() != 1) {
                        throw new IllegalStateException("This request is no longer pending.");
                    }
                    updateFood.setString(1, foodStatus);
                    updateFood.setInt(2, foodId);
                    if (updateFood.executeUpdate() != 1) {
                        throw new IllegalStateException("The related food item no longer exists.");
                    }
                }
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
            loadRequests();
        } catch (Exception ex) {
            System.err.println("EcoBite request update failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Unable to update the request. " + ex.getMessage(),
                    "Request Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
