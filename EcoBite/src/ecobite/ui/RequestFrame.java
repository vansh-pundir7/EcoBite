package ecobite.ui;

import ecobite.model.Food;
import ecobite.model.FoodRequest;
import ecobite.service.FoodRequestService;
import ecobite.service.FoodService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestFrame extends JFrame {
    private static final DateTimeFormatter REQUEST_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FoodService foodService;
    private final FoodRequestService requestService;
    private final DefaultTableModel availableFoodModel;
    private final DefaultTableModel requestModel;
    private final JTable availableFoodTable;
    private final JTable requestTable;
    private final List<FoodRequest> displayedRequests;
    private final JButton approveButton;
    private final JButton rejectButton;
    private final JButton completeButton;

    public RequestFrame() {
        UiStyle.prepare();
        foodService = new FoodService();
        requestService = new FoodRequestService();
        displayedRequests = new ArrayList<>();

        setTitle("EcoBite - NGO Food Requests");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(UiStyle.createHeader("Human Food Requests", "Rescue food through verified NGO requests."), BorderLayout.NORTH);

        availableFoodModel = createTableModel(
                "Food ID", "Food Name", "Quantity", "Prepared Time", "Expiry Time", "Status");
        availableFoodTable = new JTable(availableFoodModel);
        configureTable(availableFoodTable);

        requestModel = createTableModel(
                "Food", "Quantity", "NGO / Organization", "Phone", "Request Type", "Request Time", "Status");
        requestTable = new JTable(requestModel);
        configureTable(requestTable);
        requestTable.getSelectionModel().addListSelectionListener(this::updateManagementButtons);

        JPanel availablePanel = UITheme.createCardPanel();
        availablePanel.setLayout(new BorderLayout(8, 8));
        availablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER), "Available HUMAN food"));
        availablePanel.add(new JScrollPane(availableFoodTable), BorderLayout.CENTER);
        JPanel availableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshFoodButton = new JButton("REFRESH FOOD");
        refreshFoodButton.addActionListener(event -> refreshAvailableFoods());
        JButton requestFoodButton = new JButton("REQUEST FOOD");
        requestFoodButton.addActionListener(event -> requestSelectedFood());
        availableActions.add(refreshFoodButton);
        availableActions.add(requestFoodButton);
        availablePanel.add(availableActions, BorderLayout.SOUTH);

        JPanel requestsPanel = UITheme.createCardPanel();
        requestsPanel.setLayout(new BorderLayout(8, 8));
        requestsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER), "NGO requests"));
        requestsPanel.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        JPanel requestActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        approveButton = new JButton("APPROVE");
        rejectButton = new JButton("REJECT");
        completeButton = new JButton("COMPLETE");
        approveButton.addActionListener(event -> updateSelectedRequest("APPROVED"));
        rejectButton.addActionListener(event -> updateSelectedRequest("REJECTED"));
        completeButton.addActionListener(event -> updateSelectedRequest("COMPLETED"));
        JButton refreshRequestsButton = new JButton("REFRESH REQUESTS");
        refreshRequestsButton.addActionListener(event -> refreshRequests());
        requestActions.add(approveButton);
        requestActions.add(rejectButton);
        requestActions.add(completeButton);
        requestActions.add(refreshRequestsButton);
        requestsPanel.add(requestActions, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, availablePanel, requestsPanel);
        splitPane.setResizeWeight(0.45);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(10);
        JPanel workArea = new JPanel(new BorderLayout(0, 12));
        workArea.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        workArea.add(createWorkflowStrip(), BorderLayout.NORTH);
        workArea.add(splitPane, BorderLayout.CENTER);
        add(workArea, BorderLayout.CENTER);

        JButton backButton = new JButton("BACK");
        backButton.addActionListener(event -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 10, 20));
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTables();
        updateManagementButtons(null);
        UiStyle.styleFrame(this);
    }

    private JPanel createWorkflowStrip() {
        JPanel workflow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        workflow.setBackground(UITheme.SURFACE);
        workflow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        String[] steps = {"FOOD", "NGO REQUEST", "PENDING", "APPROVED / REJECTED", "COMPLETED"};
        for (int index = 0; index < steps.length; index++) {
            JLabel step = new JLabel(steps[index]);
            step.setFont(UITheme.BODY_BOLD_FONT);
            step.setForeground(index == 0 ? UITheme.BRAND : UITheme.TEXT);
            workflow.add(step);
            if (index < steps.length - 1) {
                JLabel arrow = new JLabel("\u2192");
                arrow.setFont(UITheme.BODY_BOLD_FONT);
                arrow.setForeground(UITheme.MUTED);
                workflow.add(arrow);
            }
        }
        return workflow;
    }

    private DefaultTableModel createTableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void configureTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void refreshTables() {
        refreshAvailableFoods();
        refreshRequests();
    }

    private void refreshAvailableFoods() {
        availableFoodModel.setRowCount(0);
        for (Food food : foodService.getAvailableFoods()) {
            if (food.getType() == Food.FoodType.HUMAN && food.getStatus() == Food.FoodStatus.AVAILABLE) {
                availableFoodModel.addRow(new Object[]{
                        food.getId(),
                        food.getFoodName(),
                        food.getQuantity(),
                        food.getPreparedTime(),
                        food.getExpiryTime(),
                        food.getStatus()
                });
            }
        }
    }

    private void refreshRequests() {
        requestModel.setRowCount(0);
        displayedRequests.clear();

        List<Food> foods = foodService.getAllFoods();
        List<FoodRequest> requests = requestService.getAllRequests();
        for (FoodRequest request : requests) {
            Food food = findFood(foods, request.getFoodId());
            displayedRequests.add(request);
            requestModel.addRow(new Object[]{
                    request.getFoodName(),
                    food == null ? "N/A" : food.getQuantity(),
                    request.getOrganizationName(),
                    request.getPhone(),
                    request.getRequestType(),
                    request.getRequestTime(),
                    request.getStatus()
            });
        }
        updateManagementButtons(null);
    }

    private Food findFood(List<Food> foods, int foodId) {
        for (Food food : foods) {
            if (food.getId() == foodId) {
                return food;
            }
        }
        return null;
    }

    private void requestSelectedFood() {
        int selectedRow = availableFoodTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an available HUMAN food record first.");
            return;
        }

        int foodId = (int) availableFoodModel.getValueAt(availableFoodTable.convertRowIndexToModel(selectedRow), 0);
        Food food = foodService.findFoodById(foodId);
        if (food == null || food.getType() != Food.FoodType.HUMAN
                || food.getStatus() != Food.FoodStatus.AVAILABLE) {
            JOptionPane.showMessageDialog(this, "Only available HUMAN food can be requested.",
                    "Food No Longer Available", JOptionPane.ERROR_MESSAGE);
            refreshAvailableFoods();
            return;
        }

        JTextField receiverNameField = new JTextField(22);
        JTextField organizationField = new JTextField(22);
        JTextField phoneField = new JTextField(22);
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Receiver Name:"));
        form.add(receiverNameField);
        form.add(new JLabel("NGO / Organization Name:"));
        form.add(organizationField);
        form.add(new JLabel("Phone Number:"));
        form.add(phoneField);
        UiStyle.styleComponentTree(form);

        int result = JOptionPane.showConfirmDialog(this, form, "Request " + food.getFoodName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String receiverName = receiverNameField.getText().trim();
        String organizationName = organizationField.getText().trim();
        String phone = phoneField.getText().trim();
        if (receiverName.isEmpty() || organizationName.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Receiver name, organization name, and phone number are required.",
                    "Missing Request Details", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Enter a valid phone number containing 7 to 15 digits.",
                    "Invalid Phone Number", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FoodRequest request = new FoodRequest(
                generateRequestId(),
                food.getId(),
                food.getFoodName(),
                receiverName,
                organizationName,
                phone,
                FoodRequest.RequestType.NGO,
                LocalDateTime.now().format(REQUEST_TIME_FORMATTER),
                "PENDING"
        );

        try {
            foodService.updateFoodStatus(food.getId(), Food.FoodStatus.PENDING);
            try {
                requestService.addRequest(request);
            } catch (RuntimeException requestSaveError) {
                foodService.updateFoodStatus(food.getId(), Food.FoodStatus.AVAILABLE);
                throw requestSaveError;
            }
        } catch (RuntimeException e) {
            showStorageError(e);
            return;
        }

        refreshTables();
        JOptionPane.showMessageDialog(this, "Food request saved with PENDING status.");
    }

    private boolean isValidPhone(String phone) {
        if (!phone.matches("[0-9+()\\-\\s]+")) {
            return false;
        }
        int digitCount = phone.replaceAll("\\D", "").length();
        return digitCount >= 7 && digitCount <= 15;
    }

    private int generateRequestId() {
        int maxId = 0;
        for (FoodRequest request : requestService.getAllRequests()) {
            if (request.getRequestId() > maxId) {
                maxId = request.getRequestId();
            }
        }
        return maxId + 1;
    }

    private void updateManagementButtons(ListSelectionEvent event) {
        int selectedRow = requestTable == null ? -1 : requestTable.getSelectedRow();
        FoodRequest selectedRequest = getSelectedRequest(selectedRow);
        String status = selectedRequest == null ? null : selectedRequest.getStatus();
        approveButton.setEnabled("PENDING".equals(status));
        rejectButton.setEnabled("PENDING".equals(status));
        completeButton.setEnabled("APPROVED".equals(status));
    }

    private FoodRequest getSelectedRequest(int selectedRow) {
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = requestTable.convertRowIndexToModel(selectedRow);
        return modelRow < displayedRequests.size() ? displayedRequests.get(modelRow) : null;
    }

    private void updateSelectedRequest(String nextStatus) {
        FoodRequest request = getSelectedRequest(requestTable.getSelectedRow());
        if (request == null) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }

        String currentStatus = request.getStatus();
        boolean validTransition = "PENDING".equals(currentStatus)
                && ("APPROVED".equals(nextStatus) || "REJECTED".equals(nextStatus))
                || "APPROVED".equals(currentStatus) && "COMPLETED".equals(nextStatus);
        if (!validTransition) {
            JOptionPane.showMessageDialog(this, "That status transition is not allowed.",
                    "Invalid Request Status", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("REJECTED".equals(nextStatus)) {
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Reject this NGO request?", "Confirm Rejection",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            requestService.updateRequestStatus(request.getRequestId(), nextStatus);
            if ("REJECTED".equals(nextStatus)) {
                try {
                    foodService.updateFoodStatus(request.getFoodId(), Food.FoodStatus.AVAILABLE);
                } catch (RuntimeException foodUpdateError) {
                    requestService.updateRequestStatus(request.getRequestId(), "PENDING");
                    throw foodUpdateError;
                }
            } else if ("COMPLETED".equals(nextStatus)) {
                try {
                    foodService.updateFoodStatus(request.getFoodId(), Food.FoodStatus.COMPLETED);
                } catch (RuntimeException foodUpdateError) {
                    requestService.updateRequestStatus(request.getRequestId(), "APPROVED");
                    throw foodUpdateError;
                }
            }
        } catch (RuntimeException e) {
            showStorageError(e);
            refreshTables();
            return;
        }

        refreshTables();
        JOptionPane.showMessageDialog(this, "Request status updated to " + nextStatus + ".");
    }

    private void showStorageError(RuntimeException error) {
        String message = error.getMessage() == null ? error.toString() : error.getMessage();
        if (error.getCause() != null && error.getCause().getMessage() != null) {
            message += "\n" + error.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(this, message, "Unable to Save Request", JOptionPane.ERROR_MESSAGE);
    }
}