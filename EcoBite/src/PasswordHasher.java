import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HexFormat;

/** Hashes passwords with a random salt using the JDK SHA-256 implementation. */
final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Prevents creating instances of this utility class. */
    private PasswordHasher() {
    }

    /** Returns a salted SHA-256 password value suitable for the users table. */
    static String hash(String password) throws Exception {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] digest = digest(salt, password);
        HexFormat hex = HexFormat.of();
        return "sha256:" + hex.formatHex(salt) + ":" + hex.formatHex(digest);
    }

    /** Checks a salted password or supports a legacy plaintext row during upgrade. */
    static boolean verify(String password, String stored) throws Exception {
        if (stored == null) {
            return false;
        }
        if (!stored.startsWith("sha256:")) {
            return MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8),
                    stored.getBytes(StandardCharsets.UTF_8));
        }
        String[] parts = stored.split(":", -1);
        if (parts.length != 3) {
            return false;
        }
        try {
            HexFormat hex = HexFormat.of();
            byte[] salt = hex.parseHex(parts[1]);
            byte[] expected = hex.parseHex(parts[2]);
            byte[] textSalt = parts[1].getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(expected, digest(salt, password))
                    || MessageDigest.isEqual(expected, digest(password, salt))
                    || MessageDigest.isEqual(expected, digest(textSalt, password))
                    || MessageDigest.isEqual(expected, digest(password, textSalt));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /** Creates the default login if needed and upgrades its original plaintext password. */
    static void ensureDefaultAdmin() throws Exception {
        try (Connection connection = DB.get()) {
            int userId = -1;
            String storedPassword = null;
            try (PreparedStatement find = connection.prepareStatement(
                    "SELECT id, password FROM users WHERE username='admin'");
                 ResultSet results = find.executeQuery()) {
                if (results.next()) {
                    userId = results.getInt("id");
                    storedPassword = results.getString("password");
                }
            }
            if (userId >= 0) {
                if (storedPassword == null || storedPassword.isBlank()
                        || "admin123".equals(storedPassword)) {
                    updatePassword(connection, userId, hash("admin123"));
                }
            } else {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO users (username, password) VALUES ('admin', ?)")) {
                    insert.setString(1, hash("admin123"));
                    insert.executeUpdate();
                }
            }
        }
    }

    /** Upgrades a user's stored value after a successful legacy-password sign in. */
    static void updatePassword(Connection connection, int userId, String password) throws Exception {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE users SET password = ? WHERE id = ?")) {
            update.setString(1, password);
            update.setInt(2, userId);
            update.executeUpdate();
        }
    }

    /** Calculates the SHA-256 digest of the salt followed by the password. */
    private static byte[] digest(byte[] salt, String password) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(salt);
        return sha256.digest(password.getBytes(StandardCharsets.UTF_8));
    }

    /** Checks the alternative salt and password order used by the original app version. */
    private static byte[] digest(String password, byte[] salt) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(password.getBytes(StandardCharsets.UTF_8));
        return sha256.digest(salt);
    }

}
