package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String HTML_TEXT = "<html>Html <font color='red'>label</font><br/> Test";

    public MainPanel() {
        super(new BorderLayout());

        JLabel label0 = new JLabel("Default JLabel");
        JLabel label1 = new JLabel(HTML_TEXT);
        JLabel label2 = new JLabel(HTML_TEXT) {
            // JLabel with html tag can not be disabled or setForegroud?!
            // https://community.oracle.com/thread/1377943
            @Override public void setEnabled(boolean b) {
                super.setEnabled(b);
                setForeground(b ? UIManager.getColor("Label.foreground")
                                : UIManager.getColor("Label.disabledForeground"));
            }
        };
        JLabel label3 = new DisabledHtmlLabel(HTML_TEXT);

        JEditorPane editor1 = new JEditorPane("text/html", HTML_TEXT);
        editor1.setOpaque(false);
        editor1.setEditable(false);

        JEditorPane editor2 = new JEditorPane("text/html", HTML_TEXT);
        editor2.setOpaque(false);
        editor2.setEditable(false);
        editor2.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editor2.setFont(UIManager.getFont("Label.font"));

        JPanel box = new JPanel(new GridLayout(2, 3));
        box.add(initTitledBorder("JLabel",        label0));
        box.add(initTitledBorder("JLabel+Html",   label1));
        box.add(initTitledBorder("JLabel+Html+",  label2));
        box.add(initTitledBorder("JLabel+Html++", label3));
        box.add(initTitledBorder("JEditorPane",   editor1));
        box.add(initTitledBorder("JEditorPane+",  editor2));

        JCheckBox check = new JCheckBox("setEnabled", true);
        check.addActionListener(e -> {
            boolean f = ((JCheckBox) e.getSource()).isSelected();
            for (Component c: box.getComponents()) {
                c.setEnabled(f);
            }
        });

        add(check, BorderLayout.NORTH);
        add(box);
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent initTitledBorder(String title, JComponent c) {
        c.setBorder(BorderFactory.createTitledBorder(title));
        return c;
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

class DisabledHtmlLabel extends JLabel {
    private static final ColorConvertOp COLOR_CONVERT = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    private transient BufferedImage shadow;

    protected DisabledHtmlLabel(String text) {
        super(text);
    }
    @Override public void setEnabled(boolean b) {
        setForeground(b ? UIManager.getColor("Label.foreground")
                        : UIManager.getColor("Label.disabledForeground"));
        if (!b) {
            BufferedImage source = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = source.createGraphics();
            g2.setPaint(new Color(0x0, true));
            g2.fillRect(0, 0, getWidth(), getHeight());
            //print(g2);
            paint(g2);
            g2.dispose();
            shadow = COLOR_CONVERT.filter(source, null);
        }
        super.setEnabled(b);
    }
    @Override protected void paintComponent(Graphics g) {
        if (!isEnabled() && Objects.nonNull(shadow)) {
            g.drawImage(shadow, 0, 0, this);
        } else {
            super.paintComponent(g);
        }
    }
}
