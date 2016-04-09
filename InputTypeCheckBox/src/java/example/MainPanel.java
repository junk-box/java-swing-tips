package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
    private final Object[][] data = {
        {true, 1, "BBB"}, {false, 12, "AAA"},
        {true, 2, "DDD"}, {false,  5, "CCC"},
        {true, 3, "EEE"}, {false,  6, "GGG"},
        {true, 4, "FFF"}, {false,  7, "HHH"}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        private static final int MODEL_COLUMN_INDEX = 0;
        private transient HeaderCheckBoxHandler handler;
        @Override public void updateUI() {
            // Bug ID: 6788475 Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
            // http://bugs.java.com/view_bug.do?bug_id=6788475
            // XXX: set dummy ColorUIResource
            setSelectionForeground(new ColorUIResource(Color.RED));
            setSelectionBackground(new ColorUIResource(Color.RED));
            getTableHeader().removeMouseListener(handler);
            TableModel m = getModel();
            if (Objects.nonNull(m)) {
                m.removeTableModelListener(handler);
            }
            super.updateUI();

            m = getModel();
            for (int i = 0; i < m.getColumnCount(); i++) {
                TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
                if (r instanceof Component) {
                    SwingUtilities.updateComponentTreeUI((Component) r);
                }
            }
            TableColumn column = getColumnModel().getColumn(MODEL_COLUMN_INDEX);
            column.setHeaderRenderer(new HeaderRenderer());
            column.setHeaderValue(Status.INDETERMINATE);

            handler = new HeaderCheckBoxHandler(this, MODEL_COLUMN_INDEX);
            m.addTableModelListener(handler);
            getTableHeader().addMouseListener(handler);
        }
        @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
            Component c = super.prepareEditor(editor, row, column);
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                b.setBackground(getSelectionBackground());
                b.setBorderPainted(true);
            }
            return c;
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table));
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
            for (UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(laf.getName())) {
                    UIManager.setLookAndFeel(laf.getClassName());
                }
            }
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

class HeaderRenderer implements TableCellRenderer {
    private static final String INPUT = "<html><table cellpadding='0' cellspacing='0'><td><input type='checkbox'><td>&nbsp;Check All";
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
        JLabel l = (JLabel) r.getTableCellRendererComponent(table, INPUT, isSelected, hasFocus, row, column);

//         //TEST:
//         JCheckBox check = new JCheckBox();
//         updateCheckBox(check, value);
//         String selected = check.isSelected() ? "checked " : "";
//         String indeterminate = check.isEnabled() ? "" : "disabled ";
//         l.setText(String.format("<html><table><td><input type='checkbox' %s%s/><td>Check All", selected, indeterminate));
//         System.out.println(l.getText());

        //http://stackoverflow.com/questions/7958378/listening-to-html-check-boxes-in-jtextpane-or-an-alternative
        for (Component c: l.getComponents()) {
            updateCheckBox(((Container) c).getComponent(0), value);
        }
        return l;
    }
    private static void updateCheckBox(Component c, Object value) {
        if (c instanceof JCheckBox) {
            JCheckBox check = (JCheckBox) c;
            check.setOpaque(false);
            check.setBorder(BorderFactory.createEmptyBorder());
            //check.setText("Check All");
            if (value instanceof Status) {
                switch ((Status) value) {
                  case SELECTED:
                    check.setSelected(true);
                    check.setEnabled(true);
                    break;
                  case DESELECTED:
                    check.setSelected(false);
                    check.setEnabled(true);
                    break;
                  case INDETERMINATE:
                    check.setSelected(true);
                    check.setEnabled(false);
                    break;
                  default:
                    throw new AssertionError("Unknown Status");
                }
            }
        }
    }
}

class HeaderCheckBoxHandler extends MouseAdapter implements TableModelListener {
    private final JTable table;
    private final int targetColumnIndex;
    protected HeaderCheckBoxHandler(JTable table, int index) {
        super();
        this.table = table;
        this.targetColumnIndex = index;
    }
    @Override public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
            int vci = table.convertColumnIndexToView(targetColumnIndex);
            TableColumn column = table.getColumnModel().getColumn(vci);
            Object status = column.getHeaderValue();
            TableModel m = table.getModel();
            if (fireUpdateEvent(m, column, status)) {
                JTableHeader h = table.getTableHeader();
                h.repaint(h.getHeaderRect(vci));
            }
        }
    }
    private boolean fireUpdateEvent(TableModel m, TableColumn column, Object status) {
        if (Status.INDETERMINATE.equals(status)) {
            boolean selected = true;
            boolean deselected = true;
            for (int i = 0; i < m.getRowCount(); i++) {
                Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
                selected &= b;
                deselected &= !b;
                if (selected == deselected) {
                    return false;
                }
            }
            if (deselected) {
                column.setHeaderValue(Status.DESELECTED);
            } else if (selected) {
                column.setHeaderValue(Status.SELECTED);
            } else {
                return false;
            }
        } else {
            column.setHeaderValue(Status.INDETERMINATE);
        }
        return true;
    }
    @Override public void mouseClicked(MouseEvent e) {
        JTableHeader header = (JTableHeader) e.getComponent();
        JTable table = header.getTable();
        TableColumnModel columnModel = table.getColumnModel();
        TableModel m = table.getModel();
        int vci = columnModel.getColumnIndexAtX(e.getX());
        int mci = table.convertColumnIndexToModel(vci);
        if (mci == targetColumnIndex && m.getRowCount() > 0) {
            TableColumn column = columnModel.getColumn(vci);
            Object v = column.getHeaderValue();
            boolean b = Status.DESELECTED.equals(v);
            for (int i = 0; i < m.getRowCount(); i++) {
                m.setValueAt(b, i, mci);
            }
            column.setHeaderValue(b ? Status.SELECTED : Status.DESELECTED);
            //header.repaint();
        }
    }
}

class ComponentIcon implements Icon {
    private final JComponent cmp;
    protected ComponentIcon(JComponent cmp) {
        this.cmp = cmp;
    }
    @Override public int getIconWidth() {
        return cmp.getPreferredSize().width;
    }
    @Override public int getIconHeight() {
        return cmp.getPreferredSize().height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight());
    }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }
