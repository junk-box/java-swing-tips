package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"JSpinner", "Buttons"};
    private final Object[][] data = {
        {50, 100}, {100, 50}, {30, 20}, {0, 100}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        @Override public void updateUI() {
            super.updateUI();
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    TableColumn column = getColumnModel().getColumn(0);
                    column.setCellRenderer(new SpinnerRenderer());
                    column.setCellEditor(new SpinnerEditor());

                    column = getColumnModel().getColumn(1);
                    column.setCellRenderer(new ButtonsRenderer());
                    column.setCellEditor(new ButtonsEditor());
                    repaint();
                }
            });
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        table.setRowHeight(36);
        table.setAutoCreateRowSorter(true);
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

class SpinnerPanel extends JPanel {
    public final JSpinner spinner = new JSpinner(new SpinnerNumberModel(100, 0, 200, 1));
    protected SpinnerPanel() {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1d;
        c.insets = new Insets(0, 10, 0, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        setOpaque(true);
        add(spinner, c);
    }
}
//*
class SpinnerRenderer implements TableCellRenderer {
    private final SpinnerPanel p = new SpinnerPanel();
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        p.spinner.setValue((Integer) value);
        return p;
    }
}

class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
    private final SpinnerPanel p = new SpinnerPanel();
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        p.setBackground(table.getSelectionBackground());
        p.spinner.setValue((Integer) value);
        return p;
    }
    @Override public Object getCellEditorValue() {
        return p.spinner.getValue();
    }
//     @Override public boolean isCellEditable(EventObject e) {
//         return true;
//     }
//     @Override public boolean shouldSelectCell(EventObject anEvent) {
//         return true;
//     }
    @Override public boolean stopCellEditing() {
        try {
            p.spinner.commitEdit();
        } catch (ParseException pe) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        return super.stopCellEditing();
        //fireEditingStopped();
        //return true;
    }
}
/*/
class SpinnerRenderer extends SpinnerPanel implements TableCellRenderer {
    @Override public void updateUI() {
        super.updateUI();
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        spinner.setValue((Integer) value);
        return this;
    }
}

class SpinnerEditor extends SpinnerPanel implements TableCellEditor {
    protected transient ChangeEvent changeEvent;
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.setBackground(table.getSelectionBackground());
        spinner.setValue((Integer) value);
        return this;
    }
    @Override public Object getCellEditorValue() {
        return spinner.getValue();
    }

    //Copied from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    @Override public boolean isCellEditable(EventObject e) {
        return true;
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        try {
            spinner.commitEdit();
        } catch (ParseException pe) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
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
//*/

class ButtonsPanel extends JPanel {
    public final List<JButton> buttons = Arrays.asList(new JButton("+"), new JButton("-"));
    public final JLabel label = new JLabel() {
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = 50;
            return d;
        }
    };
    public int i = -1;
    protected ButtonsPanel() {
        super();
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        setOpaque(true);
        add(label);
        for (JButton b: buttons) {
            b.setFocusable(false);
            b.setRolloverEnabled(false);
            add(b);
        }
    }
}
//*
class ButtonsRenderer implements TableCellRenderer {
    private final ButtonsPanel p = new ButtonsPanel();
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        p.label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        p.label.setText(Objects.toString(value, ""));
        return p;
    }
}

class ButtonsEditor extends AbstractCellEditor implements TableCellEditor {
    private final ButtonsPanel p = new ButtonsPanel();
    protected ButtonsEditor() {
        super();
        p.buttons.get(0).addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                p.i++;
                p.label.setText(Integer.toString(p.i));
                fireEditingStopped();
            }
        });

        p.buttons.get(1).addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                p.i--;
                p.label.setText(Integer.toString(p.i));
                fireEditingStopped();
            }
        });

        p.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                fireEditingStopped();
            }
        });
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        p.setBackground(table.getSelectionBackground());
        p.label.setForeground(table.getSelectionForeground());
        p.i = (Integer) value;
        p.label.setText(Integer.toString(p.i));
        return p;
    }
    @Override public Object getCellEditorValue() {
        return p.i;
    }
//     //AbstractCellEditor
//     @Override public boolean isCellEditable(EventObject e) {
//         return true;
//     }
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
/*/
class ButtonsRenderer extends ButtonsPanel implements TableCellRenderer {
    @Override public void updateUI() {
        super.updateUI();
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        label.setText(Objects.toString(value, ""));
        return this;
    }
}

class ButtonsEditor extends ButtonsPanel implements TableCellEditor {
    protected transient ChangeEvent changeEvent;
    public ButtonsEditor() {
        super();
        buttons.get(0).addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                i++;
                label.setText(Integer.toString(i));
                fireEditingStopped();
            }
        });

        buttons.get(1).addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                i--;
                label.setText(Integer.toString(i));
                fireEditingStopped();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                fireEditingStopped();
            }
        });
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.setBackground(table.getSelectionBackground());
        label.setForeground(table.getSelectionForeground());
        i = (Integer) value;
        label.setText(Integer.toString(i));
        return this;
    }
    @Override public Object getCellEditorValue() {
        return i;
    }

    //Copied from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
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
//*/
