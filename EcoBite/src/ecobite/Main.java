package ecobite;

import ecobite.service.TimerService;
import ecobite.ui.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    private static TimerService timerService;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            timerService = new TimerService();
            timerService.start();

            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
