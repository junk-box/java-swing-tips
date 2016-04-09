package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
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
    private final JTable table = new JTable(model) {
        private boolean isColumnSelectable(int column) {
            return convertColumnIndexToModel(column) == 0;
        }
        @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
            if (!isColumnSelectable(columnIndex)) {
                return;
            }
            super.changeSelection(rowIndex, columnIndex, toggle, extend);
        }
        @Override public boolean isCellEditable(int row, int column) {
            return isColumnSelectable(column);
        }
        @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            if (isColumnSelectable(column)) {
                return super.prepareRenderer(renderer, row, column);
            } else {
                return renderer.getTableCellRendererComponent(this, getValueAt(row, column), false, false, row, column);
            }
        }
    };
    public MainPanel() {
        super(new GridLayout(2, 1));
        table.setCellSelectionEnabled(true);
        //table.putClientProperty("Table.isFileList", Boolean.TRUE);
        //table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(new JTable(model)));
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
