import java.sql.Connection;
import java.sql.DriverManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Loads local MySQL settings without keeping credentials in source code. */
public class DB {

    /** Opens a connection using db.properties beside the run scripts. */
    public static Connection get() throws Exception {
        Properties settings = new Properties();
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream("db.properties"), StandardCharsets.UTF_8)) {
            settings.load(reader);
        } catch (IOException ex) {
            throw new IOException("Create db.properties from db.properties.example and set "
                    + "your MySQL connection values.", ex);
        }

        String url = settings.getProperty("jdbc.url", "").trim();
        String user = settings.getProperty("jdbc.user", "").trim();
        String password = settings.getProperty("jdbc.password", "");
        if (url.isEmpty() || user.isEmpty()) {
            throw new IOException("Set jdbc.url and jdbc.user in db.properties.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
