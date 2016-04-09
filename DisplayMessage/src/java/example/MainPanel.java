package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JComboBox<TrayIcon.MessageType> messageType = new JComboBox<>(TrayIcon.MessageType.values()); //ERROR, WARNING, INFO, NONE
    private final JButton messageButton = new JButton("TrayIcon#displayMessage()");

    public MainPanel() {
        super(new BorderLayout());
        initSystemTray();

        messageButton.addActionListener(e -> {
            TrayIcon[] icons = SystemTray.getSystemTray().getTrayIcons();
            if (icons.length > 0) {
                icons[0].displayMessage("caption", "text text text text", (TrayIcon.MessageType) messageType.getSelectedItem());
            }
        });
        JPanel p = new JPanel();
        p.add(messageType);
        p.add(messageButton);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
        setPreferredSize(new Dimension(320, 240));
    }
    private void initSystemTray() {
        MenuItem openItem = new MenuItem("OPEN");
        openItem.addActionListener(e -> {
            Container c = getTopLevelAncestor();
            if (c instanceof Window) {
                ((Window) c).setVisible(true);
            }
        });

        MenuItem exitItem = new MenuItem("EXIT");
        exitItem.addActionListener(e -> {
            Container c = getTopLevelAncestor();
            if (c instanceof Window) {
                ((Window) c).dispose();
            }
            SystemTray tray = SystemTray.getSystemTray();
            for (TrayIcon icon: tray.getTrayIcons()) {
                tray.remove(icon);
            }
        });

        PopupMenu popup = new PopupMenu();
        popup.add(openItem);
        popup.add(exitItem);

        Image image = new ImageIcon(getClass().getResource("16x16.png")).getImage();
        try {
            SystemTray.getSystemTray().add(new TrayIcon(image, "TRAY", popup));
            //icon.addActionListener(e -> log.append(e.toString() + "\n"));
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        if (SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override public void windowIconified(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });
        } else {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        frame.getContentPane().add(new MainPanel());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
