package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
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
    private final JTable table = new SortingColumnColorTable(model);
    //private final transient RowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);

    private MainPanel() {
        super(new BorderLayout());
        //table.setRowSorter(sorter);
        table.setAutoCreateRowSorter(true);

        add(new JButton(new AbstractAction("clear SortKeys") {
            @Override public void actionPerformed(ActionEvent e) {
                //sorter.setSortKeys(null);
                table.getRowSorter().setSortKeys(null);
            }
        }), BorderLayout.SOUTH);
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

class SortingColumnColorTable extends JTable {
    private static final Color EVEN_COLOR = new Color(250, 230, 230);
    public SortingColumnColorTable(TableModel model) {
        super(model);
    }
    @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        if (isRowSelected(row)) {
            c.setForeground(getSelectionForeground());
            c.setBackground(getSelectionBackground());
        } else {
            c.setForeground(getForeground());
            c.setBackground(isSortingColumn(column) ? EVEN_COLOR : getBackground());
        }
        return c;
    }
    private boolean isSortingColumn(int column) {
        RowSorter<? extends TableModel> sorter = getRowSorter();
        if (Objects.nonNull(sorter)) {
            List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
            if (keys.isEmpty()) {
                return false;
            }
            if (column == convertColumnIndexToView(keys.get(0).getColumn())) {
                return true;
            }
        }
        return false;
    }
}
