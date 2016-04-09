package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;
//import java.util.stream.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private static final String DRAWS_FOCUS_BORDER_AROUND_ICON = "Tree.drawsFocusBorderAroundIcon";
    private static final String DRAW_DASHED_FOCUS_INDICATOR    = "Tree.drawDashedFocusIndicator";
    private final JCheckBox dfbaiCheck = new ActionCommandCheckBox(DRAWS_FOCUS_BORDER_AROUND_ICON);
    private final JCheckBox ddfiCheck  = new ActionCommandCheckBox(DRAW_DASHED_FOCUS_INDICATOR);
    private final JTextArea textArea   = new JTextArea();
    private final JTree tree           = new JTree();
    public MainPanel() {
        super(new BorderLayout());

        log("MainPanel: init");
        //updateCheckBox("MainPanel: init");

        UIManager.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("lookAndFeel")) {
                //String lnf = e.getNewValue().toString();
                updateCheckBox("UIManager: propertyChange");
            }
        });

        EventQueue.invokeLater(() -> {
            ActionListener al = e -> {
                log("JMenuItem: actionPerformed");
                Object o = e.getSource();
                if (o instanceof JRadioButtonMenuItem && ((JRadioButtonMenuItem) o).isSelected()) {
                    updateCheckBox("JMenuItem: actionPerformed: invokeLater");
                }
            };
            List<JRadioButtonMenuItem> list = new ArrayList<>();
            JMenuBar menuBar = getRootPane().getJMenuBar();
            LookAndFeelUtil.searchAllMenuElements(menuBar, list);
            for (JRadioButtonMenuItem mi: list) {
                mi.addActionListener(al);
            }
        });

//         listMenuItems(menuBar)
//           .filter(mi -> mi instanceof JRadioButtonMenuItem)
//           .forEach(mi -> ((JRadioButtonMenuItem) mi).addActionListener(al));

        JPanel np = new JPanel(new GridLayout(2, 1));
        np.add(dfbaiCheck);
        np.add(ddfiCheck);

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(new JScrollPane(tree));
        p.add(new JScrollPane(textArea));

        add(np, BorderLayout.NORTH);
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }

    @Override public void updateUI() {
        super.updateUI();
        log("JPanel: updateUI");
        updateCheckBox("JPanel: updateUI: invokeLater");
    }

    private void updateCheckBox(final String str) {
        EventQueue.invokeLater(() -> {
            log("--------\n" + str);

            log(DRAWS_FOCUS_BORDER_AROUND_ICON + ": " + UIManager.getBoolean(DRAWS_FOCUS_BORDER_AROUND_ICON));
            dfbaiCheck.setSelected(UIManager.getBoolean(DRAWS_FOCUS_BORDER_AROUND_ICON));

            log(DRAW_DASHED_FOCUS_INDICATOR + ": " + UIManager.getBoolean(DRAW_DASHED_FOCUS_INDICATOR));
            ddfiCheck.setSelected(UIManager.getBoolean(DRAW_DASHED_FOCUS_INDICATOR));
        });
    }

    private void log(String str) {
        if (Objects.nonNull(textArea)) {
            textArea.append(str + "\n");
        } else {
            System.out.println(str);
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
        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(mb);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ActionCommandCheckBox extends JCheckBox {
    protected ActionCommandCheckBox(String cmd) {
        super(cmd);
        setActionCommand(cmd);
        setAction(new AbstractAction(cmd) {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox) e.getSource();
                UIManager.put(c.getActionCommand(), c.isSelected());
                SwingUtilities.updateComponentTreeUI(c.getRootPane());
            }
        });
    }
}

//http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lookAndFeelRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lookAndFeelRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, final ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try {
                    setLookAndFeel(m.getActionCommand());
                } catch (ClassNotFoundException | InstantiationException
                       | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }

    public static void searchAllMenuElements(MenuElement me, List<JRadioButtonMenuItem> list) {
        if (me instanceof JRadioButtonMenuItem) {
            list.add((JRadioButtonMenuItem) me);
        }
        MenuElement[] sub = me.getSubElements();
        if (sub.length != 0) {
            for (MenuElement e: sub) {
                searchAllMenuElements(e, list);
            }
        }
    }

//     static Stream<MenuElement> listMenuItems(MenuElement me) {
//         MenuElement[] sub = me.getSubElements();
//         if (sub.length != 0) {
//             return Arrays.stream(sub).flatMap(MainPanel::listMenuItems);
//         } else {
//             return Stream.of(me);
//         }
//     }
}
