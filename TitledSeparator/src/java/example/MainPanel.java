package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(new TitledSeparator("TitledBorder", 2, TitledBorder.DEFAULT_POSITION));
        box.add(new JCheckBox("JCheckBox 0"));
        box.add(new JCheckBox("JCheckBox 1"));
        box.add(Box.createVerticalStrut(10));

        box.add(new TitledSeparator("TitledBorder ABOVE TOP", new Color(100, 180, 200), 2, TitledBorder.ABOVE_TOP));
        box.add(new JCheckBox("JCheckBox 2"));
        box.add(new JCheckBox("JCheckBox 3"));
        box.add(Box.createVerticalStrut(10));

        box.add(new JSeparator());
        box.add(new JCheckBox("JCheckBox 4"));
        box.add(new JCheckBox("JCheckBox 5"));
        //box.add(Box.createVerticalStrut(8));

        add(box, BorderLayout.NORTH);
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

class TitledSeparator extends JLabel {
    private final String title;
    private final Color target;
    private final int height;
    private final int titlePosition;
    protected TitledSeparator(String title, int height, int titlePosition) {
        this(title, null, height, titlePosition);
    }
    protected TitledSeparator(String title, Color target, int height, int titlePosition) {
        super();
        this.title = title;
        this.target = target;
        this.height = height;
        this.titlePosition = titlePosition;
        updateBorder();
    }
    private void updateBorder() {
        Icon icon = new TitledSeparatorIcon();
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(height, 0, 0, 0, icon), title,
            TitledBorder.DEFAULT_JUSTIFICATION, titlePosition));
    }
    @Override public Dimension getMaximumSize() {
        Dimension d = super.getPreferredSize();
        d.width = Short.MAX_VALUE;
        return d;
    }
    @Override public void updateUI() {
        super.updateUI();
        updateBorder();
    }
    private class TitledSeparatorIcon implements Icon {
        private int width = -1;
        private Paint painter1;
        private Paint painter2;
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            int w = c.getWidth();
            Color color = getBackground();
            if (w != width || Objects.isNull(painter1) || Objects.isNull(painter2)) {
                width = w;
                Point2D start = new Point2D.Float();
                Point2D end   = new Point2D.Float(width, 0);
                float[] dist  = {0f, 1f};
                color = Objects.nonNull(color) ? color : UIManager.getColor("Panel.background");
                Color tc = Objects.nonNull(target) ? target : color;
                painter1 = new LinearGradientPaint(start, end, dist, new Color[] {tc.darker(),   color});
                painter2 = new LinearGradientPaint(start, end, dist, new Color[] {tc.brighter(), color});
            }
            int h = getIconHeight() / 2;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x, y);
            g2.setPaint(painter1);
            g2.fillRect(0, 0, width, getIconHeight());
            g2.setPaint(painter2);
            g2.fillRect(0, h, width, getIconHeight() - h);
            g2.dispose();
        }
        @Override public int getIconWidth() {
            return 200; //dummy width
        }
        @Override public int getIconHeight() {
            return height;
        }
    }
}
