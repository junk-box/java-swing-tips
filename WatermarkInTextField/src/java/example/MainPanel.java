package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTextField field1 = new JTextField("Please enter your E-mail address");
        field1.addFocusListener(new GhostFocusListener(field1));
        JTextField field2 = new WatermarkTextField();

        Action a = new AbstractAction("xxx") {
            @Override public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
            }
        };
        field2.getActionMap().put("xxx", a);
        InputMap im = field2.getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), "xxx");

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(makePanel("E-mail", field1));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("Search", field2));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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

class WatermarkTextField extends JTextField implements FocusListener {
    private final ImageIcon image;
    private boolean showWatermark = true;
    protected WatermarkTextField() {
        super();
        image = new ImageIcon(getClass().getResource("watermark.png"));
        addFocusListener(this);
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showWatermark) {
            Graphics2D g2 = (Graphics2D) g.create();
            //Insets i = getMargin();
            Insets i = getInsets();
            int yy = (getHeight() - image.getIconHeight()) / 2;
            g2.drawImage(image.getImage(), i.left, yy, this);
            g2.dispose();
        }
    }
    @Override public void focusGained(FocusEvent e) {
        showWatermark = false;
        repaint();
    }
    @Override public void focusLost(FocusEvent e) {
        showWatermark = "".equals(getText().trim());
        repaint();
    }
}
class GhostFocusListener implements FocusListener {
    private static final Color INACTIVE_COLOR = UIManager.getColor("TextField.inactiveForeground");
    private static final Color ORIGINAL_COLOR = UIManager.getColor("TextField.foreground");
    private final String ghostMessage;
    protected GhostFocusListener(JTextComponent tf) {
        ghostMessage = tf.getText();
        tf.setForeground(INACTIVE_COLOR);
    }
    @Override public void focusGained(FocusEvent e) {
        JTextComponent textField = (JTextComponent) e.getComponent();
        String str = textField.getText();
        Color col  = textField.getForeground();
        if (ghostMessage.equals(str) && INACTIVE_COLOR.equals(col)) {
            textField.setForeground(ORIGINAL_COLOR);
            textField.setText("");
        }
    }
    @Override public void focusLost(FocusEvent e) {
        JTextComponent textField = (JTextComponent) e.getComponent();
        String str = textField.getText().trim();
        if ("".equals(str)) {
            textField.setForeground(INACTIVE_COLOR);
            textField.setText(ghostMessage);
        }
    }
}
