package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.lang.reflect.*;
import java.security.*;
// import javax.jnlp.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        JScrollPane s1 = new JScrollPane(new JTable(model));
        JScrollPane s2 = new JScrollPane(new JTree());
        s1.setMinimumSize(new Dimension(0, 100));
        s2.setMinimumSize(new Dimension(0, 100));

        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, s1, s2);
        splitPane.setOneTouchExpandable(true);
        //splitPane.setDividerLocation(0);

//         BasicService bs;
//         try {
//             bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
//         } catch (UnavailableServiceException ex) {
//             bs = null;
//         }
        s1.setMinimumSize(new Dimension(0, 100));
        s2.setMinimumSize(new Dimension(0, 100));
//         if (bs == null) {
        if (splitPane.getUI() instanceof BasicSplitPaneUI) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override public Void run() {
                    try {
                        splitPane.setDividerLocation(0);
                        Method setKeepHidden = BasicSplitPaneUI.class.getDeclaredMethod("setKeepHidden", new Class<?>[] {Boolean.TYPE});
                        setKeepHidden.setAccessible(true);
                        setKeepHidden.invoke(splitPane.getUI(), new Object[] {Boolean.TRUE});
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            });
        }
//         } else {
// //             s1.setMinimumSize(new Dimension());
// //             s2.setMinimumSize(new Dimension());
// //             EventQueue.invokeLater(new Runnable() {
// //                 @Override public void run() {
// //                     splitPane.setDividerLocation(1d);
// //                     splitPane.setResizeWeight(1d);
// //                 }
// //             });
//             EventQueue.invokeLater(new Runnable() {
//                 @Override public void run() {
//                     Container divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
//                     for (Component c: divider.getComponents()) {
//                         if (c instanceof JButton) {
//                             ((JButton) c).doClick();
//                             break;
//                         }
//                     }
//                 }
//             });
//         }
        add(splitPane);
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
