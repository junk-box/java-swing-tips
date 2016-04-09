package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private static final String LF = "\n";
    private MainPanel() {
        super(new BorderLayout());
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 100; i++) {
            buf.append(i + LF);
        }

        final JScrollPane scrollPane = new JScrollPane(new JTextArea(buf.toString()));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(scrollPane.getVerticalScrollBar().getUnitIncrement(1), 1, 100000, 1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#####0"));
        spinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner) e.getSource();
                scrollPane.getVerticalScrollBar().setUnitIncrement((Integer) s.getValue());
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Unit Increment:"));
        box.add(Box.createHorizontalStrut(2));
        box.add(spinner);
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(box, BorderLayout.NORTH);
        add(scrollPane);
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
