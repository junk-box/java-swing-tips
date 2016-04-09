package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.time.*;
import java.time.chrono.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"LocalDateTime", "String", "Boolean"};
    private final Object[][] data = {
        {LocalDateTime.now(), "aaa", true}, {LocalDateTime.now(), "bbb", false},
        {LocalDateTime.now(), "CCC", true}, {LocalDateTime.now(), "DDD", false}
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
        table.getColumnModel().getColumn(0).setCellRenderer(new LocalDateTimeTableCellRenderer());
        table.getColumnModel().getColumn(0).setCellEditor(new LocalDateTimeTableCellEditor());
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

class LocalDateTimeTableCellRenderer extends DefaultTableCellRenderer {
    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd";
    private final transient DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof TemporalAccessor) {
            setText(dateTimeFormatter.format((TemporalAccessor) value));
        }
        return this;
    }
}

class LocalDateTimeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JComboBox<LocalDateTime> comboBox;
    private LocalDateTime selectedDate;

    public LocalDateTimeTableCellEditor() {
        super();
        UIManager.put("ComboBox.squareButton", Boolean.FALSE);
        comboBox = new JComboBox<LocalDateTime>() {
            @Override public void updateUI() {
                super.updateUI();
                setBorder(BorderFactory.createEmptyBorder());
                setOpaque(true);
                setRenderer(new LocalDateTimeCellRenderer<LocalDateTime>());
                setUI(new BasicComboBoxUI() {
                    @Override protected JButton createArrowButton() {
                        JButton button = new JButton(); //.createArrowButton();
                        button.setBorder(BorderFactory.createEmptyBorder());
                        button.setVisible(false);
                        return button;
                    }
                });
            }
        };
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof LocalDateTime) {
            comboBox.setModel(new DefaultComboBoxModel<LocalDateTime>() {
                @Override public LocalDateTime getElementAt(int index) {
                    //if (index >= 0 && index < getSize()) {
                    return LocalDateTime.now().plusDays(index);
                }
                @Override public int getSize() {
                    return 7;
                }
                @Override public Object getSelectedItem() {
                    return selectedDate;
                }
                @Override public void setSelectedItem(Object anItem) {
                    selectedDate = (LocalDateTime) anItem;
                }
            });
            selectedDate = (LocalDateTime) value;
        }
        return comboBox;
    }
    @Override public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            MouseEvent e = (MouseEvent) anEvent;
            return e.getID() != MouseEvent.MOUSE_DRAGGED;
        }
        return true;
    }
    @Override public boolean stopCellEditing() {
        if (comboBox.isEditable()) {
            // Commit edited value.
            comboBox.actionPerformed(new ActionEvent(this, 0, ""));
        }
        return super.stopCellEditing();
    }
    @Override public boolean isCellEditable(EventObject e) {
        return true;
    }
}

class LocalDateTimeCellRenderer<E extends LocalDateTime> extends JLabel implements ListCellRenderer<E> {
    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd";
    private final transient DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        if (Objects.nonNull(value)) {
            setText(dateTimeFormatter.format((TemporalAccessor) value));
        }
        setOpaque(true);
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}
