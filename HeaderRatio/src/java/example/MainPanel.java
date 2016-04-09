package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
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
    private final JTable table = new JTable(model);
    private final JTextField field = new JTextField("5 : 3 : 2");
    private final JCheckBox check = new JCheckBox("ComponentListener#componentResized(...)", true);

    public MainPanel() {
        super(new BorderLayout(5, 5));
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (check.isSelected()) {
                    setTableHeaderColumnRaito();
                }
            }
        });
        add(makeSettingPanel(), BorderLayout.NORTH);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 240));
    }

    private JPanel makeSettingPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(new JLabel("Ratio:"), BorderLayout.WEST);
        p.add(field);
        p.add(new JButton(new AbstractAction("revalidate") {
            @Override public void actionPerformed(ActionEvent e) {
                setTableHeaderColumnRaito();
            }
        }), BorderLayout.EAST);
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBorder(BorderFactory.createTitledBorder("JTableHeader column width ratio"));
        panel.add(p);
        panel.add(check);
        return panel;
    }

    private void setTableHeaderColumnRaito() {
        TableColumnModel m = table.getColumnModel();
        List<Integer> list = getWidthRaitoArray(columnNames.length);
        //System.out.println("a: "+m.getTotalColumnWidth());
        //System.out.println("b: "+table.getSize().width);
        int total = table.getSize().width; //m.getTotalColumnWidth();
        double raito = total / (double) getRaitoTotal(list);
        for (int i = 0; i < m.getColumnCount() - 1; i++) {
            TableColumn col = m.getColumn(i);
            int colwidth = (int) (.5 + list.get(i) * raito);
            //col.setMaxWidth(colwidth);
            col.setPreferredWidth(colwidth);
            total -= colwidth;
        }
        //m.getColumn(m.getColumnCount() - 1).setMaxWidth(total);
        m.getColumn(m.getColumnCount() - 1).setPreferredWidth(total);
        table.revalidate();
    }

    private static int getRaitoTotal(List<Integer> list) {
//         int w = 0;
//         for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
//             w += list.get(i);
//         }
//         return w;
        return list.stream().mapToInt(i -> i).sum();
    }

    private List<Integer> getWidthRaitoArray(int length) {
        try {
            Stream<Integer> a = Arrays.stream(field.getText().split(":")).map(String::trim).filter(s -> !s.isEmpty()).map(Integer::valueOf);
            Stream<Integer> b = Stream.generate(() -> 1).limit(length);
            return Stream.concat(a, b).limit(length).collect(Collectors.toList());
        } catch (NumberFormatException ex) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(getRootPane(), "invalid value.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return Stream.generate(() -> 1).limit(length).collect(Collectors.toList());
        }
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
