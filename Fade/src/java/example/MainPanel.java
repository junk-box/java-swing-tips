package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private boolean mode = true;
    private final Timer animator;
    private transient BufferedImage icon;

    public MainPanel() {
        super(new BorderLayout());
        URL url = getClass().getResource("test.png");
        try {
            icon = ImageIO.read(url);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        FadeImage fade = new FadeImage();
        animator = new Timer(25, fade);

        JButton button1 = new JButton(new AbstractAction("Open") {
            @Override public void actionPerformed(ActionEvent e) {
                mode = true;
                animator.start();
            }
        });
        JButton button2 = new JButton(new AbstractAction("Close") {
            @Override public void actionPerformed(ActionEvent e) {
                mode = false;
                animator.start();
            }
        });
        add(fade);
        add(button1, BorderLayout.SOUTH);
        add(button2, BorderLayout.NORTH);
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }

    class FadeImage extends JComponent implements ActionListener {
        private int alpha = 10;
        protected FadeImage() {
            super();
            setBackground(Color.BLACK);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (mode && alpha < 10) {
                alpha += 1;
            } else if (!mode && alpha > 0) {
                alpha -= 1;
            } else {
                animator.stop();
            }
            g2.setComposite(makeAlphaComposite(alpha * .1f));
            g2.drawImage(icon, null, 0, 0);
            g2.dispose();
        }
        @Override public void actionPerformed(ActionEvent e) {
            repaint();
        }
        private AlphaComposite makeAlphaComposite(float alpha) {
            return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
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
