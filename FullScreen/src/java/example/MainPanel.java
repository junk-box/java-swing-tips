package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String KEY = "full-screen";
    public MainPanel() {
        super(new BorderLayout());
        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleFullScreenWindow();
                }
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), KEY);
        getActionMap().put(KEY, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                toggleFullScreenWindow();
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getActionMap().put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println("ESC KeyEvent:");
//                 int mode = 2;
//                 if (mode == 0) {
//                     //dialog.dispose();
//                     //triggered windowClosed
//                 } else if (mode == 1) {
//                     ////When DISPOSE_ON_CLOSE met WebStart > www.pushing-pixels.org/?p=232
//                     ////Webstart thread is a non-daemon thread so the JVM cannot exit.
//                     ////JVM shutdown
//                     //System.exit(0);
//                 } else {

                ////click on the X
                Component c = SwingUtilities.getRoot(getRootPane());
                if (c instanceof JDialog) {
                    JDialog d = (JDialog) c;
                    d.dispatchEvent(new WindowEvent(d, WindowEvent.WINDOW_CLOSING));
                }
                //triggered windowClosing
            }
        });
        add(new JLabel("<html>F11 or Double Click: toggle full-screen<br>ESC: exit"), BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private void toggleFullScreenWindow() {
        Component c = SwingUtilities.getRoot(getRootPane());
        if (c instanceof JDialog) {
            JDialog dialog = (JDialog) c;
            GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
            if (Objects.isNull(graphicsDevice.getFullScreenWindow())) {
                dialog.dispose(); //destroy the native resources
                dialog.setUndecorated(true);
                dialog.setVisible(true); //rebuilding the native resources
                graphicsDevice.setFullScreenWindow(dialog);
            } else {
                graphicsDevice.setFullScreenWindow(null);
                dialog.dispose();
                dialog.setUndecorated(false);
                dialog.setVisible(true);
                dialog.repaint();
            }
        }
        requestFocusInWindow(); //for Ubuntu
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
        JDialog dialog = new JDialog();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                System.out.println("windowClosing:");
                System.out.println("  triggered only when you click on the X");
                System.out.println("  or on the close menu item in the window's system menu.'");
                System.out.println("System.exit(0);");
                System.exit(0); //WebStart
            }
            @Override public void windowClosed(WindowEvent e) {
                System.out.println("windowClosed & rebuild:");
            }
        });
        dialog.getContentPane().add(new MainPanel());
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
