package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final Image icon;
    public MainPanel() {
        super(new BorderLayout());
        icon = Toolkit.getDefaultToolkit().createImage(getClass().getResource("16x16.png"));

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Window#setIconImage(Image)"));
        p.add(new JCheckBox(new AbstractAction("setIconImage") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox) e.getSource();
                Container w = c.getTopLevelAncestor();
                if (w instanceof Window) {
                    ((Window) w).setIconImage(c.isSelected() ? icon : null);
                }
            }
        }));
        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
