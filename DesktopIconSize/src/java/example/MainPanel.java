package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import javax.swing.*;
import javax.swing.plaf.synth.*;
import com.sun.java.swing.plaf.motif.*;

public final class MainPanel extends JPanel {
    private static final int DESKTOPICON_WIDTH = 150;
    private static final int DESKTOPICON_HEIGHT = 40;
    private final JDesktopPane desktop = new JDesktopPane();
    private final JCheckBox check = new JCheckBox(String.format("JDesktopIcon: %dx%d", DESKTOPICON_WIDTH, DESKTOPICON_HEIGHT));
    public MainPanel() {
        super(new BorderLayout());
        desktop.setDesktopManager(new DefaultDesktopManager() {
            @Override protected Rectangle getBoundsForIconOf(JInternalFrame f) {
                Rectangle r = super.getBoundsForIconOf(f);
                //TEST: r.width = 200;
                System.out.println(r.getSize());
                return r;
            }
        });

        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());
        mb.add(new JButton(new AbstractAction("add") {
            private int n;
            @Override public void actionPerformed(ActionEvent e) {
                JInternalFrame f = createFrame("#" + n, n * 10, n * 10);
                desktop.add(f);
                desktop.getDesktopManager().activateFrame(f);
                n++;
            }
        }));
        mb.add(Box.createHorizontalGlue());
        mb.add(check);

        addIconifiedFrame(createFrame("Frame", 30, 10));
        addIconifiedFrame(createFrame("Frame", 50, 30));
        add(desktop);
        add(mb, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private JInternalFrame createFrame(String t, int x, int y) {
        JInternalFrame f = new JInternalFrame(t, true, true, true, true);
        f.setDesktopIcon(new JInternalFrame.JDesktopIcon(f) {
            @Override public Dimension getPreferredSize() {
                if (!check.isSelected()) {
                    return super.getPreferredSize();
                }
                if (getUI() instanceof MotifDesktopIconUI) {
                    return new Dimension(64, 64 + 32);
                } else {
                    return new Dimension(DESKTOPICON_WIDTH, DESKTOPICON_HEIGHT);
                }
            }
        });
        f.setSize(200, 100);
        f.setLocation(x, y);
        f.setVisible(true);
        return f;
    }
    private void addIconifiedFrame(JInternalFrame f) {
        desktop.add(f);
        try {
            f.setIcon(true);
        } catch (PropertyVetoException ex) {
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
            UIManager.put("DesktopIcon.width", DESKTOPICON_WIDTH);
            //TEST:
            //Font font = UIManager.getFont("InternalFrame.titleFont");
            //UIManager.put("InternalFrame.titleFont", font.deriveFont(30f));
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lookAndFeelRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lookAndFeelRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, final ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try {
                    setLookAndFeel(m.getActionCommand());
                } catch (ClassNotFoundException | InstantiationException
                       | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
