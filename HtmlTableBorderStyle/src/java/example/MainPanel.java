package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public static final String TD1 = "<td style='background-color:white;border-right:1px solid green;border-top:1px solid blue'>aaaaaaaaaaaaaaa</td>";
    public static final String TABLE_STYLE1 = "style='border-left:1px solid red;border-bottom:1px solid red;background-color:yellow' cellspacing='0px' cellpadding='5px'";

    public static final String TD2 = "<td style='background-color:white;border-right:1px solid green;border-bottom:1px solid blue'>aaaaaaaaaaaaaaa</td>";
    public static final String TABLE_STYLE2 = "style='border-left:1px solid red;border-top:1px solid red;background-color:yellow' cellspacing='0px' cellpadding='5px'";

    //http://stackoverflow.com/questions/3355469/1-pixel-table-border-in-jtextpane-using-html
    public static final String TD3 = "<td style='background-color:white'>aaaaaaaaaaaaaaa</td>";
    public static final String TABLE_STYLE3 = "style='border:0px;background-color:red' cellspacing='1px' cellpadding='5px'";

    private MainPanel() {
        super();

        // not supported:
        // table {
        //   border-collapse: collapse;
        // }
        // th, td {
        //   border:1px solid #ccc;
        // }

        String html1 = "<html><table " + TABLE_STYLE1 + ">" + "<tr>" + TD1 + TD1 + "</tr><tr>" + TD1 + TD1 + "</tr></table>";
        String html2 = "<html><table " + TABLE_STYLE2 + ">" + "<tr>" + TD2 + TD2 + "</tr><tr>" + TD2 + TD2 + "</tr></table>";
        String html3 = "<html><table " + TABLE_STYLE3 + ">" + "<tr>" + TD3 + TD3 + "</tr><tr>" + TD3 + TD3 + "</tr></table>";

        add(makeTitledPanel(new JLabel(html1), "border-left, border-bottom"));
        add(makeTitledPanel(new JLabel(html2), "border-left, border-top"));
        add(makeTitledPanel(new JLabel(html3), "cellspacing"));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(JComponent c, String title) {
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
