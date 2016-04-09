package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static int count;
    private final MyJTabbedPane tab = new MyJTabbedPane();
    private final JPopupMenu pop = new JPopupMenu();

    public MainPanel() {
        super(new BorderLayout());
        pop.add(new NewTabAction("Add"));
        pop.addSeparator();
        pop.add(new CloseAllAction("Close All"));
        tab.setComponentPopupMenu(pop);
        tab.addTab("JLabel", new JLabel("JDK 6"));
        tab.addTab("JTree",  new JScrollPane(new JTree()));
        add(tab);
        setPreferredSize(new Dimension(320, 240));
    }
    class NewTabAction extends AbstractAction {
        protected NewTabAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            tab.addTab("Title" + count, new JLabel("Tab" + count));
            tab.setSelectedIndex(tab.getTabCount() - 1);
            count++;
        }
    }
    class CloseAllAction extends AbstractAction {
        protected CloseAllAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            tab.removeAll();
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class MyJTabbedPane extends JTabbedPane {
    private static final Icon CLOSE_ICON = new CloseTabIcon();
    @Override public void addTab(String title, final Component content) {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        JButton button = new JButton(CLOSE_ICON);
        //button.setBorderPainted(false);
        //button.setFocusPainted(false);
        //button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                removeTabAt(indexOfComponent(content));
            }
        });
        tab.add(label,  BorderLayout.WEST);
        tab.add(button, BorderLayout.EAST);
        tab.setBorder(BorderFactory.createEmptyBorder(2, 1, 1, 1));
        super.addTab(title, content);
        setTabComponentAt(getTabCount() - 1, tab);
    }
}

class CloseTabIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(Color.BLACK);
        g2.drawLine(4,  4, 11, 11);
        g2.drawLine(4,  5, 10, 11);
        g2.drawLine(5,  4, 11, 10);
        g2.drawLine(11, 4,  4, 11);
        g2.drawLine(11, 5,  5, 11);
        g2.drawLine(10, 4,  4, 10);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 16;
    }
    @Override public int getIconHeight() {
        return 16;
    }
}
