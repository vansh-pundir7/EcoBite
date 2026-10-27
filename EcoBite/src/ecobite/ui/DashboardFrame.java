package ecobite.ui;

import ecobite.model.Food;
import ecobite.model.FoodRequest;
import ecobite.service.FoodRequestService;
import ecobite.service.FoodService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFrame extends JFrame {
    private final FoodService foodService;
    private final FoodRequestService foodRequestService;
    private final JPanel statsPanel;
    private final Map<String, JLabel> statisticValues;
    private final Map<String, JLabel> statisticDetails;
    private final FoodDistributionChartPanel foodDistributionChart;
    private final RequestStatusChartPanel requestStatusChart;
    private final FoodStatusOverviewPanel foodStatusOverviewPanel;
    private final RescueImpactPanel rescueImpactPanel;

    public DashboardFrame() {
        UiStyle.prepare();
        this.foodService = new FoodService();
        this.foodRequestService = new FoodRequestService();
        this.statisticValues = new LinkedHashMap<>();
        this.statisticDetails = new LinkedHashMap<>();

        setTitle(UITheme.APP_NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 860);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(18, 18));

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(UITheme.BACKGROUND);

        JPanel topPanel = new JPanel(new BorderLayout(18, 0));
        topPanel.setOpaque(false);
        topPanel.add(UiStyle.createHeader("Food Rescue Analytics", "Real-time overview of food rescue activity"), BorderLayout.CENTER);

        JButton refreshButton = new JButton("REFRESH");
        refreshButton.addActionListener(event -> refreshStatistics());
        JPanel refreshActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        refreshActions.setOpaque(false);
        refreshActions.add(refreshButton);
        topPanel.add(refreshActions, BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);

        statsPanel = new JPanel(new GridLayout(2, 4, 18, 18));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 10, 0));
        statsPanel.add(createStatCard("TOTAL FOOD", "All recorded food"));
        statsPanel.add(createStatCard("HUMAN FOOD", "Food suitable for people"));
        statsPanel.add(createStatCard("ANIMAL FOOD", "Food available for animals"));
        statsPanel.add(createStatCard("PENDING REQUESTS", "Awaiting approval"));
        statsPanel.add(createStatCard("COMPLETED REQUESTS", "Successfully fulfilled"));
        statsPanel.add(createStatCard("TIME FINISHED", "Expired food"));
        statsPanel.add(createStatCard("ANIMAL REVIEW", "Needs review"));

        foodDistributionChart = new FoodDistributionChartPanel();
        requestStatusChart = new RequestStatusChartPanel();
        foodStatusOverviewPanel = new FoodStatusOverviewPanel();
        rescueImpactPanel = new RescueImpactPanel();

        JPanel analyticsGrid = new JPanel(new GridLayout(2, 2, 18, 18));
        analyticsGrid.setOpaque(false);
        analyticsGrid.add(foodDistributionChart);
        analyticsGrid.add(requestStatusChart);
        analyticsGrid.add(foodStatusOverviewPanel);
        analyticsGrid.add(rescueImpactPanel);

        JPanel dashboardBody = new JPanel();
        dashboardBody.setOpaque(false);
        dashboardBody.setLayout(new BoxLayout(dashboardBody, BoxLayout.Y_AXIS));
        dashboardBody.add(statsPanel);
        dashboardBody.add(Box.createVerticalStrut(18));
        dashboardBody.add(analyticsGrid);

        root.add(dashboardBody, BorderLayout.CENTER);
        root.add(createNavigationPanel(), BorderLayout.SOUTH);
        add(root);

        refreshStatistics();
        UiStyle.styleFrame(this);
    }

    private JPanel createStatCard(String title, String description) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(18, 16, 16, 16));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(UITheme.BODY_BOLD_FONT);
        titleLabel.setForeground(UITheme.MUTED);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel valueLabel = new JLabel("0", SwingConstants.CENTER);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(UITheme.BRAND);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel detailLabel = new JLabel(description, SwingConstants.CENTER);
        detailLabel.setFont(UITheme.SMALL_FONT);
        detailLabel.setForeground(UITheme.MUTED);
        detailLabel.setHorizontalAlignment(SwingConstants.CENTER);

        statisticValues.put(title, valueLabel);
        statisticDetails.put(title, detailLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(detailLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new GridLayout(2, 3, 12, 12));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        navigationPanel.setOpaque(false);

        navigationPanel.add(createActionButton("ADD FOOD"));
        navigationPanel.add(createActionButton("AVAILABLE FOOD"));
        navigationPanel.add(createActionButton("ANIMAL FOOD"));
        navigationPanel.add(createActionButton("REQUESTS"));
        navigationPanel.add(createActionButton("HISTORY"));
        navigationPanel.add(createActionButton("LOGOUT"));
        return navigationPanel;
    }

    private JButton createActionButton(String label) {
        return createActionButton(label, event -> handleNavigation(label));
    }

    private JButton createActionButton(String label, java.awt.event.ActionListener actionListener) {
        JButton button = new JButton(label);
        button.setFont(UITheme.BODY_BOLD_FONT);
        button.addActionListener(actionListener);
        return button;
    }

    private void handleNavigation(String action) {
        switch (action) {
            case "ADD FOOD":
                openAddFoodScreen();
                break;
            case "AVAILABLE FOOD":
                openFoodListScreen();
                break;
            case "ANIMAL FOOD":
                openAnimalFoodScreen();
                break;
            case "REQUESTS":
                openRequestScreen();
                break;
            case "HISTORY":
                openHistoryScreen();
                break;
            case "LOGOUT":
                handleLogout();
                break;
            default:
                showComingSoonMessage("This feature will be available in the next stage.");
        }
    }

    private void openAddFoodScreen() {
        AddFoodFrame addFoodFrame = new AddFoodFrame();
        addFoodFrame.setVisible(true);
    }

    private void openFoodListScreen() {
        FoodListFrame foodListFrame = new FoodListFrame();
        foodListFrame.setVisible(true);
    }

    private void openAnimalFoodScreen() {
        AnimalFoodFrame animalFoodFrame = new AnimalFoodFrame();
        animalFoodFrame.setVisible(true);
    }

    private void openRequestScreen() {
        RequestFrame requestFrame = new RequestFrame();
        requestFrame.setVisible(true);
    }

    private void openHistoryScreen() {
        HistoryFrame historyFrame = new HistoryFrame();
        historyFrame.setVisible(true);
    }

    private void handleLogout() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        dispose();
    }

    private void showComingSoonMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void updateKpiCard(String title, String valueText, String detailText) {
        JLabel valueLabel = statisticValues.get(title);
        JLabel detailLabel = statisticDetails.get(title);
        if (valueLabel != null) {
            valueLabel.setText(valueText);
        }
        if (detailLabel != null) {
            detailLabel.setText(detailText);
        }
    }

    private void refreshStatistics() {
        List<Food> foods = foodService.getAllFoods();
        List<FoodRequest> requests = foodRequestService.getAllRequests();

        long totalQuantity = 0;
        long humanQuantity = 0;
        long animalQuantity = 0;
        long timeFinishedQuantity = 0;
        long animalReviewQuantity = 0;
        long availableHumanQuantity = 0;
        long availableAnimalQuantity = 0;
        long completedFoodQuantity = 0;
        long completedHumanQuantity = 0;
        long completedAnimalQuantity = 0;
        long rejectedFoodQuantity = 0;
        double humanShare = 0d;
        double animalShare = 0d;

        Map<String, Long> foodStatusQuantities = new LinkedHashMap<>();
        for (Food.FoodStatus status : Food.FoodStatus.values()) {
            foodStatusQuantities.put(status.name(), 0L);
        }

        for (Food food : foods) {
            int quantity = food.getQuantity();
            totalQuantity += quantity;

            if (food.getType() == Food.FoodType.HUMAN) {
                humanQuantity += quantity;
            } else if (food.getType() == Food.FoodType.ANIMAL) {
                animalQuantity += quantity;
            }

            if (food.getStatus() != null) {
                long statusTotal = foodStatusQuantities.getOrDefault(food.getStatus().name(), 0L) + quantity;
                foodStatusQuantities.put(food.getStatus().name(), statusTotal);
            }

            switch (food.getStatus()) {
                case AVAILABLE -> {
                    if (food.getType() == Food.FoodType.HUMAN) {
                        availableHumanQuantity += quantity;
                    } else if (food.getType() == Food.FoodType.ANIMAL) {
                        availableAnimalQuantity += quantity;
                    }
                }
                case TIME_FINISHED -> timeFinishedQuantity += quantity;
                case ANIMAL_REVIEW -> animalReviewQuantity += quantity;
                case COMPLETED -> {
                    completedFoodQuantity += quantity;
                    if (food.getType() == Food.FoodType.HUMAN) {
                        completedHumanQuantity += quantity;
                    } else if (food.getType() == Food.FoodType.ANIMAL) {
                        completedAnimalQuantity += quantity;
                    }
                }
                case REJECTED -> rejectedFoodQuantity += quantity;
                default -> {
                }
            }
        }

        Map<String, Integer> requestStatuses = new LinkedHashMap<>();
        for (String status : List.of("PENDING", "APPROVED", "COMPLETED", "REJECTED")) {
            requestStatuses.put(status, 0);
        }
        for (FoodRequest request : requests) {
            if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
                continue;
            }
            String normalized = request.getStatus().trim().toUpperCase(Locale.ROOT);
            if (requestStatuses.containsKey(normalized)) {
                requestStatuses.put(normalized, requestStatuses.get(normalized) + 1);
            }
        }

        int pendingRequestCount = requestStatuses.getOrDefault("PENDING", 0);
        int approvedRequestCount = requestStatuses.getOrDefault("APPROVED", 0);
        int completedRequestCount = requestStatuses.getOrDefault("COMPLETED", 0);
        int rejectedRequestCount = requestStatuses.getOrDefault("REJECTED", 0);
        int totalRequestCount = pendingRequestCount + approvedRequestCount + completedRequestCount + rejectedRequestCount;

        if (totalQuantity > 0) {
            humanShare = (humanQuantity * 100.0d) / totalQuantity;
            animalShare = (animalQuantity * 100.0d) / totalQuantity;
        }

        updateKpiCard("TOTAL FOOD", formatQuantity(totalQuantity), "All recorded food");
        updateKpiCard("HUMAN FOOD", formatQuantity(humanQuantity), "All recorded human food");
        updateKpiCard("ANIMAL FOOD", formatQuantity(animalQuantity), "All recorded animal food");
        updateKpiCard("PENDING REQUESTS", String.valueOf(pendingRequestCount), "Awaiting approval");
        updateKpiCard("COMPLETED REQUESTS", String.valueOf(completedRequestCount), "Successfully fulfilled");
        updateKpiCard("TIME FINISHED", formatQuantity(timeFinishedQuantity), "Expired food");
        updateKpiCard("ANIMAL REVIEW", formatQuantity(animalReviewQuantity), "Needs review");

        long availableTotalQuantity = availableHumanQuantity + availableAnimalQuantity;
        foodDistributionChart.setData(availableHumanQuantity, availableAnimalQuantity, availableTotalQuantity);
        requestStatusChart.setData(requestStatuses);
        foodStatusOverviewPanel.setData(foodStatusQuantities, List.of(
                Food.FoodStatus.AVAILABLE,
                Food.FoodStatus.TIME_FINISHED,
                Food.FoodStatus.ANIMAL_REVIEW,
                Food.FoodStatus.COMPLETED,
                Food.FoodStatus.REJECTED
        ));
        rescueImpactPanel.setData(
                completedFoodQuantity,
                totalQuantity,
                completedRequestCount,
                completedHumanQuantity,
                completedAnimalQuantity,
                pendingRequestCount,
                approvedRequestCount,
                rejectedRequestCount,
                totalRequestCount,
                humanShare,
                animalShare,
                safePercentage(completedRequestCount, totalRequestCount),
                safePercentage(pendingRequestCount, totalRequestCount)
        );
    }

    private static String formatQuantity(long value) {
        return value + " KG";
    }

    private static double safePercentage(double numerator, double denominator) {
        if (denominator <= 0d) {
            return 0d;
        }
        double percentage = (numerator / denominator) * 100d;
        if (Double.isNaN(percentage) || Double.isInfinite(percentage)) {
            return 0d;
        }
        return Math.max(0d, Math.min(100d, percentage));
    }

    private static final class FoodDistributionChartPanel extends JPanel {
        private long humanQuantity;
        private long animalQuantity;
        private long totalQuantity;

        FoodDistributionChartPanel() {
            setOpaque(true);
            setBackground(UITheme.SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(1, 1, 1, 1),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
            ));
            setLayout(new BorderLayout(10, 12));
            setPreferredSize(new Dimension(360, 230));
            setMinimumSize(new Dimension(280, 200));
        }

        void setData(long humanQuantity, long animalQuantity, long totalQuantity) {
            this.humanQuantity = humanQuantity;
            this.animalQuantity = animalQuantity;
            this.totalQuantity = totalQuantity;
            rebuildContents();
        }

        private void rebuildContents() {
            removeAll();

            JLabel title = new JLabel("Available Food Distribution");
            title.setFont(UITheme.BODY_BOLD_FONT);
            title.setForeground(UITheme.TEXT);

            JLabel summary = new JLabel("Available total: " + totalQuantity + " KG");
            summary.setFont(UITheme.SMALL_FONT);
            summary.setForeground(UITheme.MUTED);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.add(title, BorderLayout.WEST);
            header.add(summary, BorderLayout.EAST);

            JPanel dataPanel = new JPanel();
            dataPanel.setOpaque(false);
            dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.add(createDistributionRow("HUMAN FOOD", humanQuantity, totalQuantity, UITheme.BRAND));
            dataPanel.add(Box.createVerticalStrut(12));
            dataPanel.add(createDistributionRow("ANIMAL FOOD", animalQuantity, totalQuantity, UITheme.SUCCESS));

            add(header, BorderLayout.NORTH);
            add(dataPanel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        private JPanel createDistributionRow(String labelText, long value, long total, Color color) {
            JPanel row = new JPanel();
            row.setOpaque(false);
            row.setLayout(new GridBagLayout());
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 6, 10);

            JLabel label = new JLabel(labelText);
            label.setFont(UITheme.SMALL_FONT);
            label.setForeground(UITheme.MUTED);
            row.add(label, gbc);

            JLabel percentLabel = new JLabel(formatPercentage(safePercentage(value, total)) + "%");
            percentLabel.setFont(UITheme.SMALL_FONT);
            percentLabel.setForeground(UITheme.TEXT);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            row.add(percentLabel, gbc);

            JLabel quantityLabel = new JLabel(value + " KG");
            quantityLabel.setFont(UITheme.BODY_BOLD_FONT);
            quantityLabel.setForeground(UITheme.TEXT);
            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            row.add(quantityLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 3;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 0, 0);

            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setValue((int) Math.round(safePercentage(value, total)));
            progressBar.setStringPainted(false);
            progressBar.setForeground(color);
            progressBar.setBackground(new Color(41, 60, 52));
            progressBar.setBorderPainted(false);
            progressBar.setPreferredSize(new Dimension(10, 14));
            progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
            row.add(progressBar, gbc);

            return row;
        }

        private String formatPercentage(double value) {
            return String.format(Locale.US, "%.0f", value);
        }
    }

    private static final class RequestStatusChartPanel extends JPanel {
        private final Map<String, Integer> statusCounts = new LinkedHashMap<>();

        RequestStatusChartPanel() {
            setOpaque(true);
            setBackground(UITheme.SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(1, 1, 1, 1),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
            ));
            for (String status : List.of("PENDING", "APPROVED", "COMPLETED", "REJECTED")) {
                statusCounts.put(status, 0);
            }
        }

        void setData(Map<String, Integer> requestStatuses) {
            statusCounts.clear();
            for (String status : List.of("PENDING", "APPROVED", "COMPLETED", "REJECTED")) {
                statusCounts.put(status, requestStatuses.getOrDefault(status, 0));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int x = 14;
            int startY = 46;
            int barHeight = 18;
            int barWidth = Math.max(110, width - 140);
            int maxCount = 1;
            for (Integer count : statusCounts.values()) {
                maxCount = Math.max(maxCount, count);
            }

            g2.setColor(UITheme.TEXT);
            g2.setFont(UITheme.BODY_BOLD_FONT);
            g2.drawString("Request Status", 14, 20);

            int row = 0;
            for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
                int count = entry.getValue();
                int fillWidth = maxCount > 0 ? (int) Math.round((count * 1.0d / maxCount) * barWidth) : 0;
                int y = startY + row * 42;
                g2.setColor(UITheme.MUTED);
                g2.setFont(UITheme.SMALL_FONT);
                g2.drawString(entry.getKey(), x, y + 14);

                int barX = x + 96;
                g2.setColor(new Color(41, 60, 52));
                g2.fillRoundRect(barX, y - 2, barWidth, barHeight, 12, 12);
                g2.setColor(getStatusColor(entry.getKey()));
                g2.fillRoundRect(barX, y - 2, fillWidth, barHeight, 12, 12);

                g2.setColor(UITheme.TEXT);
                g2.setFont(UITheme.BODY_BOLD_FONT);
                g2.drawString(String.valueOf(count), barX + barWidth + 10, y + 14);
                row++;
            }
            g2.dispose();
        }

        private Color getStatusColor(String status) {
            return switch (status) {
                case "PENDING" -> UITheme.BRAND;
                case "APPROVED" -> UITheme.BRAND_HOVER;
                case "COMPLETED" -> UITheme.SUCCESS;
                case "REJECTED" -> UITheme.DANGER;
                default -> UITheme.MUTED;
            };
        }
    }

    private static final class FoodStatusOverviewPanel extends JPanel {
        private final Map<String, Long> statusTotals = new LinkedHashMap<>();
        private final List<Food.FoodStatus> orderedStatuses = new ArrayList<>();

        FoodStatusOverviewPanel() {
            setOpaque(true);
            setBackground(UITheme.SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(1, 1, 1, 1),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
            ));
            orderedStatuses.addAll(List.of(
                    Food.FoodStatus.AVAILABLE,
                    Food.FoodStatus.TIME_FINISHED,
                    Food.FoodStatus.ANIMAL_REVIEW,
                    Food.FoodStatus.COMPLETED,
                    Food.FoodStatus.REJECTED
            ));
        }

        void setData(Map<String, Long> foodStatusQuantities, List<Food.FoodStatus> statuses) {
            statusTotals.clear();
            for (Food.FoodStatus status : statuses) {
                String key = status.name();
                long value = foodStatusQuantities.getOrDefault(key, 0L);
                if (value > 0L || foodStatusQuantities.containsKey(key)) {
                    statusTotals.put(key, value);
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int maxValue = 1;
            for (Long value : statusTotals.values()) {
                maxValue = Math.max(maxValue, value.intValue());
            }

            g2.setColor(UITheme.TEXT);
            g2.setFont(UITheme.BODY_BOLD_FONT);
            g2.drawString("Food Status Overview", 14, 20);

            int y = 44;
            for (Map.Entry<String, Long> entry : statusTotals.entrySet()) {
                long value = entry.getValue();
                int barWidth = maxValue > 0 ? (int) Math.round((value * 1.0d / maxValue) * (width - 180)) : 0;
                g2.setColor(UITheme.MUTED);
                g2.setFont(UITheme.SMALL_FONT);
                g2.drawString(entry.getKey(), 14, y + 14);

                int barX = 150;
                g2.setColor(new Color(41, 60, 52));
                g2.fillRoundRect(barX, y - 2, Math.max(80, width - 180), 16, 10, 10);
                g2.setColor(getStatusColor(entry.getKey()));
                g2.fillRoundRect(barX, y - 2, Math.min(barWidth, Math.max(80, width - 180)), 16, 10, 10);

                g2.setColor(UITheme.TEXT);
                g2.setFont(UITheme.BODY_BOLD_FONT);
                g2.drawString(value + " KG", barX + Math.max(80, width - 180) + 12, y + 14);
                y += 28;
            }
            g2.dispose();
        }

        private Color getStatusColor(String status) {
            return switch (status) {
                case "AVAILABLE" -> UITheme.BRAND;
                case "TIME_FINISHED" -> UITheme.DANGER;
                case "ANIMAL_REVIEW" -> UITheme.BRAND_HOVER;
                case "COMPLETED" -> UITheme.SUCCESS;
                case "REJECTED" -> UITheme.ERROR;
                default -> UITheme.MUTED;
            };
        }
    }

    private static final class RescueImpactPanel extends JPanel {
        private long completedFoodQuantity;
        private long totalQuantity;
        private int completedRequestCount;
        private long completedHumanQuantity;
        private long completedAnimalQuantity;
        private int pendingRequestCount;
        private int approvedRequestCount;
        private int rejectedRequestCount;
        private int totalRequestCount;
        private double humanShare;
        private double animalShare;
        private double completionRate;
        private double pendingRate;

        RescueImpactPanel() {
            setOpaque(true);
            setBackground(UITheme.SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(1, 1, 1, 1),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
            ));
        }

        void setData(long completedFoodQuantity,
                     long totalQuantity,
                     int completedRequestCount,
                     long completedHumanQuantity,
                     long completedAnimalQuantity,
                     int pendingRequestCount,
                     int approvedRequestCount,
                     int rejectedRequestCount,
                     int totalRequestCount,
                     double humanShare,
                     double animalShare,
                     double completionRate,
                     double pendingRate) {
            this.completedFoodQuantity = completedFoodQuantity;
            this.totalQuantity = totalQuantity;
            this.completedRequestCount = completedRequestCount;
            this.completedHumanQuantity = completedHumanQuantity;
            this.completedAnimalQuantity = completedAnimalQuantity;
            this.pendingRequestCount = pendingRequestCount;
            this.approvedRequestCount = approvedRequestCount;
            this.rejectedRequestCount = rejectedRequestCount;
            this.totalRequestCount = totalRequestCount;
            this.humanShare = humanShare;
            this.animalShare = animalShare;
            this.completionRate = completionRate;
            this.pendingRate = pendingRate;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(UITheme.TEXT);
            g2.setFont(UITheme.BODY_BOLD_FONT);
            g2.drawString("Rescue Impact", 14, 20);

            int left = 14;
            int top = 38;
            int lineHeight = 26;
            g2.setFont(UITheme.SMALL_FONT);
            g2.setColor(UITheme.MUTED);

            drawMetric(g2, "FOOD RESCUED", completedFoodQuantity + " KG", left, top, true);
            drawMetric(g2, "HUMAN FOOD RESCUED", completedHumanQuantity + " KG", left, top + lineHeight * 1, false);
            drawMetric(g2, "ANIMAL FOOD RESCUED", completedAnimalQuantity + " KG", left, top + lineHeight * 2, false);
            drawMetric(g2, "COMPLETED REQUESTS", String.valueOf(completedRequestCount), left, top + lineHeight * 3, false);

            int barX = left;
            int barY = top + lineHeight * 4 + 10;
            int barWidth = Math.max(120, getWidth() - 40);
            drawPercentageRow(g2, "Human Food Share", humanShare, barX, barY, barWidth, UITheme.BRAND);
            drawPercentageRow(g2, "Animal Food Share", animalShare, barX, barY + 26, barWidth, UITheme.SUCCESS);
            drawPercentageRow(g2, "Completion Rate", completionRate, barX, barY + 52, barWidth, UITheme.BRAND_HOVER);
            drawPercentageRow(g2, "Pending Rate", pendingRate, barX, barY + 78, barWidth, UITheme.DANGER);

            g2.dispose();
        }

        private void drawMetric(Graphics2D g2, String label, String value, int x, int y, boolean accent) {
            g2.setColor(accent ? UITheme.BRAND : UITheme.MUTED);
            g2.drawString(label, x, y);
            g2.setColor(UITheme.TEXT);
            g2.setFont(UITheme.BODY_BOLD_FONT);
            g2.drawString(value, getWidth() - 120, y);
            g2.setFont(UITheme.SMALL_FONT);
        }

        private void drawPercentageRow(Graphics2D g2, String label, double percent, int x, int y, int width, Color color) {
            g2.setColor(UITheme.MUTED);
            g2.drawString(label, x, y + 12);
            g2.setColor(new Color(42, 60, 52));
            g2.fillRoundRect(x + 180, y, width - 190, 12, 8, 8);
            g2.setColor(color);
            int fillWidth = (int) Math.round((percent / 100d) * (width - 190));
            g2.fillRoundRect(x + 180, y, Math.min(fillWidth, width - 190), 12, 8, 8);
            g2.setColor(UITheme.TEXT);
            g2.drawString(String.format(Locale.US, "%.0f%%", percent), x + width - 56, y + 12);
        }
    }
}
