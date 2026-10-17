import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// ============================================
// SCREEN 2: DASHBOARD
// Counts + menu buttons
// ============================================
public class DashboardFrame extends JFrame {

    private JLabel totalLbl   = new JLabel();
    private JLabel humanLbl   = new JLabel();
    private JLabel animalLbl  = new JLabel();
    private JLabel pendingLbl = new JLabel();
    private JLabel doneLbl    = new JLabel();

    public DashboardFrame() {
        setTitle("EcoBite - Dashboard");
        setSize(420, 430);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("ECOBITE DASHBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(5, 1, 5, 5));
        stats.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        stats.add(totalLbl);
        stats.add(humanLbl);
        stats.add(animalLbl);
        stats.add(pendingLbl);
        stats.add(doneLbl);
        add(stats, BorderLayout.CENTER);

        JButton addBtn    = new JButton("ADD FOOD");
        JButton listBtn   = new JButton("AVAILABLE FOOD");
        JButton animalBtn = new JButton("ANIMAL FOOD");
        JButton reqBtn    = new JButton("REQUESTS");
        JButton histBtn   = new JButton("HISTORY");
        JButton logoutBtn = new JButton("LOGOUT");

        JPanel menu = new JPanel(new GridLayout(3, 2, 8, 8));
        menu.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));
        menu.add(addBtn);
        menu.add(listBtn);
        menu.add(animalBtn);
        menu.add(reqBtn);
        menu.add(histBtn);
        menu.add(logoutBtn);
        add(menu, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> new AddFoodFrame().setVisible(true));
        listBtn.addActionListener(e -> new FoodListFrame().setVisible(true));
        animalBtn.addActionListener(e -> new AnimalFoodFrame().setVisible(true));
        reqBtn.addActionListener(e -> new RequestsFrame().setVisible(true));
        histBtn.addActionListener(e -> new HistoryFrame().setVisible(true));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        loadCounts();

        // doosri screen se wapas aane par counts refresh ho jayein
        addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                loadCounts();
            }
        });
    }

    private void loadCounts() {
        try (Connection con = DB.get()) {
            // abhi system mein jo food hai (completed/discard nahi hua)
            String inStock = "status IN ('AVAILABLE','REQUESTED','ANIMAL_REVIEW')";
            totalLbl.setText("Total Food    : " + sumFood(con, inStock) + " KG");
            humanLbl.setText("Human Food    : " + sumFood(con, inStock + " AND type='HUMAN'") + " KG");
            animalLbl.setText("Animal Food   : " + sumFood(con, inStock + " AND type='ANIMAL'") + " KG");
            pendingLbl.setText("Pending Req.  : " + countRows(con,
                    "SELECT COUNT(*) FROM requests WHERE status='PENDING'"));
            doneLbl.setText("Completed     : " + countRows(con,
                    "SELECT COUNT(*) FROM food WHERE status='COMPLETED'"));
        } catch (Exception ex) {
            totalLbl.setText("Database error: " + ex.getMessage());
        }
    }

    private double sumFood(Connection con, String where) throws Exception {
        ResultSet rs = con.createStatement().executeQuery(
                "SELECT COALESCE(SUM(quantity),0) FROM food WHERE " + where);
        rs.next();
        return rs.getDouble(1);
    }

    private int countRows(Connection con, String sql) throws Exception {
        ResultSet rs = con.createStatement().executeQuery(sql);
        rs.next();
        return rs.getInt(1);
    }
}
