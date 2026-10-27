import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

/** Runs database-backed checks without opening Swing windows. */
public class SmokeTest {

    private static final String PREFIX = "Smoke-" + UUID.randomUUID().toString().substring(0, 8);

    /** Runs the core workflow checks and cleans up test rows. */
    public static void main(String[] args) throws Exception {
        boolean connected = false;
        try {
            PasswordHasher.ensureDefaultAdmin();
            try (Connection connection = DB.get()) {
                connected = true;
                pass("MySQL connection and default account");
                checkSchema(connection);
                checkValidators();
                checkPasswords(connection);
                runWorkflows(connection);
            }
            pass("Smoke test completed");
        } finally {
            if (connected) {
                cleanRows();
            }
        }
    }

    /** Confirms required request schema elements are installed. */
    private static void checkSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT created_at FROM requests WHERE 1=0").close();
        }
        DatabaseMetaData metadata = connection.getMetaData();
        boolean foreignKey = false;
        try (ResultSet keys = metadata.getImportedKeys(connection.getCatalog(), null, "requests")) {
            while (keys.next()) {
                if ("food".equalsIgnoreCase(keys.getString("PKTABLE_NAME"))
                        && "food_id".equalsIgnoreCase(keys.getString("FKCOLUMN_NAME"))) {
                    foreignKey = true;
                }
            }
        }
        check(foreignKey, "requests.food_id foreign key exists");
        pass("Schema");
    }

    /** Checks representative accepted and rejected form values. */
    private static void checkValidators() {
        check(Validator.foodName("Rice") == null, "valid food name");
        check(Validator.foodName("x".repeat(51)) != null, "food name length");
        check(Validator.quantity("5.5") == null, "valid quantity");
        check(Validator.quantity("0") != null && Validator.quantity("100001") != null,
                "quantity limits");
        check(Validator.phone("1234567890") == null
                && Validator.phone("12345") != null, "phone format");
        check(Validator.required(" ", "Name") != null, "required name");
        pass("Input validation");
    }

    /** Checks salted password hashing and confirms the default account can authenticate. */
    private static void checkPasswords(Connection connection) throws Exception {
        String hashed = PasswordHasher.hash("smoke-password");
        check(hashed.startsWith("sha256:"), "password format");
        check(PasswordHasher.verify("smoke-password", hashed), "correct password");
        check(!PasswordHasher.verify("wrong-password", hashed), "wrong password rejected");
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT password FROM users WHERE username='admin'");
             ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                throw new IllegalStateException("Default admin row is missing.");
            }
            String stored = result.getString(1);
            check(PasswordHasher.verify("admin123", stored), "default account login check");
            check(!PasswordHasher.verify("wrong-password", stored),
                    "default account rejects an incorrect password");
        }
        pass("Password hashing and login");
    }

    /** Exercises expiry, request decisions, shelter assignment, and history queries. */
    private static void runWorkflows(Connection connection) throws Exception {
        double[] before = DashboardFrame.queryStats(connection);
        int expired = insertFood(connection, "expired", "HUMAN", -60);
        int approve = insertFood(connection, "approve", "HUMAN", 180);
        int reject = insertFood(connection, "reject", "HUMAN", 180);
        int animal = insertFood(connection, "animal", "ANIMAL", 180);
        int discard = insertFood(connection, "discard", "HUMAN", -60);

        ExpiryService.checkExpiredFood();
        check("ANIMAL_REVIEW".equals(foodStatus(connection, expired)), "expiry review status");
        long now = System.currentTimeMillis();
        check(FoodListFrame.formatTimeLeft(new java.sql.Timestamp(now + 65_000), now)
                        .equals("00:01:05"), "countdown formatting");
        pass("Food expiry");

        int approvedRequest = insertRequest(connection, approve, "Community NGO");
        updateRequest(connection, approvedRequest, "COMPLETED", "COMPLETED");
        check("COMPLETED".equals(foodStatus(connection, approve)), "approved food status");
        int rejectedRequest = insertRequest(connection, reject, "Local NGO");
        updateRequest(connection, rejectedRequest, "REJECTED", "AVAILABLE");
        check("AVAILABLE".equals(foodStatus(connection, reject)), "rejected food status");
        pass("Request approval and rejection");

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE food SET type='ANIMAL', status='AVAILABLE' "
                        + "WHERE id=? AND status='ANIMAL_REVIEW'")) {
            statement.setInt(1, expired);
            check(statement.executeUpdate() == 1, "safe-for-animal status update");
        }
        check("ANIMAL".equals(foodType(connection, expired)), "animal type after review");
        pass("Safe for animals");

        try (Connection transaction = DB.get()) {
            transaction.setAutoCommit(false);
            try (PreparedStatement insert = transaction.prepareStatement(
                    "INSERT INTO requests (food_id, receiver_name, organization, phone, status) "
                            + "VALUES (?, 'SHELTER', 'Smoke Shelter', '', 'COMPLETED')");
                 PreparedStatement update = transaction.prepareStatement(
                         "UPDATE food SET status='COMPLETED' WHERE id=?")) {
                update.setInt(1, animal);
                check(update.executeUpdate() == 1, "shelter food status");
                insert.setInt(1, animal);
                insert.executeUpdate();
                transaction.commit();
            } catch (Exception ex) {
                transaction.rollback();
                throw ex;
            }
        }
        check("COMPLETED".equals(foodStatus(connection, animal)), "shelter assignment");
        pass("Shelter assignment");

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE food SET status='DISCARDED' WHERE id=?")) {
            statement.setInt(1, discard);
            statement.executeUpdate();
        }
        check("DISCARDED".equals(foodStatus(connection, discard)), "discarded status");
        pass("Discard");

        double[] dashboard = DashboardFrame.queryStats(connection);
        check(dashboard.length == 6 && closeTo(dashboard[0] - before[0], 4)
                        && closeTo(dashboard[1] - before[1], 2)
                        && closeTo(dashboard[2] - before[2], 2),
                "dashboard total and category quantities");
        check((int) dashboard[4] - (int) before[4] == 2,
                "dashboard completed count");
        check(historyContains(connection, approve, "Community NGO"), "history receiver join");
        check(HistoryFrame.csv("Rice, \"fresh\"").equals("\"Rice, \"\"fresh\"\"\""),
                "CSV escaping");
        Path csv = Files.createTempFile("ecobite-smoke-", ".csv");
        try {
            Files.writeString(csv, HistoryFrame.csv("Food, \"saved\""), StandardCharsets.UTF_8);
            check(Files.readString(csv, StandardCharsets.UTF_8)
                    .equals("\"Food, \"\"saved\"\"\""), "CSV file output");
        } finally {
            Files.deleteIfExists(csv);
        }
        pass("Dashboard, history, and CSV");
    }

    /** Inserts a uniquely named test food row with an offset expiry time. */
    private static int insertFood(Connection connection, String suffix, String type, int offsetMinutes)
            throws Exception {
        String sql = "INSERT INTO food (food_name, quantity, type, prepared_time, expiry_time, status) "
                + "VALUES (?, 2, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 'AVAILABLE')";
        try (PreparedStatement statement = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, PREFIX + "-" + suffix);
            statement.setString(2, type);
            statement.setInt(3, offsetMinutes);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Food insert returned no id.");
                }
                return keys.getInt(1);
            }
        }
    }

    /** Inserts one pending smoke-test request. */
    private static int insertRequest(Connection connection, int foodId, String organization)
            throws Exception {
        String sql = "INSERT INTO requests "
                + "(food_id, receiver_name, organization, phone, status) VALUES (?, 'Smoke User', ?, "
                + "'1234567890', 'PENDING')";
        try (PreparedStatement statement = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, foodId);
            statement.setString(2, organization);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Request insert returned no id.");
                }
                return keys.getInt(1);
            }
        }
    }

    /** Updates request and food statuses in one smoke-test transaction. */
    private static void updateRequest(Connection connection, int requestId,
                                      String requestStatus, String foodStatus) throws Exception {
        try (Connection transaction = DB.get()) {
            transaction.setAutoCommit(false);
            try {
                int foodId;
                try (PreparedStatement select = transaction.prepareStatement(
                        "SELECT food_id FROM requests WHERE id=?")) {
                    select.setInt(1, requestId);
                    try (ResultSet result = select.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalStateException("Request not found.");
                        }
                        foodId = result.getInt(1);
                    }
                }
                try (PreparedStatement updateRequest = transaction.prepareStatement(
                        "UPDATE requests SET status=? WHERE id=?");
                     PreparedStatement updateFood = transaction.prepareStatement(
                             "UPDATE food SET status=? WHERE id=?")) {
                    updateRequest.setString(1, requestStatus);
                    updateRequest.setInt(2, requestId);
                    updateRequest.executeUpdate();
                    updateFood.setString(1, foodStatus);
                    updateFood.setInt(2, foodId);
                    updateFood.executeUpdate();
                }
                transaction.commit();
            } catch (Exception ex) {
                transaction.rollback();
                throw ex;
            }
        }
    }

    /** Returns the status for a food id. */
    private static String foodStatus(Connection connection, int foodId) throws Exception {
        return foodValue(connection, foodId, "status");
    }

    /** Returns the food type for a food id. */
    private static String foodType(Connection connection, int foodId) throws Exception {
        return foodValue(connection, foodId, "type");
    }

    /** Reads one fixed food column by its id. */
    private static String foodValue(Connection connection, int foodId, String column) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + column + " FROM food WHERE id=?")) {
            statement.setInt(1, foodId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        }
    }

    /** Confirms the history join contains a completed receiver. */
    private static boolean historyContains(Connection connection, int foodId, String organization)
            throws Exception {
        String sql = "SELECT r.organization FROM food f LEFT JOIN requests r "
                + "ON r.food_id=f.id AND r.status='COMPLETED' "
                + "WHERE f.id=? AND f.status='COMPLETED'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, foodId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && organization.equals(result.getString(1));
            }
        }
    }

    /** Deletes only requests and foods created by this smoke-test run. */
    private static void cleanRows() throws Exception {
        try (Connection connection = DB.get();
             PreparedStatement requests = connection.prepareStatement(
                     "DELETE r FROM requests r JOIN food f ON f.id=r.food_id WHERE f.food_name LIKE ?");
             PreparedStatement food = connection.prepareStatement(
                     "DELETE FROM food WHERE food_name LIKE ?")) {
            requests.setString(1, PREFIX + "-%");
            requests.executeUpdate();
            food.setString(1, PREFIX + "-%");
            food.executeUpdate();
            pass("Smoke rows cleaned");
        }
    }

    /** Fails fast with a useful message when a smoke-test expectation is false. */
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("FAIL: " + message);
        }
    }

    /** Compares dashboard quantities with a small tolerance for decimal storage. */
    private static boolean closeTo(double actual, double expected) {
        return Math.abs(actual - expected) < 0.01;
    }

    /** Prints one successful smoke-test section. */
    private static void pass(String message) {
        System.out.println("PASS: " + message);
    }
}
