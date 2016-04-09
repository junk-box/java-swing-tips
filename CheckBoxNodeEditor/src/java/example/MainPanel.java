package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTree tree = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                setCellEditor(null);
                super.updateUI();
                //???#1: JDK 1.6.0 bug??? Nimbus LnF
                setCellRenderer(new CheckBoxNodeRenderer());
                setCellEditor(new CheckBoxNodeEditor());
            }
        };
        TreeModel model = tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        Enumeration e = root.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            node.setUserObject(new CheckBoxNode(Objects.toString(node.getUserObject(), ""), Status.DESELECTED));
        }
        model.addTreeModelListener(new CheckBoxStatusUpdateListener());

        tree.setEditable(true);
        tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        tree.expandRow(0);
        //tree.setToggleClickCount(1);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(new JScrollPane(tree));
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

class TriStateCheckBox extends JCheckBox {
    @Override public void updateUI() {
        Icon currentIcon = getIcon();
        setIcon(null);
        super.updateUI();
        if (Objects.nonNull(currentIcon)) {
            setIcon(new IndeterminateIcon());
        }
        setOpaque(false);
    }
}

class IndeterminateIcon implements Icon {
    private static final Color FOREGROUND = new Color(50, 20, 255, 200); //TEST: UIManager.getColor("CheckBox.foreground");
    private static final int SIDE_MARGIN = 4;
    private static final int HEIGHT = 2;
    private final Icon icon = UIManager.getIcon("CheckBox.icon");
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        int w = getIconWidth();
        int h = getIconHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        icon.paintIcon(c, g2, 0, 0);
        g2.setPaint(FOREGROUND);
        g2.fillRect(SIDE_MARGIN, (h - HEIGHT) / 2, w - SIDE_MARGIN - SIDE_MARGIN, HEIGHT);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return icon.getIconWidth();
    }
    @Override public int getIconHeight() {
        return icon.getIconHeight();
    }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }

class CheckBoxNode {
    public final String label;
    public final Status status;
    protected CheckBoxNode(String label) {
        this.label = label;
        status = Status.INDETERMINATE;
    }
    protected CheckBoxNode(String label, Status status) {
        this.label = label;
        this.status = status;
    }
    @Override public String toString() {
        return label;
    }
}

class CheckBoxStatusUpdateListener implements TreeModelListener {
    private boolean adjusting;
    @Override public void treeNodesChanged(TreeModelEvent e) {
        if (adjusting) {
            return;
        }
        adjusting = true;
        Object[] children = e.getChildren();
        DefaultTreeModel model = (DefaultTreeModel) e.getSource();

        DefaultMutableTreeNode node;
        CheckBoxNode c; // = (CheckBoxNode) node.getUserObject();
        if (Objects.nonNull(children) && children.length == 1) {
            node = (DefaultMutableTreeNode) children[0];
            c = (CheckBoxNode) node.getUserObject();
            TreePath parent = e.getTreePath();
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) parent.getLastPathComponent();
            while (Objects.nonNull(n)) {
                updateParentUserObject(n);
                DefaultMutableTreeNode tmp = (DefaultMutableTreeNode) n.getParent();
                if (Objects.nonNull(tmp)) {
                    n = tmp;
                } else {
                    break;
                }
            }
            model.nodeChanged(n);
        } else {
            node = (DefaultMutableTreeNode) model.getRoot();
            c = (CheckBoxNode) node.getUserObject();
        }
        updateAllChildrenUserObject(node, c.status);
        model.nodeChanged(node);
        adjusting = false;
    }
    private void updateParentUserObject(DefaultMutableTreeNode parent) {
        int selectedCount = 0;
        Enumeration children = parent.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
            CheckBoxNode check = (CheckBoxNode) node.getUserObject();
            if (check.status == Status.INDETERMINATE) {
                selectedCount = -1;
                break;
            }
            if (check.status == Status.SELECTED) {
                selectedCount++;
            }
        }
        String label = ((CheckBoxNode) parent.getUserObject()).label;
        if (selectedCount == 0) {
            parent.setUserObject(new CheckBoxNode(label, Status.DESELECTED));
        } else if (selectedCount == parent.getChildCount()) {
            parent.setUserObject(new CheckBoxNode(label, Status.SELECTED));
        } else {
            parent.setUserObject(new CheckBoxNode(label));
        }
    }
    private void updateAllChildrenUserObject(DefaultMutableTreeNode root, Status status) {
        Enumeration breadth = root.breadthFirstEnumeration();
        while (breadth.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) breadth.nextElement();
            if (Objects.equals(root, node)) {
                continue;
            }
            CheckBoxNode check = (CheckBoxNode) node.getUserObject();
            node.setUserObject(new CheckBoxNode(check.label, status));
        }
    }
    @Override public void treeNodesInserted(TreeModelEvent e)    { /* not needed */ }
    @Override public void treeNodesRemoved(TreeModelEvent e)     { /* not needed */ }
    @Override public void treeStructureChanged(TreeModelEvent e) { /* not needed */ }
}

class CheckBoxNodeRenderer implements TreeCellRenderer {
    private final JPanel panel = new JPanel(new BorderLayout());
    private final TriStateCheckBox checkBox = new TriStateCheckBox();
    private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        l.setFont(tree.getFont());
        if (value instanceof DefaultMutableTreeNode) {
            panel.setFocusable(false);
            panel.setRequestFocusEnabled(false);
            panel.setOpaque(false);
            checkBox.setEnabled(tree.isEnabled());
            checkBox.setFont(tree.getFont());
            checkBox.setFocusable(false);
            checkBox.setOpaque(false);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode) userObject;
                if (node.status == Status.INDETERMINATE) {
                    checkBox.setIcon(new IndeterminateIcon());
                } else {
                    checkBox.setIcon(null);
                }
                l.setText(node.label);
                checkBox.setSelected(node.status == Status.SELECTED);
            }
            panel.add(checkBox, BorderLayout.WEST);
            panel.add(l);
            return panel;
        }
        return l;
    }
}

class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
    private final JPanel panel = new JPanel(new BorderLayout());
    private final TriStateCheckBox checkBox = new TriStateCheckBox() {
        private transient ActionListener handler;
        @Override public void updateUI() {
            removeActionListener(handler);
            super.updateUI();
            setOpaque(false);
            setFocusable(false);
            handler = e -> stopCellEditing();
            addActionListener(handler);
        }
    };
    private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    private String str;

    @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
        JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
        l.setFont(tree.getFont());
        if (value instanceof DefaultMutableTreeNode) {
            panel.setFocusable(false);
            panel.setRequestFocusEnabled(false);
            panel.setOpaque(false);
            checkBox.setEnabled(tree.isEnabled());
            checkBox.setFont(tree.getFont());
            //checkBox.setFocusable(false);
            //checkBox.setOpaque(false);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode) userObject;
                if (node.status == Status.INDETERMINATE) {
                    checkBox.setIcon(new IndeterminateIcon());
                } else {
                    checkBox.setIcon(null);
                }
                l.setText(node.label);
                checkBox.setSelected(node.status == Status.SELECTED);
                str = node.label;
            }
            panel.add(checkBox, BorderLayout.WEST);
            panel.add(l);
            return panel;
        }
        return l;
    }
    @Override public Object getCellEditorValue() {
        return new CheckBoxNode(str, checkBox.isSelected() ? Status.SELECTED : Status.DESELECTED);
    }
    @Override public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent && e.getSource() instanceof JTree) {
            MouseEvent me = (MouseEvent) e;
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getPathForLocation(me.getX(), me.getY());
            Rectangle r = tree.getPathBounds(path);
            if (Objects.isNull(r)) {
                return false;
            }
            Dimension d = checkBox.getPreferredSize();
            r.setSize(new Dimension(d.width, r.height));
            if (r.contains(me.getX(), me.getY())) {
                return true;
            }
        }
        return false;
    }
//     //AbstractCellEditor
//     @Override public boolean shouldSelectCell(EventObject anEvent) {
//         return true;
//     }
//     @Override public boolean stopCellEditing() {
//         fireEditingStopped();
//         return true;
//     }
//     @Override public void cancelCellEditing() {
//         fireEditingCanceled();
//     }
}

// //*
// // extends JCheckBox TreeCellRenderer Editor version
// class CheckBoxNodeRenderer extends TriStateCheckBox implements TreeCellRenderer {
//     private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//     private final JPanel panel = new JPanel(new BorderLayout());
//     public CheckBoxNodeRenderer() {
//         super();
//         //Fixed?
//         //String uiName = getUI().getClass().getName();
//         //if (uiName.contains("Synth") && System.getProperty("java.version").startsWith("1.7.0")) {
//         //    System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
//         //    renderer.setBackgroundSelectionColor(new Color(0x0, true));
//         //}
//         panel.setFocusable(false);
//         panel.setRequestFocusEnabled(false);
//         panel.setOpaque(false);
//         panel.add(this, BorderLayout.WEST);
//         this.setOpaque(false);
//     }
//     @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//         JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//         l.setFont(tree.getFont());
//         if (value instanceof DefaultMutableTreeNode) {
//             this.setEnabled(tree.isEnabled());
//             this.setFont(tree.getFont());
//             Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//             if (userObject instanceof CheckBoxNode) {
//                 CheckBoxNode node = (CheckBoxNode) userObject;
//                 if (node.status == Status.INDETERMINATE) {
//                     setIcon(new IndeterminateIcon());
//                 } else {
//                     setIcon(null);
//                 }
//                 l.setText(node.label);
//                 setSelected(node.status == Status.SELECTED);
//             }
//             //panel.add(this, BorderLayout.WEST);
//             panel.add(l);
//             return panel;
//         }
//         return l;
//     }
//     //Fixed?
//     //@Override public void updateUI() {
//     //    super.updateUI();
//     //    if (Objects.nonNull(panel)) {
//     //        //panel.removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
//     //        panel.updateUI();
//     //        //panel.add(this, BorderLayout.WEST);
//     //    }
//     //    setName("Tree.cellRenderer");
//     //    //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
//     //    //if (System.getProperty("java.version").startsWith("1.6.0")) {
//     //    //    renderer = new DefaultTreeCellRenderer();
//     //    //}
//     //}
// }
//
// class CheckBoxNodeEditor extends TriStateCheckBox implements TreeCellEditor {
//     private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//     private final JPanel panel = new JPanel(new BorderLayout());
//     private String str;
//
//     protected CheckBoxNodeEditor() {
//         super();
//         this.addActionListener(e -> stopCellEditing());
//         panel.setFocusable(false);
//         panel.setRequestFocusEnabled(false);
//         panel.setOpaque(false);
//         panel.add(this, BorderLayout.WEST);
//         this.setOpaque(false);
//     }
//     @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
//         //JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//         JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
//         l.setFont(tree.getFont());
//         if (value instanceof DefaultMutableTreeNode) {
//             this.setEnabled(tree.isEnabled());
//             this.setFont(tree.getFont());
//             Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//             if (userObject instanceof CheckBoxNode) {
//                 CheckBoxNode node = (CheckBoxNode) userObject;
//                 if (node.status == Status.INDETERMINATE) {
//                     setIcon(new IndeterminateIcon());
//                 } else {
//                     setIcon(null);
//                 }
//                 l.setText(node.label);
//                 setSelected(node.status == Status.SELECTED);
//                 str = node.label;
//             }
//             //panel.add(this, BorderLayout.WEST);
//             panel.add(l);
//             return panel;
//         }
//         return l;
//     }
//     @Override public Object getCellEditorValue() {
//         return new CheckBoxNode(str, isSelected() ? Status.SELECTED : Status.DESELECTED);
//     }
//     @Override public boolean isCellEditable(EventObject e) {
//         if (e instanceof MouseEvent && e.getSource() instanceof JTree) {
//             MouseEvent me = (MouseEvent) e;
//             JTree tree = (JTree) e.getSource();
//             TreePath path = tree.getPathForLocation(me.getX(), me.getY());
//             Rectangle r = tree.getPathBounds(path);
//             if (Objects.isNull(r)) {
//                 return false;
//             }
//             Dimension d = getPreferredSize();
//             r.setSize(new Dimension(d.width, r.height));
//             if (r.contains(me.getX(), me.getY())) {
//                 //Fixed: Bug ID: JDK-8023474 First mousepress doesn't start editing in JTree
//                 //       http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8023474
//                 //if (Objects.isNull(str) && System.getProperty("java.version").startsWith("1.7.0")) {
//                 //    System.out.println("XXX: Java 7, only on first run\n" + getBounds());
//                 //    setBounds(new Rectangle(d.width, r.height));
//                 //}
//                 //System.out.println(getBounds());
//                 return true;
//             }
//         }
//         return false;
//     }
//     //Fixed?
//     //@Override public void updateUI() {
//     //    super.updateUI();
//     //    setName("Tree.cellEditor");
//     //    if (Objects.nonNull(panel)) {
//     //        //panel.removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
//     //        panel.updateUI();
//     //        //panel.add(this, BorderLayout.WEST);
//     //    }
//     //    //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
//     //    //if (System.getProperty("java.version").startsWith("1.6.0")) {
//     //    //    renderer = new DefaultTreeCellRenderer();
//     //    //}
//     //}
//
//     //Copied from AbstractCellEditor
// //     protected EventListenerList listenerList = new EventListenerList();
// //     protected transient ChangeEvent changeEvent;
//     @Override public boolean shouldSelectCell(EventObject anEvent) {
//         return true;
//     }
//     @Override public boolean stopCellEditing() {
//         fireEditingStopped();
//         return true;
//     }
//     @Override public void cancelCellEditing() {
//         fireEditingCanceled();
//     }
//     @Override public void addCellEditorListener(CellEditorListener l) {
//         listenerList.add(CellEditorListener.class, l);
//     }
//     @Override public void removeCellEditorListener(CellEditorListener l) {
//         listenerList.remove(CellEditorListener.class, l);
//     }
//     public CellEditorListener[] getCellEditorListeners() {
//         return listenerList.getListeners(CellEditorListener.class);
//     }
//     protected void fireEditingStopped() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for (int i = listeners.length - 2; i >= 0; i -= 2) {
//             if (listeners[i] == CellEditorListener.class) {
//                 // Lazily create the event:
//                 if (Objects.isNull(changeEvent)) {
//                     changeEvent = new ChangeEvent(this);
//                 }
//                 ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
//             }
//         }
//     }
//     protected void fireEditingCanceled() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for (int i = listeners.length - 2; i >= 0; i -= 2) {
//             if (listeners[i] == CellEditorListener.class) {
//                 // Lazily create the event:
//                 if (Objects.isNull(changeEvent)) {
//                     changeEvent = new ChangeEvent(this);
//                 }
//                 ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
//             }
//         }
//     }
// }
// /*/
// // extends JPanel TreeCellRenderer Editor version
// class CheckBoxNodeRenderer extends JPanel implements TreeCellRenderer {
//     private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//     private final TriStateCheckBox check = new TriStateCheckBox();
//     protected CheckBoxNodeRenderer() {
//         super(new BorderLayout());
//         String uiName = getUI().getClass().getName();
//         if (uiName.contains("Synth") && System.getProperty("java.version").startsWith("1.7.0")) {
//             System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
//             renderer.setBackgroundSelectionColor(new Color(0x0, true));
//         }
//         setFocusable(false);
//         setRequestFocusEnabled(false);
//         setOpaque(false);
//         add(check, BorderLayout.WEST);
//         check.setOpaque(false);
//     }
//     @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//         JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//         l.setFont(tree.getFont());
//         if (value instanceof DefaultMutableTreeNode) {
//             check.setEnabled(tree.isEnabled());
//             check.setFont(tree.getFont());
//             Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//             if (userObject instanceof CheckBoxNode) {
//                 CheckBoxNode node = (CheckBoxNode) userObject;
//                 if (node.status == Status.INDETERMINATE) {
//                     check.setIcon(new IndeterminateIcon());
//                 } else {
//                     check.setIcon(null);
//                 }
//                 l.setText(node.label);
//                 check.setSelected(node.status == Status.SELECTED);
//             }
//             //add(this, BorderLayout.WEST);
//             add(l);
//             return this;
//         }
//         return l;
//     }
//     @Override public void updateUI() {
//         super.updateUI();
//         if (Objects.nonNull(check)) {
//             //removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
//             check.updateUI();
//             //add(check, BorderLayout.WEST);
//         }
//         setName("Tree.cellRenderer");
//         //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
//         //if (System.getProperty("java.version").startsWith("1.6.0")) {
//         //    renderer = new DefaultTreeCellRenderer();
//         //}
//     }
// }
//
// class CheckBoxNodeEditor extends JPanel implements TreeCellEditor {
//     private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//     private final TriStateCheckBox check = new TriStateCheckBox();
//     private String str = null;
//     protected CheckBoxNodeEditor() {
//         super(new BorderLayout());
//         check.addActionListener(e -> stopCellEditing());
//         setFocusable(false);
//         setRequestFocusEnabled(false);
//         setOpaque(false);
//         add(check, BorderLayout.WEST);
//         check.setOpaque(false);
//     }
//     @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
//         //JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//         JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
//         l.setFont(tree.getFont());
//         if (value instanceof DefaultMutableTreeNode) {
//             check.setEnabled(tree.isEnabled());
//             check.setFont(tree.getFont());
//             Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//             if (userObject instanceof CheckBoxNode) {
//                 CheckBoxNode node = (CheckBoxNode) userObject;
//                 if (node.status == Status.INDETERMINATE) {
//                     check.setIcon(new IndeterminateIcon());
//                 } else {
//                     check.setIcon(null);
//                 }
//                 l.setText(node.label);
//                 check.setSelected(node.status == Status.SELECTED);
//                 str = node.label;
//             }
//             //add(this, BorderLayout.WEST);
//             add(l);
//             return this;
//         }
//         return l;
//     }
//     @Override public Object getCellEditorValue() {
//         return new CheckBoxNode(str, check.isSelected() ? Status.SELECTED : Status.DESELECTED);
//     }
//     @Override public boolean isCellEditable(EventObject e) {
//         if (e instanceof MouseEvent && e.getSource() instanceof JTree) {
//             MouseEvent me = (MouseEvent) e;
//             JTree tree = (JTree) e.getSource();
//             TreePath path = tree.getPathForLocation(me.getX(), me.getY());
//             Rectangle r = tree.getPathBounds(path);
//             if (Objects.isNull(r)) {
//                 return false;
//             }
//             Dimension d = check.getPreferredSize();
//             r.setSize(new Dimension(d.width, r.height));
//             if (r.contains(me.getX(), me.getY())) {
//                 //Fixed: Bug ID: JDK-8023474 First mousepress doesn't start editing in JTree
//                 //       http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8023474
//                 //if (Objects.isNull(str) && System.getProperty("java.version").startsWith("1.7.0")) {
//                 //    System.out.println("XXX: Java 7, only on first run\n" + getBounds());
//                 //    check.setBounds(new Rectangle(d.width, r.height));
//                 //}
//                 //System.out.println(getBounds());
//                 return true;
//             }
//         }
//         return false;
//     }
//     @Override public void updateUI() {
//         super.updateUI();
//         setName("Tree.cellEditor");
//         if (Objects.nonNull(check)) {
//             //removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
//             check.updateUI();
//             //add(check, BorderLayout.WEST);
//         }
//         //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
//         //if (System.getProperty("java.version").startsWith("1.6.0")) {
//         //    renderer = new DefaultTreeCellRenderer();
//         //}
//     }
//
//     //Copied from AbstractCellEditor
//     protected EventListenerList listenerList = new EventListenerList();
//     protected transient ChangeEvent changeEvent;
//     @Override public boolean shouldSelectCell(EventObject anEvent) {
//         return true;
//     }
//     @Override public boolean stopCellEditing() {
//         fireEditingStopped();
//         return true;
//     }
//     @Override public void cancelCellEditing() {
//         fireEditingCanceled();
//     }
//     @Override public void addCellEditorListener(CellEditorListener l) {
//         listenerList.add(CellEditorListener.class, l);
//     }
//     @Override public void removeCellEditorListener(CellEditorListener l) {
//         listenerList.remove(CellEditorListener.class, l);
//     }
//     public CellEditorListener[] getCellEditorListeners() {
//         return listenerList.getListeners(CellEditorListener.class);
//     }
//     protected void fireEditingStopped() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for (int i = listeners.length - 2; i >= 0; i -= 2) {
//             if (listeners[i] == CellEditorListener.class) {
//                 // Lazily create the event:
//                 if (Objects.isNull(changeEvent)) {
//                     changeEvent = new ChangeEvent(this);
//                 }
//                 ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
//             }
//         }
//     }
//     protected void fireEditingCanceled() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for (int i = listeners.length - 2; i >= 0; i -= 2) {
//             if (listeners[i] == CellEditorListener.class) {
//                 // Lazily create the event:
//                 if (Objects.isNull(changeEvent)) {
//                     changeEvent = new ChangeEvent(this);
//                 }
//                 ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
//             }
//         }
//     }
// }
// //*/
