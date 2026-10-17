import java.sql.Connection;
import java.sql.DriverManager;

// ============================================
// DATABASE CONNECTION
// Password sirf yahin change karna hai!
// ============================================
public class DB {

    private static final String URL  = "jdbc:mysql://localhost:3306/ecobite";
    private static final String USER = "root";
    private static final String PASS = "vnsh7422";   // <-- apna MySQL password yahan likho

    public static Connection get() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
