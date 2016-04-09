package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

public final class MainPanel extends JPanel {
    private final URL url = getClass().getResource("anime.gif");
    private final JLabel l1 = new JLabel("Timer Animated ToolTip") {
        @Override public JToolTip createToolTip() {
            JToolTip tip = new AnimatedToolTip(new AnimatedLabel(""));
            tip.setComponent(this);
            return tip;
        }
    };
    private final JLabel l2 = new JLabel("Gif Animated ToolTip") {
        //private final Icon icon = new ImageIcon(url);
        @Override public JToolTip createToolTip() {
            JToolTip tip = new AnimatedToolTip(new JLabel("", new ImageIcon(url), SwingConstants.LEFT));
            tip.setComponent(this);
            return tip;
        }
    };
    private final JLabel l3 = new JLabel("Gif Animated ToolTip(html)");

    public MainPanel() {
        super(new BorderLayout());
        l1.setToolTipText("Test1");
        l2.setToolTipText("Test2");
        l3.setToolTipText(String.format("<html><img src='%s'>Test3</html>", url));

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder("javax.swing.Timer"));
        p1.add(l1);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Animated Gif"));
        p2.add(l2, BorderLayout.NORTH);
        p2.add(l3, BorderLayout.SOUTH);

        Box box = Box.createVerticalBox();
        box.add(p1);
        box.add(Box.createVerticalStrut(20));
        box.add(p2);
        box.add(Box.createVerticalGlue());
        add(box);
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class AnimatedToolTip extends JToolTip {
    private final JLabel iconlabel;
    protected AnimatedToolTip(JLabel label) {
        super();
        this.iconlabel = label;
        LookAndFeel.installColorsAndFont(iconlabel, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
        iconlabel.setOpaque(true);
        setLayout(new BorderLayout());
        add(iconlabel);
    }
    @Override public Dimension getPreferredSize() {
        return getLayout().preferredLayoutSize(this);
    }
//     @Override public Dimension getPreferredSize() {
//         Insets i = getInsets();
//         Dimension d = iconlabel.getPreferredSize();
//         d.width  += i.left + i.right;
//         d.height += i.top + i.bottom;
//         return d;
//     }
    @Override public void setTipText(String tipText) {
        String oldValue = iconlabel.getText();
        iconlabel.setText(tipText);
        firePropertyChange("tiptext", oldValue, tipText);
    }
    @Override public String getTipText() {
        return Objects.nonNull(iconlabel) ? iconlabel.getText() : "";
    }
}

class AnimatedLabel extends JLabel implements ActionListener {
    private final Timer animator;
    private final transient AnimeIcon icon = new AnimeIcon();
    protected AnimatedLabel(String title) {
        super(title);
        setOpaque(true);
        animator = new Timer(100, this);
        setIcon(icon);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (e.getComponent().isShowing()) {
                    startAnimation();
                } else {
                    stopAnimation();
                }
            }
        });
    }
    @Override public void actionPerformed(ActionEvent e) {
        icon.next();
        repaint();
    }
    private void startAnimation() {
        icon.setRunning(true);
        animator.start();
    }
    private void stopAnimation() {
        icon.setRunning(false);
        animator.stop();
    }
}

class AnimeIcon implements Icon {
    private static final Color ELLIPSE_COLOR = new Color(.5f, .5f, .5f);
    private static final double R  = 2d;
    private static final double SX = 1d;
    private static final double SY = 1d;
    private static final int WIDTH  = (int) (R * 8 + SX * 2);
    private static final int HEIGHT = (int) (R * 8 + SY * 2);
    private final List<Shape> list = new ArrayList<Shape>(Arrays.asList(
        new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));

    private boolean isRunning;
    public void next() {
        if (isRunning) {
            list.add(list.remove(0));
        }
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(Objects.nonNull(c) ? c.getBackground() : Color.WHITE);
        g2.fillRect(x, y, getIconWidth(), getIconHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(ELLIPSE_COLOR);
        g2.translate(x, y);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            float alpha = isRunning ? (i + 1) / (float) size : .5f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.fill(list.get(i));
        }
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return WIDTH;
    }
    @Override public int getIconHeight() {
        return HEIGHT;
    }
}
