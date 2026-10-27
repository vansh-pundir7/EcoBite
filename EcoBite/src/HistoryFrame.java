import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;

/** Shows completed and discarded food, receiver details, search, and CSV export. */
public class HistoryFrame extends JFrame {

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Food", "Qty (KG)", "Type", "Status", "Received By", "Date / Time"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField searchField = new JTextField(18);
    private final JPanel center = new JPanel(new CardLayout());

    /** Creates searchable history and CSV export controls. */
    public HistoryFrame() {
        LegacyUiStyle.prepare();
        setSize(1100, 640);
        setMinimumSize(new Dimension(850, 520));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        top.add(new JLabel("Search food"), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        JButton export = new JButton("EXPORT CSV");
        LegacyUiStyle.setButtonStyle(export, "primary");
        top.add(export, BorderLayout.EAST);
        JPanel north = new JPanel(new BorderLayout());
        north.add(LegacyUiStyle.createHeader("Food Rescue History"), BorderLayout.NORTH);
        north.add(top, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        table.setRowSorter(sorter);
        center.add(new JScrollPane(table), "table");
        JLabel empty = new JLabel("No food history found", SwingConstants.CENTER);
        empty.setForeground(LegacyUiStyle.MUTED);
        LegacyUiStyle.preserveLabelStyle(empty);
        center.add(empty, "empty");
        add(center, BorderLayout.CENTER);

        JButton refresh = new JButton("REFRESH");
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);
        refresh.addActionListener(event -> loadHistory());
        export.addActionListener(event -> exportCsv());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                filterRows();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                filterRows();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                filterRows();
            }
        });
        loadHistory();
        LegacyUiStyle.styleFrame(this);
    }

    /** Loads rescue outcomes and their matching completed request details. */
    private void loadHistory() {
        model.setRowCount(0);
        String sql = "SELECT f.id, f.food_name, f.quantity, f.type, f.status, "
                + "CASE WHEN r.receiver_name='SHELTER' THEN r.organization "
                + "WHEN r.organization IS NOT NULL THEN CONCAT(r.receiver_name, ' - ', r.organization) "
                + "ELSE '-' END AS receiver, COALESCE(r.created_at, f.prepared_time) AS event_time "
                + "FROM food f LEFT JOIN requests r ON r.food_id=f.id AND r.status='COMPLETED' "
                + "WHERE f.status IN ('COMPLETED','DISCARDED') ORDER BY event_time DESC";
        try (Connection connection = DB.get();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                model.addRow(new Object[]{results.getInt("id"), results.getString("food_name"),
                        results.getDouble("quantity"), results.getString("type"),
                        results.getString("status"), results.getString("receiver"),
                        results.getTimestamp("event_time")});
            }
            ((CardLayout) center.getLayout()).show(center,
                    model.getRowCount() == 0 ? "empty" : "table");
            filterRows();
        } catch (Exception ex) {
            System.err.println("EcoBite history load failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Unable to load history. Check MySQL and the database setup.",
                    "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Filters visible rows by the food name without another database query. */
    private void filterRows() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1));
    }

    /** Writes the currently visible history rows as quoted UTF-8 CSV. */
    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("ecobite-history.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(
                chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8)) {
            for (int column = 0; column < model.getColumnCount(); column++) {
                if (column > 0) {
                    writer.write(',');
                }
                writer.write(csv(model.getColumnName(column)));
            }
            writer.newLine();
            for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
                for (int column = 0; column < table.getColumnCount(); column++) {
                    if (column > 0) {
                        writer.write(',');
                    }
                    Object value = table.getValueAt(viewRow, column);
                    writer.write(csv(value == null ? "" : value.toString()));
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "History exported successfully.");
        } catch (Exception ex) {
            System.err.println("EcoBite CSV export failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Unable to export CSV: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Escapes a CSV value by quoting it and doubling embedded quotes. */
    static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
