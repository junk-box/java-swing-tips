package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super();
        JLabel label = new LabelWithToolBox(new ImageIcon(getClass().getResource("test.png")));
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 222, 222)),
            BorderFactory.createLineBorder(Color.WHITE, 4)));
        add(label);
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

class LabelWithToolBox extends JLabel {
    private static final int DELAY = 8;
    private final Timer animator = new Timer(DELAY, new ActionListener() {
        @Override public void actionPerformed(ActionEvent e) {
            int height = toolBox.getPreferredSize().height;
            double h = (double) height;
            if (isHidden) {
                double a = AnimationUtil.easeInOut(++counter / h);
                yy = (int) (.5 + a * h);
                toolBox.setBackground(new Color(0f, 0f, 0f, (float) (.6 * a)));
                if (yy >= height) {
                    yy = height;
                    animator.stop();
                }
            } else {
                double a = AnimationUtil.easeInOut(--counter / h);
                yy = (int) (.5 + a * h);
                toolBox.setBackground(new Color(0f, 0f, 0f, (float) (.6 * a)));
                if (yy <= 0) {
                    yy = 0;
                    animator.stop();
                }
            }
            toolBox.revalidate();
        }
    });
    private transient ToolBoxHandler handler;
    private boolean isHidden;
    private int counter;
    private int yy;
    private final JToolBar toolBox = new JToolBar() {
        private transient MouseAdapter listener;
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public void updateUI() {
            removeMouseListener(listener);
            super.updateUI();
            listener = new ParentDispatchMouseListener();
            addMouseListener(listener);
            setFloatable(false);
            setOpaque(false);
            setBackground(new Color(0x0, true));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
        }
    };
    protected LabelWithToolBox(Icon image) {
        super(image);

        //toolBox.setLayout(new BoxLayout(toolBox, BoxLayout.X_AXIS));
        toolBox.add(Box.createGlue());
        //http://chrfb.deviantart.com/art/quot-ecqlipse-2-quot-PNG-59941546
        toolBox.add(makeToolButton("ATTACHMENT_16x16-32.png"));
        toolBox.add(Box.createHorizontalStrut(2));
        toolBox.add(makeToolButton("RECYCLE BIN - EMPTY_16x16-32.png"));
        add(toolBox);
    }
    @Override public void updateUI() {
        removeMouseListener(handler);
        addHierarchyListener(handler);
        super.updateUI();
        setLayout(new OverlayLayout(this) {
            @Override public void layoutContainer(Container parent) {
                //Insets insets = parent.getInsets();
                int ncomponents = parent.getComponentCount();
                if (ncomponents == 0) {
                    return;
                }
                int width = parent.getWidth(); // - insets.left - insets.right;
                int height = parent.getHeight(); // - insets.left - insets.right;
                int x = 0; //insets.left; int y = insets.top;
                //for (int i = 0; i < ncomponents; i++) {
                Component c = parent.getComponent(0); //= toolBox;
                c.setBounds(x, height - yy, width, c.getPreferredSize().height);
                //}
            }
        });
        handler = new ToolBoxHandler();
        addMouseListener(handler);
        addHierarchyListener(handler);
    }
    private class ToolBoxHandler extends MouseAdapter implements HierarchyListener {
        @Override public void mouseEntered(MouseEvent e) {
            if (!animator.isRunning()) { // && yy != toolBox.getPreferredSize().height) {
                isHidden = true;
                animator.start();
            }
        }
        @Override public void mouseExited(MouseEvent e) {
            if (!contains(e.getPoint())) { //!animator.isRunning()) {
                isHidden = false;
                animator.start();
            }
        }
        @Override public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
                animator.stop();
            }
        }
    }
    private JButton makeToolButton(String name) {
        ImageIcon icon = new ImageIcon(getClass().getResource(name));
        JButton b = new JButton();
        b.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
//         b.addChangeListener(new ChangeListener() {
//             @Override public void stateChanged(ChangeEvent e) {
//                 JButton button = (JButton) e.getSource();
//                 ButtonModel model = button.getModel();
//                 if (button.isRolloverEnabled() && model.isRollover()) {
//                     button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
//                 } else {
//                     button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
//                 }
//             }
//         });
        b.setIcon(makeRolloverIcon(icon));
        b.setRolloverIcon(icon);
        b.setContentAreaFilled(false);
        //b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setToolTipText(name);
        return b;
    }
    private static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
        RescaleOp op = new RescaleOp(
            new float[] {.5f, .5f, .5f, 1f},
            new float[] {0f, 0f, 0f, 0f}, null);
        BufferedImage img = new BufferedImage(
            srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        srcIcon.paintIcon(null, g, 0, 0);
        g.dispose();
        return new ImageIcon(op.filter(img, null));
    }
}

class ParentDispatchMouseListener extends MouseAdapter {
    @Override public void mouseEntered(MouseEvent e) {
        dispatchMouseEvent(e);
    }
    @Override public void mouseExited(MouseEvent e) {
        dispatchMouseEvent(e);
    }
    private void dispatchMouseEvent(MouseEvent e) {
        Component src = e.getComponent();
        Optional.ofNullable(SwingUtilities.getUnwrappedParent(src)).ifPresent(tgt -> {
            tgt.dispatchEvent(SwingUtilities.convertMouseEvent(src, e, tgt));
        });
    }
}

final class AnimationUtil {
    private static final int N = 3;
    private AnimationUtil() { /* Singleton */ }
    //http://www.anima-entertainment.de/math-easein-easeout-easeinout-and-bezier-curves
    //Math: EaseIn EaseOut, EaseInOut and Bezier Curves | Anima Entertainment GmbH
    public static double easeIn(double t) {
        //range: 0.0 <= t <= 1.0
        return Math.pow(t, N);
    }
    public static double easeOut(double t) {
        return Math.pow(t - 1d, N) + 1d;
    }
    public static double easeInOut(double t) {
/*/
        if (t < .5) {
            return .5 * Math.pow(t * 2d, N);
        } else {
            return .5 * (Math.pow(t * 2d - 2d, N) + 2d);
        }
    }
/*/
        double ret;
        if (t < .5) {
            ret = .5 * intpow(t * 2d, N);
        } else {
            ret = .5 * (intpow(t * 2d - 2d, N) + 2d);
        }
        return ret;
    }
    //http://d.hatena.ne.jp/pcl/20120617/p1
    //http://d.hatena.ne.jp/rexpit/20110328/1301305266
    //http://c2.com/cgi/wiki?IntegerPowerAlgorithm
    //http://www.osix.net/modules/article/?id=696
    public static double intpow(double da, int ib) {
        int b = ib;
        if (b < 0) {
            //return d / intpow(a, -b);
            throw new IllegalArgumentException("B must be a positive integer or zero");
        }
        double a = da;
        double d = 1d;
        for (; b > 0; a *= a, b >>>= 1) {
            if ((b & 1) != 0) {
                d *= a;
            }
        }
        return d;
    }
//*/
//     public static double delta(double t) {
//         return 1d - Math.sin(Math.acos(t));
//     }
}
