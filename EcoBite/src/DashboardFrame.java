import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

/** Shows EcoBite totals, a category chart, and navigation buttons. */
public class DashboardFrame extends JFrame {

    private final JLabel[] values = new JLabel[5];
    private final JLabel expiringLabel = new JLabel("Expiring in next 1 hour: --");
    private final FoodBarChart chart = new FoodBarChart();
    private final DecimalFormat format = new DecimalFormat("#,##0.##");

    /** Creates the dashboard for the signed-in user. */
    public DashboardFrame(String username) {
        LegacyUiStyle.prepare();
        setSize(1000, 760);
        setMinimumSize(new Dimension(850, 660));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(LegacyUiStyle.createHeader("Dashboard", username), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(BorderFactory.createEmptyBorder(18, 24, 12, 24));
        JPanel upper = new JPanel(new BorderLayout(0, 12));
        upper.add(LegacyUiStyle.createIntro("Food rescue at a glance",
                "Manage donations and help food reach the right place."), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 5, 10, 0));
        String[] labels = {"Total KG", "Human KG", "Animal KG", "Pending", "Completed"};
        Color[] accents = {
                new Color(80, 205, 135), new Color(90, 165, 235),
                new Color(235, 165, 75), new Color(175, 135, 220),
                new Color(70, 195, 170)
        };
        for (int i = 0; i < labels.length; i++) {
            cards.add(createStatCard(labels[i], accents[i], i));
        }
        upper.add(cards, BorderLayout.CENTER);
        content.add(upper, BorderLayout.NORTH);

        JPanel chartArea = new JPanel(new BorderLayout(0, 10));
        chartArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LegacyUiStyle.BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JPanel titleRow = new JPanel(new BorderLayout());
        JLabel chartTitle = new JLabel("Rescue overview");
        chartTitle.setFont(LegacyUiStyle.BODY_BOLD);
        LegacyUiStyle.preserveLabelStyle(chartTitle);
        titleRow.add(chartTitle, BorderLayout.WEST);
        expiringLabel.setForeground(LegacyUiStyle.BRAND);
        LegacyUiStyle.preserveLabelStyle(expiringLabel);
        titleRow.add(expiringLabel, BorderLayout.EAST);
        chartArea.add(titleRow, BorderLayout.NORTH);
        chartArea.add(chart, BorderLayout.CENTER);
        content.add(chartArea, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        JPanel menu = new JPanel(new GridLayout(2, 3, 10, 10));
        menu.setBorder(BorderFactory.createEmptyBorder(6, 24, 20, 24));
        JButton addButton = new JButton("ADD FOOD");
        JButton listButton = new JButton("AVAILABLE FOOD");
        JButton animalButton = new JButton("ANIMAL FOOD");
        JButton requestsButton = new JButton("REQUESTS");
        JButton historyButton = new JButton("HISTORY");
        JButton logoutButton = new JButton("LOG OUT");
        for (JButton button : new JButton[]{addButton, listButton, animalButton,
                requestsButton, historyButton, logoutButton}) {
            menu.add(button);
        }
        add(menu, BorderLayout.SOUTH);
        addButton.addActionListener(event -> new AddFoodFrame().setVisible(true));
        listButton.addActionListener(event -> new FoodListFrame().setVisible(true));
        animalButton.addActionListener(event -> new AnimalFoodFrame().setVisible(true));
        requestsButton.addActionListener(event -> new RequestsFrame().setVisible(true));
        historyButton.addActionListener(event -> new HistoryFrame().setVisible(true));
        logoutButton.addActionListener(event -> logout());

        loadCounts();
        addWindowFocusListener(new WindowAdapter() {
            /** Refreshes the numbers after returning from a child window. */
            @Override
            public void windowGainedFocus(WindowEvent event) {
                loadCounts();
            }
        });
        LegacyUiStyle.styleFrame(this);
    }

    /** Builds one dashboard card and keeps its number available for updates. */
    private JPanel createStatCard(String label, Color accent, int index) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)));
        values[index] = new JLabel("--", SwingConstants.CENTER);
        values[index].setFont(new Font("SansSerif", Font.BOLD, 23));
        values[index].setForeground(accent);
        LegacyUiStyle.preserveLabelStyle(values[index]);
        JLabel caption = new JLabel(label, SwingConstants.CENTER);
        caption.setFont(new Font("SansSerif", Font.BOLD, 12));
        caption.setForeground(LegacyUiStyle.MUTED);
        LegacyUiStyle.preserveLabelStyle(caption);
        JLabel accentBar = new JLabel();
        accentBar.setOpaque(true);
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(24, 3));
        LegacyUiStyle.preserveLabelStyle(accentBar);
        card.add(accentBar, BorderLayout.NORTH);
        card.add(values[index], BorderLayout.CENTER);
        card.add(caption, BorderLayout.SOUTH);
        return card;
    }

    /** Loads current totals and updates the chart. */
    private void loadCounts() {
        try (Connection connection = DB.get()) {
            double[] stats = queryStats(connection);
            for (int i = 0; i < 3; i++) {
                values[i].setText(format.format(stats[i]));
            }
            values[3].setText(Integer.toString((int) stats[3]));
            values[4].setText(Integer.toString((int) stats[4]));
            expiringLabel.setText("Expiring in next 1 hour: " + (int) stats[5]);
            chart.setValues(stats[1], stats[2]);
        } catch (Exception ex) {
            System.err.println("EcoBite dashboard load failed: " + ex.getMessage());
            expiringLabel.setText("Dashboard data unavailable; check MySQL.");
        }
    }

    /** Reads dashboard totals and the near-expiry count in one query. */
    static double[] queryStats(Connection connection) throws SQLException {
        String active = "status IN ('AVAILABLE','REQUESTED','ANIMAL_REVIEW')";
        String sql = "SELECT "
                + "COALESCE(SUM(CASE WHEN " + active + " THEN quantity ELSE 0 END),0),"
                + "COALESCE(SUM(CASE WHEN " + active + " AND type='HUMAN' THEN quantity ELSE 0 END),0),"
                + "COALESCE(SUM(CASE WHEN " + active + " AND type='ANIMAL' THEN quantity ELSE 0 END),0),"
                + "(SELECT COUNT(*) FROM requests WHERE status='PENDING'),"
                + "(SELECT COUNT(*) FROM food WHERE status='COMPLETED'),"
                + "(SELECT COUNT(*) FROM food WHERE type='HUMAN' AND status='AVAILABLE' "
                + "AND expiry_time >= NOW() AND expiry_time <= DATE_ADD(NOW(), INTERVAL 1 HOUR)) "
                + "FROM food";
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            if (result.next()) {
                return new double[]{result.getDouble(1), result.getDouble(2), result.getDouble(3),
                        result.getInt(4), result.getInt(5), result.getInt(6)};
            }
        }
        throw new SQLException("Dashboard totals query returned no row.");
    }

    /** Confirms logout before returning to the sign-in window. */
    private void logout() {
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    /** Draws the human and animal quantities as simple horizontal bars. */
    private static final class FoodBarChart extends JPanel {
        private double human;
        private double animal;

        /** Sets up the chart drawing area. */
        FoodBarChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 190));
        }

        /** Updates chart values and requests a redraw. */
        void setValues(double human, double animal) {
            this.human = human;
            this.animal = animal;
            repaint();
        }

        /** Paints the two category bars scaled to the larger quantity. */
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int labelWidth = 100;
            int width = Math.max(10, getWidth() - labelWidth - 90);
            int top = Math.max(24, getHeight() / 2 - 45);
            double max = Math.max(1, Math.max(human, animal));
            drawBar(g, "Human KG", human, max, top, width,
                    labelWidth, new Color(90, 165, 235));
            drawBar(g, "Animal KG", animal, max, top + 70, width,
                    labelWidth, new Color(235, 165, 75));
            g.dispose();
        }

        /** Paints one labeled bar and its quantity. */
        private void drawBar(Graphics2D g, String label, double amount, double max,
                             int y, int width, int labelWidth, Color color) {
            g.setFont(LegacyUiStyle.BODY_BOLD);
            g.setColor(LegacyUiStyle.TEXT);
            g.drawString(label, 8, y + 23);
            g.setColor(LegacyUiStyle.SURFACE_RAISED);
            g.fillRoundRect(labelWidth, y, width, 34, 14, 14);
            g.setColor(color);
            g.fillRoundRect(labelWidth, y, Math.max(7, (int) (width * amount / max)), 34, 14, 14);
            g.setColor(LegacyUiStyle.TEXT);
            g.setFont(LegacyUiStyle.BODY);
            g.drawString(new DecimalFormat("#,##0.##").format(amount),
                    labelWidth + width + 10, y + 22);
        }
    }
}
