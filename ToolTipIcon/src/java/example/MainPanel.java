package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
    private final URL url = getClass().getResource("wi0124-48.png");
    private final ImageIcon icon = new ImageIcon(url);
    public MainPanel() {
        super(new BorderLayout());
        JLabel l1 = new JLabel("ToolTip icon using JLabel") {
            @Override public JToolTip createToolTip() {
                final JLabel iconlabel = new JLabel(icon);
                iconlabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                //LookAndFeel.installColorsAndFont(iconlabel, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
                JToolTip tip = new JToolTip() {
                    @Override public Dimension getPreferredSize() {
//                     if (getLayout() == null) {
//                         Insets i = getInsets();
//                         Dimension d = iconlabel.getPreferredSize();
//                         d.width  += i.left + i.right;
//                         d.height += i.top + i.bottom;
//                         return d;
                        return getLayout().preferredLayoutSize(this);
                    }
                    @Override public void setTipText(String tipText) {
                        String oldValue = iconlabel.getText();
                        iconlabel.setText(tipText);
                        firePropertyChange("tiptext", oldValue, tipText);
                    }
                };
                tip.setComponent(this);
                tip.setLayout(new BorderLayout());
                tip.add(iconlabel);
                return tip;
            }
        };
        l1.setToolTipText("Test1");

        JLabel l2 = new JLabel("ToolTip icon using MatteBorder") {
            @Override public JToolTip createToolTip() {
                JToolTip tip = new JToolTip() {
                    @Override public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        Insets i = getInsets();
                        d.height = Math.max(d.height, icon.getIconHeight() + i.top + i.bottom);
                        return d;
                    }
                };
                tip.setComponent(this);
                Border b1 = tip.getBorder();
                Border b2 = BorderFactory.createMatteBorder(0, icon.getIconWidth(), 0, 0, icon);
                Border b3 = BorderFactory.createEmptyBorder(1, 1, 1, 1);
                Border b4 = BorderFactory.createCompoundBorder(b3, b2);
                tip.setBorder(BorderFactory.createCompoundBorder(b1, b4));
                return tip;
            }
        };
        l2.setToolTipText("Test2");

        JLabel l3 = new JLabel("ToolTip icon using HTML tags");
        l3.setToolTipText(String.format("<html><img src='%s'>Test3</img></html>", url)); //align='middle'

        Box box = Box.createVerticalBox();
        box.add(l1);
        box.add(Box.createVerticalStrut(20));
        box.add(l2);
        box.add(Box.createVerticalStrut(20));
        box.add(l3);
        box.add(Box.createVerticalGlue());

        add(box);
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
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
