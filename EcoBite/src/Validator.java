/** Contains small input checks shared by EcoBite forms. */
final class Validator {

    /** Prevents creating instances of this utility class. */
    private Validator() {
    }

    /** Returns a clear error when a food name is empty or longer than 50 characters. */
    static String foodName(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Enter a food name.";
        }
        return value.trim().length() > 50 ? "Food name must be 50 characters or fewer." : null;
    }

    /** Returns a clear error when the quantity is invalid or greater than 100,000 KG. */
    static String quantity(String value) {
        try {
            double quantity = Double.parseDouble(value.trim());
            return Double.isFinite(quantity) && quantity > 0 && quantity <= 100_000
                    ? null : "Quantity must be greater than 0 and no more than 100,000 KG.";
        } catch (RuntimeException ex) {
            return "Enter a valid quantity greater than 0.";
        }
    }

    /** Returns a clear error when a required name field is empty. */
    static String required(String value, String label) {
        return value == null || value.trim().isEmpty() ? label + " cannot be empty." : null;
    }

    /** Returns a clear error unless the phone number has exactly ten digits. */
    static String phone(String value) {
        return value != null && value.matches("\\d{10}")
                ? null : "Phone number must contain exactly 10 digits.";
    }
}
