import javax.swing.Timer;
import java.sql.Connection;
import java.sql.Statement;

/** Checks for expired human food on one shared Swing timer. */
final class ExpiryService {

    private static Timer timer;

    /** Prevents creating instances of this utility class. */
    private ExpiryService() {
    }

    /** Starts the app-wide expiry check once, on the Swing event thread. */
    static void start() {
        if (timer == null) {
            timer = new Timer(5000, event -> checkExpiredFood());
            timer.start();
        }
    }

    /** Moves expired available human food to review and reports database errors. */
    static void checkExpiredFood() {
        String sql = "UPDATE food SET status='ANIMAL_REVIEW' "
                + "WHERE type='HUMAN' AND status='AVAILABLE' AND expiry_time < NOW()";
        try (Connection connection = DB.get();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (Exception ex) {
            System.err.println("EcoBite expiry check failed: " + ex.getMessage());
        }
    }
}
