package ecobite.ui;

import ecobite.model.Food;
import ecobite.service.FoodService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FoodListFrame extends JFrame {
    private final FoodService foodService;
    private final JTable foodTable;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final JComboBox<String> filterComboBox;
    private final CardLayout resultsLayout;
    private final JPanel resultsPanel;

    public FoodListFrame() {
        this("AVAILABLE FOOD", "AVAILABLE");
    }

    public FoodListFrame(String heading, String initialFilter) {
        UiStyle.prepare();
        this.foodService = new FoodService();

        setTitle("EcoBite - " + heading);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1320, 760);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 18, 4, 18));
        JLabel searchLabel = new JLabel("Search food");
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by food ID or food name");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateSearch() {
                applyFilters();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSearch();
            }
        });

        JLabel filterLabel = new JLabel("Show");
        filterComboBox = new JComboBox<>(new String[]{
            "ALL", "HUMAN", "ANIMAL", "AVAILABLE", "PENDING", "APPROVED",
            "COMPLETED", "REJECTED", "TIME_FINISHED", "ANIMAL_REVIEW"
        });
        filterComboBox.addActionListener(e -> applyFilters());

        JButton refreshButton = new JButton("REFRESH");
        refreshButton.addActionListener(e -> applyFilters());
        JButton backButton = new JButton("BACK");
        backButton.addActionListener(e -> dispose());

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(filterLabel);
        topPanel.add(filterComboBox);
        topPanel.add(refreshButton);
        topPanel.add(backButton);

        JPanel screenHeader = new JPanel(new BorderLayout());
        screenHeader.add(UiStyle.createHeader(heading, "Search, filter and review food rescue status."), BorderLayout.NORTH);
        screenHeader.add(topPanel, BorderLayout.SOUTH);
        add(screenHeader, BorderLayout.NORTH);

        String[] columnNames = {
            "ID", "Food Name", "Quantity (KG)", "Type", "Prepared Time", "Expiry Time", "Status"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        foodTable = new JTable(tableModel);
        foodTable.setFillsViewportHeight(true);
        foodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(foodTable);
        resultsLayout = new CardLayout();
        resultsPanel = new JPanel(resultsLayout);
        resultsPanel.add(scrollPane, "results");
        JLabel emptyState = new JLabel("No food records match this search or filter.", SwingConstants.CENTER);
        emptyState.setFont(UITheme.BODY_FONT);
        emptyState.setForeground(UITheme.MUTED);
        resultsPanel.add(emptyState, "empty");

        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tableCard.add(resultsPanel, BorderLayout.CENTER);
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(16, 22, 18, 22));
        center.add(tableCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        filterComboBox.setSelectedItem(initialFilter);
        applyFilters();
        UiStyle.styleFrame(this);
        styleFilterComboBox();
    }

    private void styleFilterComboBox() {
        filterComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setFont(UITheme.BODY_FONT);
                label.setOpaque(true);
                label.setForeground(isSelected ? Color.WHITE : UITheme.TEXT);
                label.setBackground(isSelected ? UITheme.BRAND_DARK : UITheme.INPUT);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return label;
            }
        });

        FontMetrics fontMetrics = filterComboBox.getFontMetrics(filterComboBox.getFont());
        int widestOption = 0;
        for (int index = 0; index < filterComboBox.getItemCount(); index++) {
            widestOption = Math.max(widestOption,
                    fontMetrics.stringWidth(filterComboBox.getItemAt(index)));
        }
        Insets borderInsets = filterComboBox.getInsets();
        int width = widestOption + 56 + borderInsets.left + borderInsets.right;
        int height = Math.max(UITheme.CONTROL_HEIGHT, fontMetrics.getHeight() + 20
                + borderInsets.top + borderInsets.bottom);
        Dimension size = new Dimension(width, height);
        filterComboBox.setPreferredSize(size);
        filterComboBox.setMinimumSize(size);
    }

    private void applyFilters() {
        tableModel.setRowCount(0);

        String searchText = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedFilter = (String) filterComboBox.getSelectedItem();

        List<Food> foods = foodService.getAllFoods();
        for (Food food : foods) {
            if (!matchesSearch(food, searchText)) {
                continue;
            }

            if (!matchesFilter(food, selectedFilter)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    food.getId(),
                    food.getFoodName(),
                    food.getQuantity(),
                    food.getType(),
                    food.getPreparedTime(),
                    food.getExpiryTime(),
                    food.getStatus()
            });
        }
        resultsLayout.show(resultsPanel, tableModel.getRowCount() == 0 ? "empty" : "results");
    }

    private boolean matchesSearch(Food food, String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return true;
        }

        String idText = String.valueOf(food.getId());
        String nameText = food.getFoodName() == null ? "" : food.getFoodName().toLowerCase();

        return idText.contains(searchText) || nameText.contains(searchText);
    }

    private boolean matchesFilter(Food food, String selectedFilter) {
        if (selectedFilter == null || "ALL".equals(selectedFilter)) {
            return true;
        }

        return food.getType() != null && selectedFilter.equals(food.getType().name())
                || food.getStatus() != null && selectedFilter.equals(food.getStatus().name());
    }
}
