package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.net.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String PATH = "/toolbarButtonGraphics/general/";
    private final JToolBar toolbar1 = new JToolBar("ToolBarButton");
    private final JToolBar toolbar2 = new JToolBar("JButton");
    public MainPanel() {
        super(new BorderLayout());
        //toolbar1.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        //toolbar2.putClientProperty("JToolBar.isRollover", Boolean.FALSE);

        URL url1 = getClass().getResource(PATH + "Copy24.gif");
        URL url2 = getClass().getResource(PATH + "Cut24.gif");
        URL url3 = getClass().getResource(PATH + "Help24.gif");

        toolbar1.add(new JButton(new ImageIcon(url1)));
        toolbar1.add(new JButton(new ImageIcon(url2)));
        toolbar1.add(Box.createGlue());
        toolbar1.add(new JButton(new ImageIcon(url3)));

        toolbar2.add(createToolBarButton(url1));
        toolbar2.add(createToolBarButton(url2));
        toolbar2.add(Box.createGlue());
        toolbar2.add(createToolBarButton(url3));

        add(toolbar1, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
        add(toolbar2, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JButton createToolBarButton(URL url) {
        JButton b = new JButton(new ImageIcon(url));
        b.setRequestFocusEnabled(false);
        return b;
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

// class ToolBarButton extends JButton {
//     public ToolBarButton(ImageIcon icon) {
//         super(icon);
//         setContentAreaFilled(false);
//         setFocusPainted(false);
//         addMouseListener(new MouseAdapter() {
//             @Override public void mouseEntered(MouseEvent e) {
//                 setContentAreaFilled(true);
//             }
//             @Override public void mouseExited(MouseEvent e) {
//                 setContentAreaFilled(false);
//             }
//         });
//     }
// }
