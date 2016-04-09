package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.Date;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"Type", "Value"};
    private final Object[][] data = {
        {"String",  "text"      },
        {"Date",    new Date()  },
        {"Integer", 12          },
        {"Double",  3.45        },
        {"Boolean", Boolean.TRUE},
        {"Color",   Color.RED   }
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        JTable table1 = new PropertyTable(model);
        JTable table2 = new PropertyTable(model);
        table2.setTransferHandler(new HtmlTableTransferHandler());

        JPanel p = new JPanel(new GridLayout(3, 1));
        p.add(new JScrollPane(table1));
        p.add(new JScrollPane(table2));
        p.add(new JScrollPane(new JEditorPane("text/html", "")));
        add(p);
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

class PropertyTable extends JTable {
    private Class<?> editingClass;
    protected PropertyTable(TableModel model) {
        super(model);
    }
    //public PropertyTable(Object[][] data, String[] columnNames) {
    //    super(data, columnNames);
    //}
    private Class<?> getClassAt(int row, int column) {
        int mc = convertColumnIndexToModel(column);
        int mr = convertRowIndexToModel(row);
        return getModel().getValueAt(mr, mc).getClass();
    }
    @Override public void updateUI() {
        // Bug ID: 6788475 Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
        // http://bugs.java.com/view_bug.do?bug_id=6788475
        // XXX: set dummy ColorUIResource
        setSelectionForeground(new ColorUIResource(Color.RED));
        setSelectionBackground(new ColorUIResource(Color.RED));
        super.updateUI();
        setDefaultRenderer(Color.class, new ColorRenderer());
        setDefaultEditor(Color.class,   new ColorEditor());
        setDefaultEditor(Date.class,    new DateEditor());
    }
    @Override public TableCellRenderer getCellRenderer(int row, int column) {
        if (convertColumnIndexToModel(column) == 1) {
            return getDefaultRenderer(getClassAt(row, column));
        } else {
            return super.getCellRenderer(row, column);
        }
    }
    @Override public TableCellEditor getCellEditor(int row, int column) {
        if (convertColumnIndexToModel(column) == 1) {
            editingClass = getClassAt(row, column);
            return getDefaultEditor(editingClass);
        } else {
            editingClass = null;
            return super.getCellEditor(row, column);
        }
    }
    @Override public Class<?> getColumnClass(int column) {
        if (convertColumnIndexToModel(column) == 1) {
            return editingClass;
        } else {
            return super.getColumnClass(column);
        }
    }
}
//*
//delegation pattern
class DateEditor extends AbstractCellEditor implements TableCellEditor {
    private final JSpinner spinner;
    private final JSpinner.DateEditor editor;

    protected DateEditor() {
        super();
        spinner = new JSpinner(new SpinnerDateModel());
        editor = new JSpinner.DateEditor(spinner, "yyyy/MM/dd");
        spinner.setEditor(editor);
        setArrowButtonEnabled(false);
        editor.getTextField().setHorizontalAlignment(JFormattedTextField.LEFT);

        editor.getTextField().addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                setArrowButtonEnabled(false);
            }
            @Override public void focusGained(FocusEvent e) {
                //System.out.println("getTextField");
                setArrowButtonEnabled(true);
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        editor.getTextField().setCaretPosition(8);
                        editor.getTextField().setSelectionStart(8);
                        editor.getTextField().setSelectionEnd(10);
                    }
                });
            }
        });
        spinner.setBorder(BorderFactory.createEmptyBorder());
    }
    private void setArrowButtonEnabled(boolean flag) {
        for (Component c: spinner.getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).setEnabled(flag);
            }
        }
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        spinner.setValue(value);
        return spinner;
    }
    @Override public Object getCellEditorValue() {
        return spinner.getValue();
    }
//     //AbstractCellEditor
//     @Override public boolean isCellEditable(EventObject e) {
//         return true;
//     }
//     @Override public boolean shouldSelectCell(EventObject anEvent) {
//         return true;
//     }
    @Override public boolean stopCellEditing() {
        try {
            spinner.commitEdit();
        } catch (ParseException pe) {
            Toolkit.getDefaultToolkit().beep();
            return false;
//             // Edited value is invalid, spinner.getValue() will return
//             // the last valid value, you could revert the spinner to show that:
//             editor.getTextField().setValue(getValue());
        }
        return super.stopCellEditing();
        //fireEditingStopped();
        //return true;
    }
//     @Override public void cancelCellEditing() {
//         fireEditingCanceled();
//     }
}
/*/
//inheritence to extend a class
class DateEditor extends JSpinner implements TableCellEditor {
    protected transient ChangeEvent changeEvent;
    private final JSpinner.DateEditor editor;

    public DateEditor() {
        super(new SpinnerDateModel());
        editor = new JSpinner.DateEditor(this, "yyyy/MM/dd");
        setEditor(editor);
        setArrowButtonEnabled(false);
        editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);

        editor.getTextField().addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                setArrowButtonEnabled(false);
            }
            @Override public void focusGained(FocusEvent e) {
                //System.out.println("getTextField");
                setArrowButtonEnabled(true);
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        editor.getTextField().setCaretPosition(8);
                        editor.getTextField().setSelectionStart(8);
                        editor.getTextField().setSelectionEnd(10);
                    }
                });
            }
        });
        setBorder(BorderFactory.createEmptyBorder());
    }
    private void setArrowButtonEnabled(boolean flag) {
        for (Component c: getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).setEnabled(flag);
            }
        }
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        setValue(value);
        return this;
    }
    @Override public Object getCellEditorValue() {
        return getValue();
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
        try {
            commitEdit();
        } catch (ParseException pe) {
            Toolkit.getDefaultToolkit().beep();
            return false;
//             // Edited value is invalid, spinner.getValue() will return
//             // the last valid value, you could revert the spinner to show that:
//             editor.getTextField().setValue(getValue());
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

class ColorRenderer extends DefaultTableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Color) {
            Color color = (Color) value;
            l.setIcon(new ColorIcon(color));
            l.setText(String.format("(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue()));
        }
        return l;
    }
}

//http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDialogEditDemoProject/src/components/ColorEditor.java
class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    protected static final String EDIT = "edit";
    private final JButton button = new JButton();
    private final JColorChooser colorChooser;
    private final JDialog dialog;
    private Color currentColor;

    protected ColorEditor() {
        super();
        //Set up the editor (from the table's point of view),
        //which is a button.
        //This button brings up the color chooser dialog,
        //which is the editor from the user's point of view.
        button.setActionCommand(EDIT);
        button.addActionListener(this);
        //button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        button.setOpaque(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);

        //Set up the dialog that the button brings up.
        colorChooser = new JColorChooser();
        dialog = JColorChooser.createDialog(button, "Pick a Color", true, colorChooser, this, null);
    }
    /**
     * Handles events from the editor button and from
     * the dialog's OK button.
     */
    @Override public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            //The user has clicked the cell, so
            //bring up the dialog.
            button.setBackground(currentColor);
            button.setIcon(new ColorIcon(currentColor));
            colorChooser.setColor(currentColor);
            dialog.setVisible(true);

            //Make the renderer reappear.
            fireEditingStopped();
        } else { //User pressed dialog's "OK" button.
            currentColor = colorChooser.getColor();
        }
    }
    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    @Override public Object getCellEditorValue() {
        return currentColor;
    }
    //Implement the one method defined by TableCellEditor.
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentColor = (Color) value;
        button.setIcon(new ColorIcon(currentColor));
        button.setText(String.format("(%d, %d, %d)", currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue()));
        return button;
    }
}

class ColorIcon implements Icon {
    private final Color color;
    protected ColorIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(color);
        g2.fillRect(0, 0, getIconWidth(), getIconHeight());
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 10;
    }
    @Override public int getIconHeight() {
        return 10;
    }
}
