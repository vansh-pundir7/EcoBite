package ecobite.ui;

import ecobite.model.Food;
import ecobite.service.FoodService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddFoodFrame extends JFrame {
    private final FoodService foodService;
    private final JTextField foodNameField;
    private final JTextField quantityField;
    private final JComboBox<Food.FoodType> foodTypeComboBox;
    private final JTextField preparedTimeField;
    private final JTextField expiryTimeField;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    public AddFoodFrame() {
        UiStyle.prepare();
        this.foodService = new FoodService();

        setTitle(UITheme.APP_NAME);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 680);
        setMinimumSize(new Dimension(680, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(22, 28, 24, 28));
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 22));
        formCard.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel formHeading = new JPanel();
        formHeading.setOpaque(false);
        formHeading.setLayout(new BoxLayout(formHeading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Food details");
        title.setFont(UITheme.SECTION_FONT);
        title.setForeground(UITheme.TEXT);
        JLabel description = new JLabel("Add surplus food so it can reach the right community.");
        description.setFont(UITheme.BODY_FONT);
        description.setForeground(UITheme.MUTED);
        formHeading.add(title);
        formHeading.add(Box.createVerticalStrut(UITheme.SPACE_XS));
        formHeading.add(description);
        formCard.add(formHeading, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 8, 9, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        addFieldLabel(formPanel, "Food Name", gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        foodNameField = new JTextField(20);
        formPanel.add(foodNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        addFieldLabel(formPanel, "Quantity (KG)", gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        quantityField = new JTextField(20);
        formPanel.add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        addFieldLabel(formPanel, "Food Type", gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        foodTypeComboBox = new JComboBox<>(Food.FoodType.values());
        UITheme.styleComboBox(foodTypeComboBox);
        formPanel.add(foodTypeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        addFieldLabel(formPanel, "Prepared Time", gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        preparedTimeField = new JTextField(20);
        preparedTimeField.setToolTipText("Use yyyy-MM-dd HH:mm or a relative age such as 5h, 30m, or 2d.");
        formPanel.add(preparedTimeField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        addFieldLabel(formPanel, "Expiry Time", gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        expiryTimeField = new JTextField(20);
        expiryTimeField.setToolTipText("Use yyyy-MM-dd HH:mm or a date such as yyyy/MM/dd.");
        formPanel.add(expiryTimeField, gbc);

        JLabel timeHint = new JLabel("24-hour time · yyyy-MM-dd HH:mm");
        timeHint.setFont(UITheme.SMALL_FONT);
        timeHint.setForeground(UITheme.MUTED);
        gbc.gridx = 1;
        gbc.gridy++;
        gbc.insets = new Insets(0, 8, 0, 8);
        formPanel.add(timeHint, gbc);
        formCard.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton saveButton = new JButton("SAVE FOOD");
        JButton cancelButton = new JButton("BACK");

        saveButton.addActionListener(e -> saveFood());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        formCard.add(buttonPanel, BorderLayout.SOUTH);

        GridBagConstraints contentConstraints = new GridBagConstraints();
        contentConstraints.fill = GridBagConstraints.BOTH;
        contentConstraints.weightx = 1;
        contentConstraints.weighty = 1;
        content.add(formCard, contentConstraints);
        add(UiStyle.createHeader("Add Food", "Rescue surplus food before it becomes waste."), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        UiStyle.styleFrame(this);
    }

    private void addFieldLabel(JPanel panel, String text, GridBagConstraints constraints) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.BODY_BOLD_FONT);
        label.setForeground(UITheme.MUTED);
        panel.add(label, constraints);
    }

    private void saveFood() {
        String foodName = foodNameField.getText() == null ? "" : foodNameField.getText().trim();
        String quantityText = quantityField.getText() == null ? "" : quantityField.getText().trim();
        Food.FoodType type = (Food.FoodType) foodTypeComboBox.getSelectedItem();
        String preparedInput = preparedTimeField.getText() == null ? "" : preparedTimeField.getText().trim();
        String expiryInput = expiryTimeField.getText() == null ? "" : expiryTimeField.getText().trim();

        if (foodName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Food name cannot be empty.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive number.");
            return;
        }

        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive number.");
            return;
        }

        if (type == null) {
            JOptionPane.showMessageDialog(this, "Food type must be selected.");
            return;
        }

        String preparedTime = normalizePreparedTime(preparedInput);
        if (preparedTime == null) {
            JOptionPane.showMessageDialog(this,
                "Enter Prepared Time as yyyy-MM-dd HH:mm or a relative age such as 5h, 30m, or 2d.");
            return;
        }

        String expiryTime = normalizeExpiryTime(expiryInput);
        if (expiryTime == null) {
            JOptionPane.showMessageDialog(this,
                "Enter Expiry Time as yyyy-MM-dd HH:mm or a valid date such as yyyy/MM/dd.");
            return;
        }

        if (!isAfter(preparedTime, expiryTime)) {
            JOptionPane.showMessageDialog(this, "Expiry time must be after prepared time.");
            return;
        }

        try {
            int foodId = generateFoodId();
            Food food = new Food(foodId, foodName, quantity, type, preparedTime, expiryTime,
                    Food.FoodStatus.AVAILABLE);
            foodService.addFood(food);
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? e.toString() : e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                message += "\n" + e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(this, message, "Unable to Save Food", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Food saved successfully.");
        clearForm();
    }

    private void clearForm() {
        foodNameField.setText("");
        quantityField.setText("");
        foodTypeComboBox.setSelectedIndex(0);
        preparedTimeField.setText("");
        expiryTimeField.setText("");
    }

    private int generateFoodId() {
        int maxId = 0;
        for (Food food : foodService.getAllFoods()) {
            if (food.getId() > maxId) {
                maxId = food.getId();
            }
        }
        return maxId + 1;
    }

    private String normalizePreparedTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        Matcher relativeAge = Pattern.compile("(?i)^(\\d+)\\s*(m|min|mins|minute|minutes|h|hr|hrs|hour|hours|d|day|days)$")
                .matcher(value.trim());
        if (relativeAge.matches()) {
            long amount;
            try {
                amount = Long.parseLong(relativeAge.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
            if (amount <= 0) {
                return null;
            }

            String unit = relativeAge.group(2).toLowerCase();
            LocalDateTime prepared = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            if (unit.startsWith("m")) {
                prepared = prepared.minusMinutes(amount);
            } else if (unit.startsWith("h")) {
                prepared = prepared.minusHours(amount);
            } else {
                prepared = prepared.minusDays(amount);
            }
            return DATE_TIME_FORMATTER.format(prepared);
        }

        for (DateTimeFormatter formatter : dateTimeFormatters()) {
            try {
                return DATE_TIME_FORMATTER.format(LocalDateTime.parse(value.trim(), formatter));
            } catch (DateTimeParseException ignored) {
                // Try the other documented date/time format.
            }
        }
        return null;
    }

    private String normalizeExpiryTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String trimmedValue = value.trim();
        for (DateTimeFormatter formatter : dateTimeFormatters()) {
            try {
                return DATE_TIME_FORMATTER.format(LocalDateTime.parse(trimmedValue, formatter));
            } catch (DateTimeParseException ignored) {
                // Try date-only formats next.
            }
        }

        List<DateTimeFormatter> dateFormatters = List.of(
                DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT),
                DateTimeFormatter.ofPattern("uuuu/MM/dd").withResolverStyle(ResolverStyle.STRICT)
        );
        for (DateTimeFormatter formatter : dateFormatters) {
            try {
                return DATE_TIME_FORMATTER.format(
                    java.time.LocalDate.parse(trimmedValue, formatter).atTime(23, 59));
            } catch (DateTimeParseException ignored) {
                // Reject input that is not one of the supported expiry formats.
            }
        }
        return null;
    }

    private List<DateTimeFormatter> dateTimeFormatters() {
        return List.of(
                DATE_TIME_FORMATTER,
                DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm").withResolverStyle(ResolverStyle.STRICT)
        );
    }

    private boolean isAfter(String preparedTime, String expiryTime) {
        try {
            LocalDateTime prepared = LocalDateTime.parse(preparedTime, DATE_TIME_FORMATTER);
            LocalDateTime expiry = LocalDateTime.parse(expiryTime, DATE_TIME_FORMATTER);
            return expiry.isAfter(prepared);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
