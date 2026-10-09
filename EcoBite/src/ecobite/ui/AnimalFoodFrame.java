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

public class AnimalFoodFrame extends JFrame {
    private static final DateTimeFormatter REQUEST_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FoodService foodService;
    private final FoodRequestService requestService;
    private final DefaultTableModel animalFoodModel;
    private final DefaultTableModel shelterRequestModel;
    private final JTable animalFoodTable;
    private final JTable shelterRequestTable;
    private final List<FoodRequest> displayedShelterRequests;
    private final JButton approveButton;
    private final JButton rejectButton;
    private final JButton completeButton;

    public AnimalFoodFrame() {
        UiStyle.prepare();
        foodService = new FoodService();
        requestService = new FoodRequestService();
        displayedShelterRequests = new ArrayList<>();

        setTitle("EcoBite - Animal Food Rescue");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(UiStyle.createHeader("Animal Food Rescue", "Redirect surplus food to animal shelters."), BorderLayout.NORTH);

        animalFoodModel = createTableModel(
                "Food ID", "Food Name", "Quantity", "Prepared Time", "Expiry Time", "Status");
        animalFoodTable = new JTable(animalFoodModel);
        configureTable(animalFoodTable);

        shelterRequestModel = createTableModel(
                "Food", "Quantity", "Shelter", "Contact Person", "Phone", "Request Time", "Status");
        shelterRequestTable = new JTable(shelterRequestModel);
        configureTable(shelterRequestTable);
        shelterRequestTable.getSelectionModel().addListSelectionListener(this::updateManagementButtons);

        JPanel animalFoodPanel = UITheme.createCardPanel();
        animalFoodPanel.setLayout(new BorderLayout(8, 8));
        animalFoodPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER), "Available ANIMAL food"));
        animalFoodPanel.add(new JScrollPane(animalFoodTable), BorderLayout.CENTER);
        JPanel animalActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshFoodButton = new JButton("REFRESH FOOD");
        refreshFoodButton.addActionListener(event -> refreshAvailableAnimalFood());
        JButton assignButton = new JButton("ASSIGN TO SHELTER");
        assignButton.addActionListener(event -> assignSelectedFood());
        animalActions.add(refreshFoodButton);
        animalActions.add(assignButton);
        animalFoodPanel.add(animalActions, BorderLayout.SOUTH);

        JPanel requestPanel = UITheme.createCardPanel();
        requestPanel.setLayout(new BorderLayout(8, 8));
        requestPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER), "Shelter assignments"));
        requestPanel.add(new JScrollPane(shelterRequestTable), BorderLayout.CENTER);
        JPanel requestActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        approveButton = new JButton("APPROVE");
        rejectButton = new JButton("REJECT");
        completeButton = new JButton("COMPLETE");
        approveButton.addActionListener(event -> updateSelectedRequest("APPROVED"));
        rejectButton.addActionListener(event -> updateSelectedRequest("REJECTED"));
        completeButton.addActionListener(event -> updateSelectedRequest("COMPLETED"));
        JButton refreshRequestsButton = new JButton("REFRESH REQUESTS");
        refreshRequestsButton.addActionListener(event -> refreshShelterRequests());
        requestActions.add(approveButton);
        requestActions.add(rejectButton);
        requestActions.add(completeButton);
        requestActions.add(refreshRequestsButton);
        requestPanel.add(requestActions, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, animalFoodPanel, requestPanel);
        splitPane.setResizeWeight(0.45);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(10);
        JPanel workArea = new JPanel(new BorderLayout());
        workArea.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
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
        refreshAvailableAnimalFood();
        refreshShelterRequests();
    }

    private void refreshAvailableAnimalFood() {
        animalFoodModel.setRowCount(0);
        for (Food food : foodService.getAvailableFoods()) {
            if (food.getType() == Food.FoodType.ANIMAL && food.getStatus() == Food.FoodStatus.AVAILABLE) {
                animalFoodModel.addRow(new Object[]{
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

    private void refreshShelterRequests() {
        shelterRequestModel.setRowCount(0);
        displayedShelterRequests.clear();

        List<Food> foods = foodService.getAllFoods();
        for (FoodRequest request : requestService.getAllRequests()) {
            if (request.getRequestType() != FoodRequest.RequestType.SHELTER) {
                continue;
            }
            Food food = findFood(foods, request.getFoodId());
            displayedShelterRequests.add(request);
            shelterRequestModel.addRow(new Object[]{
                    request.getFoodName(),
                    food == null ? "N/A" : food.getQuantity(),
                    request.getOrganizationName(),
                    request.getRequesterName(),
                    request.getPhone(),
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

    private void assignSelectedFood() {
        int selectedRow = animalFoodTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an available ANIMAL food record first.");
            return;
        }

        int modelRow = animalFoodTable.convertRowIndexToModel(selectedRow);
        int foodId = (int) animalFoodModel.getValueAt(modelRow, 0);
        Food food = foodService.findFoodById(foodId);
        if (food == null || food.getType() != Food.FoodType.ANIMAL
                || food.getStatus() != Food.FoodStatus.AVAILABLE) {
            JOptionPane.showMessageDialog(this, "Only available ANIMAL food can be assigned to a shelter.",
                    "Food No Longer Available", JOptionPane.ERROR_MESSAGE);
            refreshAvailableAnimalFood();
            return;
        }

        JTextField shelterNameField = new JTextField(22);
        JTextField contactPersonField = new JTextField(22);
        JTextField phoneField = new JTextField(22);
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Shelter Name:"));
        form.add(shelterNameField);
        form.add(new JLabel("Contact Person:"));
        form.add(contactPersonField);
        form.add(new JLabel("Phone Number:"));
        form.add(phoneField);
        UiStyle.styleComponentTree(form);

        int result = JOptionPane.showConfirmDialog(this, form, "Assign " + food.getFoodName() + " to Shelter",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String shelterName = shelterNameField.getText().trim();
        String contactPerson = contactPersonField.getText().trim();
        String phone = phoneField.getText().trim();
        if (shelterName.isEmpty() || contactPerson.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Shelter name, contact person, and phone number are required.",
                    "Missing Shelter Details", JOptionPane.ERROR_MESSAGE);
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
                contactPerson,
                shelterName,
                phone,
                FoodRequest.RequestType.SHELTER,
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
        JOptionPane.showMessageDialog(this, "Shelter assignment saved with PENDING status.");
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
        FoodRequest selectedRequest = getSelectedRequest(shelterRequestTable.getSelectedRow());
        String status = selectedRequest == null ? null : selectedRequest.getStatus();
        approveButton.setEnabled("PENDING".equals(status));
        rejectButton.setEnabled("PENDING".equals(status));
        completeButton.setEnabled("APPROVED".equals(status));
    }

    private FoodRequest getSelectedRequest(int selectedRow) {
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = shelterRequestTable.convertRowIndexToModel(selectedRow);
        return modelRow < displayedShelterRequests.size() ? displayedShelterRequests.get(modelRow) : null;
    }

    private void updateSelectedRequest(String nextStatus) {
        FoodRequest request = getSelectedRequest(shelterRequestTable.getSelectedRow());
        if (request == null) {
            JOptionPane.showMessageDialog(this, "Select a shelter request first.");
            return;
        }

        String currentStatus = request.getStatus();
        boolean validTransition = "PENDING".equals(currentStatus)
                && ("APPROVED".equals(nextStatus) || "REJECTED".equals(nextStatus))
                || "APPROVED".equals(currentStatus) && "COMPLETED".equals(nextStatus);
        if (!validTransition) {
            JOptionPane.showMessageDialog(this, "That shelter request status transition is not allowed.",
                    "Invalid Request Status", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("REJECTED".equals(nextStatus)) {
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Reject this shelter request?", "Confirm Rejection",
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
        JOptionPane.showMessageDialog(this, "Shelter request status updated to " + nextStatus + ".");
    }

    private void showStorageError(RuntimeException error) {
        String message = error.getMessage() == null ? error.toString() : error.getMessage();
        if (error.getCause() != null && error.getCause().getMessage() != null) {
            message += "\n" + error.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(this, message, "Unable to Save Shelter Assignment",
                JOptionPane.ERROR_MESSAGE);
    }
}