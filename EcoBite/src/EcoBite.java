import javax.swing.SwingUtilities;

// ============================================
// ECOBITE - main file (yahin se program start hoga)
// ============================================
public class EcoBite {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExpiryService.start();
            try {
                PasswordHasher.ensureDefaultAdmin();
            } catch (Exception ex) {
                System.err.println("EcoBite startup database check failed: " + ex.getMessage());
            }
            new LoginFrame().setVisible(true);
        });
    }
}
