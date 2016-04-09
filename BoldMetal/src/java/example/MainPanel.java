package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private static final String TAG = "<html><b>";
    private MainPanel() {
        super(new BorderLayout());

        JCheckBox check = new JCheckBox("swing.boldMetal");
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                //http://docs.oracle.com/javase/jp/6/api/javax/swing/plaf/metal/DefaultMetalTheme.html
                JCheckBox c = (JCheckBox) e.getSource();
                UIManager.put("swing.boldMetal", c.isSelected());
                // re-install the Metal Look and Feel
                try {
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                } catch (UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                // Update the ComponentUIs for all Components. This
                // needs to be invoked for all windows.
                SwingUtilities.updateComponentTreeUI(c.getTopLevelAncestor());
            }
        });

        JTree tree = new JTree();
        tree.setComponentPopupMenu(new TreePopupMenu());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createTitledBorder("TitledBorder"));
        tabbedPane.addTab(TAG + "JTree", new JScrollPane(tree));
        tabbedPane.addTab("JLabel",      new JLabel("JLabel"));
        tabbedPane.addTab("JTextArea",   new JScrollPane(new JTextArea("JTextArea")));
        tabbedPane.addTab("JButton",     new JScrollPane(new JButton("JButton")));
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JTabbedPane t = (JTabbedPane) e.getSource();
                for (int i = 0; i < t.getTabCount(); i++) {
                    String title = t.getTitleAt(i);
                    if (i == t.getSelectedIndex()) {
                        t.setTitleAt(i, TAG + title);
                    } else if (title.startsWith(TAG)) {
                        t.setTitleAt(i, title.substring(TAG.length()));
                    }
                }
            }
        });
        add(check, BorderLayout.NORTH);
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        //XXX: UIManager.put("swing.boldMetal", Boolean.FALSE);
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TreePopupMenu extends JPopupMenu {
    private final JTextField textField = new JTextField(24);
    private TreePath path;
    private final Action addNodeAction = new AbstractAction("add") {
        @Override public void actionPerformed(ActionEvent e) {
            JTree tree = (JTree) getInvoker();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode child  = new DefaultMutableTreeNode("New node");
            model.insertNodeInto(child, parent, parent.getChildCount());
            //parent.add(child);
            //model.reload(parent); //= model.nodeStructureChanged(parent);
            tree.scrollPathToVisible(new TreePath(child.getPath()));
        }
    };
    private final Action editNodeAction = new AbstractAction("edit") {
        @Override public void actionPerformed(ActionEvent e) {
            Object node = path.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
                textField.setText(leaf.getUserObject().toString());
                JTree tree = (JTree) getInvoker();
                int result = JOptionPane.showConfirmDialog(tree, textField, "edit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    String str = textField.getText();
                    if (!str.trim().isEmpty()) {
                        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                        model.valueForPathChanged(path, str);
                        //leaf.setUserObject(str);
                        //model.nodeChanged(leaf);
                    }
                }
            }
        }
    };
    private final Action removeNodeAction = new AbstractAction("remove") {
        @Override public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (!node.isRoot()) {
                JTree tree = (JTree) getInvoker();
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.removeNodeFromParent(node);
            }
        }
    };
    protected TreePopupMenu() {
        super();
        textField.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                textField.requestFocusInWindow();
            }
            @Override public void ancestorMoved(AncestorEvent e)   { /* not needed */ }
            @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
        });
        add(addNodeAction);
        add(editNodeAction);
        addSeparator();
        add(removeNodeAction);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTree) {
            JTree tree = (JTree) c;
            //TreePath[] tsp = tree.getSelectionPaths();
            path = tree.getPathForLocation(x, y);
            //if (Objects.nonNull(path) && Arrays.asList(tsp).contains(path)) {
            if (Objects.nonNull(path)) {
                tree.setSelectionPath(path);
                super.show(c, x, y);
            }
        }
    }
}
