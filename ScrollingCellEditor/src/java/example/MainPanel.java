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
    private final String[] columnNames = {"JTextField", "JTextArea"};
    private final Object[][] data = {
        {"aaa", "JTextArea+JScrollPane\nCtrl-Enter: stopCellEditing"},
        {"bbb", "ggg"}, {"ccccDDD", "hhh\njjj\nkkk"}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());

        table.setAutoCreateRowSorter(true);
        table.setSurrendersFocusOnKeystroke(true);
        table.setRowHeight(64);

        TableColumn c = table.getColumnModel().getColumn(1);
        c.setCellEditor(new TextAreaCellEditor());
        c.setCellRenderer(new TextAreaCellRenderer());

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

class TextAreaCellEditor extends JTextArea implements TableCellEditor {
    private static final String KEY = "Stop-Cell-Editing";
    protected transient ChangeEvent changeEvent;
    private final JScrollPane scroll;

    protected TextAreaCellEditor() {
        super();
        scroll = new JScrollPane(this);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        //scroll.setViewportBorder(BorderFactory.createEmptyBorder());

        setLineWrap(true);
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK);
        getInputMap(JComponent.WHEN_FOCUSED).put(enter, KEY);
        getActionMap().put(KEY, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                stopCellEditing();
            }
        });
    }
    @Override public Object getCellEditorValue() {
        return getText();
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        System.out.println("getTableCellEditorComponent");
        setFont(table.getFont());
        setText(Objects.toString(value, ""));
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                setCaretPosition(getText().length());
                requestFocusInWindow();
                System.out.println("invokeLater: getTableCellEditorComponent");
            }
        });
        return scroll;
    }
    @Override public boolean isCellEditable(final EventObject e) {
        if (e instanceof MouseEvent) {
            return ((MouseEvent) e).getClickCount() >= 2;
        }
        System.out.println("isCellEditable");
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                if (e instanceof KeyEvent) {
                    KeyEvent ke = (KeyEvent) e;
                    char kc = ke.getKeyChar();
                    if (Character.isUnicodeIdentifierStart(kc)) {
                        setText(getText() + kc);
                        System.out.println("invokeLater: isCellEditable");
                    }
                }
            }
        });
        return true;
    }

    //Copied from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    //protected transient ChangeEvent changeEvent;
    @Override public boolean shouldSelectCell(EventObject e) {
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

class TextAreaCellRenderer extends JTextArea implements TableCellRenderer {
    @Override public void updateUI() {
        super.updateUI();
        setLineWrap(true);
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        //setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setFont(table.getFont());
        setText(Objects.toString(value, ""));
        return this;
    }
//     //Overridden for performance reasons. ---->
//     @Override public boolean isOpaque() {
//         Color back = getBackground();
//         Component p = getParent();
//         if (Objects.nonNull(p)) {
//             p = p.getParent();
//         } // p should now be the JTable.
//         boolean colorMatch = Objects.nonNull(back) && Objects.nonNull(p) && back.equals(p.getBackground()) && p.isOpaque();
//         return !colorMatch && super.isOpaque();
//     }
//     @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//         //String literal pool
//         //if (propertyName == "document" || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue)) {
//         if ("document".equals(propertyName) || !Objects.equals(oldValue, newValue) && ("font".equals(propertyName) || "foreground".equals(propertyName))) {
//             super.firePropertyChange(propertyName, oldValue, newValue);
//         }
//     }
//     @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { /* Overridden for performance reasons. */ }
//     @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
//     @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
//     @Override public void repaint()    { /* Overridden for performance reasons. */ }
//     @Override public void invalidate() { /* Overridden for performance reasons. */ }
//     @Override public void validate()   { /* Overridden for performance reasons. */ }
//     @Override public void revalidate() { /* Overridden for performance reasons. */ }
//     //<---- Overridden for performance reasons.
}
