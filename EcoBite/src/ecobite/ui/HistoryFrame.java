package ecobite.ui;

import ecobite.model.Food;
import ecobite.model.FoodRequest;
import ecobite.service.FoodRequestService;
import ecobite.service.FoodService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryFrame extends JFrame {
    private final FoodService foodService;
    private final FoodRequestService requestService;
    private final DefaultTableModel historyModel;
    private final JTextField searchField;
    private final CardLayout historyLayout;
    private final JPanel historyContent;

    public HistoryFrame() {
        UiStyle.prepare();
        foodService = new FoodService();
        requestService = new FoodRequestService();

        setTitle(UITheme.APP_NAME);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1420, 780);
        setMinimumSize(new Dimension(1050, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        toolbar.setBorder(BorderFactory.createEmptyBorder(4, 18, 4, 18));
        toolbar.add(new JLabel("Search food"));
        searchField = new JTextField(24);
        searchField.setToolTipText("Search by food ID or name");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateSearch() {
                refreshHistory();
            }

            @Override
            public void insertUpdate(DocumentEvent event) {
                updateSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateSearch();
            }
        });
        toolbar.add(searchField);

        JButton refreshButton = new JButton("REFRESH");
        refreshButton.addActionListener(event -> refreshHistory());
        toolbar.add(refreshButton);

        JPanel screenHeader = new JPanel(new BorderLayout());
        screenHeader.add(UiStyle.createHeader(
                "Food Rescue History", "Track completed, rejected and expired food activity."), BorderLayout.NORTH);
        screenHeader.add(toolbar, BorderLayout.SOUTH);
        add(screenHeader, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(new String[]{
                "Food ID", "Food Name", "Quantity", "Type", "Prepared Time", "Expiry Time", "Status",
                "Request ID", "Organization / Shelter", "Request Type", "Request Time", "Request Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = new JTable(historyModel);
        historyTable.setFillsViewportHeight(true);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        historyLayout = new CardLayout();
        historyContent = new JPanel(historyLayout);
        historyContent.add(scrollPane, "results");
        JLabel emptyState = new JLabel("No historical food or request activity matches this search.",
                SwingConstants.CENTER);
        emptyState.setFont(UITheme.BODY_FONT);
        emptyState.setForeground(UITheme.MUTED);
        historyContent.add(emptyState, "empty");

        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tableCard.add(historyContent, BorderLayout.CENTER);
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(16, 22, 18, 22));
        center.add(tableCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JButton backButton = new JButton("BACK");
        backButton.addActionListener(event -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshHistory();
        UiStyle.styleFrame(this);
    }

    private void refreshHistory() {
        historyModel.setRowCount(0);

        String searchText = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase();
        Map<Integer, List<FoodRequest>> requestsByFoodId = new HashMap<>();
        for (FoodRequest request : requestService.getAllRequests()) {
            requestsByFoodId.computeIfAbsent(request.getFoodId(), ignored -> new ArrayList<>()).add(request);
        }

        for (Food food : foodService.getAllFoods()) {
            if (!matchesSearch(food, searchText)) {
                continue;
            }

            List<FoodRequest> relatedRequests = requestsByFoodId.getOrDefault(food.getId(), List.of());
            boolean historicalFood = isHistoricalFoodStatus(food.getStatus());
            boolean addedRow = false;
            for (FoodRequest request : relatedRequests) {
                if (historicalFood || isHistoricalRequestStatus(request.getStatus())) {
                    addHistoryRow(food, request);
                    addedRow = true;
                }
            }
            if (historicalFood && !addedRow) {
                addHistoryRow(food, null);
            }
        }
        historyLayout.show(historyContent, historyModel.getRowCount() == 0 ? "empty" : "results");
    }

    private boolean matchesSearch(Food food, String searchText) {
        return searchText.isEmpty()
                || String.valueOf(food.getId()).contains(searchText)
                || food.getFoodName() != null && food.getFoodName().toLowerCase().contains(searchText);
    }

    private boolean isHistoricalFoodStatus(Food.FoodStatus status) {
        return status == Food.FoodStatus.COMPLETED
                || status == Food.FoodStatus.REJECTED
                || status == Food.FoodStatus.TIME_FINISHED
                || status == Food.FoodStatus.ANIMAL_REVIEW;
    }

    private boolean isHistoricalRequestStatus(String status) {
        return "COMPLETED".equals(status) || "REJECTED".equals(status);
    }

    private void addHistoryRow(Food food, FoodRequest request) {
        historyModel.addRow(new Object[]{
                food.getId(),
                food.getFoodName(),
                food.getQuantity(),
                food.getType(),
                food.getPreparedTime(),
                food.getExpiryTime(),
                food.getStatus(),
                request == null ? "N/A" : request.getRequestId(),
                request == null ? "N/A" : request.getOrganizationName(),
                request == null ? "N/A" : request.getRequestType(),
                request == null ? "N/A" : request.getRequestTime(),
                request == null ? "N/A" : request.getStatus()
        });
    }
}