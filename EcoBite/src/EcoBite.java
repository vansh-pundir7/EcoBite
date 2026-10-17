import javax.swing.SwingUtilities;

// ============================================
// ECOBITE - main file (yahin se program start hoga)
// ============================================
public class EcoBite {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
