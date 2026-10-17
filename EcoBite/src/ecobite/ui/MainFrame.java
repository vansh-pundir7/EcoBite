package ecobite.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        UiStyle.prepare();
        setTitle(UITheme.APP_NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 700);
        setMinimumSize(new Dimension(820, 580));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTextArea infoArea = new JTextArea(
                "EcoBite System Ready\n\n" +
                        "Application Started\n" +
                        "Ready for food rescue management."
        );
        infoArea.setEditable(false);
        infoArea.setFont(UITheme.BODY_FONT);
        infoArea.setForeground(UITheme.TEXT);
        infoArea.setBackground(UITheme.SURFACE);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(infoArea);

        add(UiStyle.createHeader("APPLICATION OVERVIEW", "Rescue food. Reduce waste. Feed communities."),
                BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        UiStyle.styleFrame(this);
    }
}
