package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"user", "rwx"};
    private final Object[][] data = {
        {"owner", EnumSet.allOf(Permissions.class)}, //EnumSet.of(Permissions.READ, Permissions.WRITE, Permissions.EXECUTE)},
        {"group", EnumSet.of(Permissions.READ)},
        {"other", EnumSet.noneOf(Permissions.class)}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel label = new JLabel();

    public MainPanel() {
        super(new BorderLayout());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        if (System.getProperty("java.version").startsWith("1.6.0")) {
            //1.6.0_xx bug? column header click -> edit cancel?
            table.getTableHeader().addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    if (table.isEditing()) {
                        table.getCellEditor().stopCellEditing();
                    }
                }
            });
        }

        TableColumn c = table.getColumnModel().getColumn(1);
        c.setCellRenderer(new CheckBoxesRenderer());
        c.setCellEditor(new CheckBoxesEditor());

        final EnumMap<Permissions, Integer> map = new EnumMap<>(Permissions.class);
        map.put(Permissions.READ,    1 << 2);
        map.put(Permissions.WRITE,   1 << 1);
        map.put(Permissions.EXECUTE, 1 << 0);

        JPanel p = new JPanel(new BorderLayout());
        p.add(label);
        p.add(new JButton(new AbstractAction("ls -l (chmod)") {
            private static final String M = "-";
            @Override public void actionPerformed(ActionEvent e) {
                StringBuilder nbuf = new StringBuilder(3);
                StringBuilder buf = new StringBuilder(9);
                for (int i = 0; i < model.getRowCount(); i++) {
                    EnumSet<?> v = (EnumSet<?>) model.getValueAt(i, 1);
                    int flg = 0;
                    if (v.contains(Permissions.READ)) {
                        flg |= map.get(Permissions.READ);
                        buf.append('r');
                    } else {
                        buf.append(M);
                    }
                    if (v.contains(Permissions.WRITE)) {
                        flg |= map.get(Permissions.WRITE);
                        buf.append('w');
                    } else {
                        buf.append(M);
                    }
                    if (v.contains(Permissions.EXECUTE)) {
                        flg |= map.get(Permissions.EXECUTE);
                        buf.append('x');
                    } else {
                        buf.append(M);
                    }
                    nbuf.append(flg);
                }
                label.setText(String.format(" %s %s%s", nbuf, M, buf));
            }
        }), BorderLayout.EAST);
        add(new JScrollPane(table));
        add(p, BorderLayout.SOUTH);
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

enum Permissions { EXECUTE, WRITE, READ; }

class CheckBoxesPanel extends JPanel {
    protected final String[] title = {"r", "w", "x"};
    public JCheckBox[] buttons;
    protected CheckBoxesPanel() {
        super();
        setOpaque(false);
        setBackground(new Color(0x0, true));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        initButtons();
    }
    private void initButtons() {
        buttons = new JCheckBox[title.length];
        for (int i = 0; i < buttons.length; i++) {
            JCheckBox b = new JCheckBox(title[i]);
            b.setOpaque(false);
            b.setFocusable(false);
            b.setRolloverEnabled(false);
            b.setBackground(new Color(0x0, true));
            buttons[i] = b;
            add(b);
            add(Box.createHorizontalStrut(5));
        }
    }
    protected void updateButtons(Object v) {
        removeAll();
        initButtons();
        EnumSet<?> f = v instanceof EnumSet ? (EnumSet<?>) v : EnumSet.noneOf(Permissions.class);
        buttons[0].setSelected(f.contains(Permissions.READ));
        buttons[1].setSelected(f.contains(Permissions.WRITE));
        buttons[2].setSelected(f.contains(Permissions.EXECUTE));
    }
}

class CheckBoxesRenderer extends CheckBoxesPanel implements TableCellRenderer {
    @Override public void updateUI() {
        super.updateUI();
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        updateButtons(value);
        return this;
    }
    //public static class UIResource extends CheckBoxesRenderer implements javax.swing.plaf.UIResource {}
}

class CheckBoxesEditor extends CheckBoxesPanel implements TableCellEditor {
    protected transient ChangeEvent changeEvent;

    protected CheckBoxesEditor() {
        super();
        ActionMap am = getActionMap();
        for (int i = 0; i < buttons.length; i++) {
            //buttons[i].addActionListener(al);
            final String t = title[i];
            am.put(t, new AbstractAction(t) {
                @Override public void actionPerformed(ActionEvent e) {
                    for (JCheckBox b: buttons) {
                        if (b.getText().equals(t)) {
                            b.doClick();
                            break;
                        }
                    }
                    fireEditingStopped();
                }
            });
        }
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), title[0]);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), title[1]);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), title[2]);
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        updateButtons(value);
        return this;
    }
    @Override public Object getCellEditorValue() {
        EnumSet<Permissions> f = EnumSet.noneOf(Permissions.class);
        if (buttons[0].isSelected()) {
            f.add(Permissions.READ);
        }
        if (buttons[1].isSelected()) {
            f.add(Permissions.WRITE);
        }
        if (buttons[2].isSelected()) {
            f.add(Permissions.EXECUTE);
        }
        return f;
    }

    //Copied from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    //protected transient ChangeEvent changeEvent;
    @Override public boolean isCellEditable(EventObject e) {
        return true;
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
            }
        }
    }
}
