package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super();
        setPreferredSize(new Dimension(320, 240));
    }
    private static TrayIcon makeTrayIcon(JFrame frame) {
//         final SystemTray tray = SystemTray.getSystemTray();
//         final Dimension d = tray.getTrayIconSize();
//         BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
//         ImageIcon i = new ImageIcon(getClass().getResource("16x16.png"));
//         Graphics2D g = image.createGraphics();
//         g.setBackground(new Color(0x0, true));
//         g.clearRect(0, 0, d.width, d.height);
//         i.paintIcon(null, g, (d.width - i.getIconWidth()) / 2, (d.height - i.getIconWidth()) / 2);
//         g.dispose();

        MenuItem item1 = new MenuItem("OPEN");
        item1.addActionListener(e -> {
            frame.setExtendedState(Frame.NORMAL);
            frame.setVisible(true);
        });

        MenuItem item2 = new MenuItem("EXIT");
        item2.addActionListener(e -> {
            SystemTray tray = SystemTray.getSystemTray();
            for (TrayIcon icon: tray.getTrayIcons()) {
                tray.remove(icon);
            }
            frame.dispose();
            //System.exit(0);
        });

        PopupMenu popup = new PopupMenu();
        popup.add(item1);
        popup.add(item2);

        Image image = new ImageIcon(MainPanel.class.getResource("16x16.png")).getImage();
        return new TrayIcon(image, "TRAY", popup);
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
            frame.addWindowStateListener(e -> {
                if (e.getNewState() == Frame.ICONIFIED) {
                    e.getWindow().dispose();
                }
            });
            try {
                SystemTray.getSystemTray().add(makeTrayIcon(frame));
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        } else {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
