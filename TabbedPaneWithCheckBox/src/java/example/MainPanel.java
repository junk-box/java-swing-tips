package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    private final JTabbedPane tab  = new JTabbedPane();
    private final JCheckBox cbox   = new JCheckBox("Details");
    private final JComponent panel = new JLabel("Preferences");

    public MainPanel() {
        super(new BorderLayout());
        cbox.setFocusPainted(false);
        cbox.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JCheckBox cb = (JCheckBox) e.getComponent();
                cb.setSelected(!cb.isSelected());
            }
        });
        cbox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                tab.addTab("Preferences", panel);
                tab.setSelectedComponent(panel);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                tab.remove(panel);
            }
        });

        TabbedPaneWithCompBorder b = new TabbedPaneWithCompBorder(cbox, tab);
        tab.addMouseListener(b);
        tab.setBorder(b);
        tab.addTab("Quick Preferences", new JLabel("aaaaaaaaa"));
        add(tab);
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
        frame.setMinimumSize(new Dimension(256, 80));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TabbedPaneWithCompBorder implements Border, MouseListener, SwingConstants {
    private final JCheckBox cbox;
    private final JTabbedPane tab;
    private final JComponent rubberStamp = new JPanel();
    private final Rectangle rect = new Rectangle();

    protected TabbedPaneWithCompBorder(JCheckBox cbox, JTabbedPane tab) {
        this.cbox = cbox;
        this.tab  = tab;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Dimension size = cbox.getPreferredSize();
        int xx = tab.getSize().width - size.width;
        Rectangle lastTab = tab.getUI().getTabBounds(tab, tab.getTabCount() - 1);
        int tabEnd = lastTab.x + lastTab.width;
        if (xx < tabEnd) {
            xx = tabEnd;
        }
        rect.setBounds(xx, -2, size.width, size.height);
        SwingUtilities.paintComponent(g, cbox, rubberStamp, rect);
    }
    @Override public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
    private void dispatchEvent(MouseEvent e) {
        if (!rect.contains(e.getX(), e.getY())) {
            return;
        }
        cbox.setBounds(rect);
        cbox.dispatchEvent(SwingUtilities.convertMouseEvent(tab, e, cbox));
    }
    @Override public void mouseClicked(MouseEvent e) {
        dispatchEvent(e);
    }
    @Override public void mouseEntered(MouseEvent e) {
        dispatchEvent(e);
    }
    @Override public void mouseExited(MouseEvent e) {
        dispatchEvent(e);
    }
    @Override public void mousePressed(MouseEvent e) {
        dispatchEvent(e);
    }
    @Override public void mouseReleased(MouseEvent e) {
        dispatchEvent(e);
    }
}
