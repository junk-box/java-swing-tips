package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    private static int openFrameCount;
    private static final TexturePaint TEXTURE = makeTexturePaint();
    private final JDesktopPane desktop = new JDesktopPane();

    public MainPanel() {
        super(new BorderLayout());

        JPanel p1 = new JPanel();
        p1.setOpaque(false);

        JPanel p2 = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                //super.paintComponent(g);
                g.setColor(new Color(100, 50, 50, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel p3 = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(TEXTURE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        desktop.add(createFrame(p1));
        desktop.add(createFrame(p2));
        desktop.add(createFrame(p3));

        URL url = getClass().getResource("tokeidai.jpg");
        BufferedImage bi = getFilteredImage(url);
        desktop.setBorder(new CentredBackgroundBorder(bi));
        // Bug ID: 6655001 D3D/OGL: Window translucency doesn't work with accelerated pipelines
        // http://bugs.java.com/view_bug.do?bug_id=6655001
        //desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        add(desktop);
        add(createMenuBar(), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Frame");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);
        JMenuItem menuItem = new JMenuItem("New Frame");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
        menuItem.setActionCommand("new");
        menuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                createFrame();
            }
        });
        menu.add(menuItem);
        return menuBar;
    }

    private JInternalFrame createFrame() {
        return createFrame(null);
    }

    private static JInternalFrame createFrame(JComponent c) {
        String title = String.format("Frame #%s", ++openFrameCount);
        JInternalFrame frame = new JInternalFrame(title, true, true, true, true);
        if (c instanceof JPanel) {
            JPanel p = (JPanel) c;
            p.add(new JLabel("label"));
            p.add(new JButton("button"));
            frame.setContentPane(p);
        }
        frame.setSize(160, 100);
        frame.setLocation(30 * openFrameCount, 30 * openFrameCount);
        frame.setOpaque(false);
        frame.setVisible(true);
        //desktop.getDesktopManager().activateFrame(frame);
        return frame;
    }

    private static TexturePaint makeTexturePaint() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(new Color(100, 120, 100, 100));
        g2.fillRect(0, 0, 16, 16);
        int cs = 4;
        for (int i = 0; i * cs < 16; i++) {
            for (int j = 0; j * cs < 16; j++) {
                if ((i + j) % 2 == 0) {
                    g2.fillRect(i * cs, j * cs, cs, cs);
                }
            }
        }
        g2.dispose();
        return new TexturePaint(img, new Rectangle(16, 16));
    }

    private static BufferedImage getFilteredImage(URL url) {
        BufferedImage image;
        try {
            image = ImageIO.read(url);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
        return image;
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

// https://community.oracle.com/thread/1395763 How can I use TextArea with Background Picture ?
// http://ateraimemo.com/Swing/CentredBackgroundBorder.html
class CentredBackgroundBorder implements Border {
    private final Insets insets = new Insets(0, 0, 0, 0);
    private final BufferedImage image;
    protected CentredBackgroundBorder(BufferedImage image) {
        this.image = image;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        int cx = x + (width - image.getWidth()) / 2;
        int cy = y + (height - image.getHeight()) / 2;
        g2.drawRenderedImage(image, AffineTransform.getTranslateInstance(cx, cy));
        g2.dispose();
    }
    @Override public Insets getBorderInsets(Component c) {
        return insets;
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
}
