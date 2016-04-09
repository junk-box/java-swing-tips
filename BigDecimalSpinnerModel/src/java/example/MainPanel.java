package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.math.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        Box box = Box.createVerticalBox();
        box.add(new JLabel("SpinnerNumberModel(double, ...)"));
        box.add(Box.createVerticalStrut(2));
        JPanel p1 = new JPanel(new GridLayout(1, 2, 5, 5));
        p1.add(new JSpinner(new SpinnerNumberModel(2.01, 2.00, 3.02, .01)));
        p1.add(new JSpinner(new SpinnerNumberModel(29.7, 29.6, 30.2, .1)));
        box.add(p1);
        box.add(Box.createVerticalStrut(5));
        box.add(new JLabel("BigDecimalSpinnerModel"));
        box.add(Box.createVerticalStrut(2));
        JPanel p2 = new JPanel(new GridLayout(1, 2, 5, 5));
        p2.add(new JSpinner(new BigDecimalSpinnerModel(2.01, 2.00, 3.02, .01)));
        p2.add(new JSpinner(new BigDecimalSpinnerModel(29.7, 29.6, 30.2, .1)));
        box.add(p2);
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //TEST:
        StringBuilder sb = new StringBuilder();
        double d = 29.7 - 29.6 - .1;
        sb.append(String.format("%f-%f-%f>=0:%b%n", 29.7, 29.6, .1, d >= 0))
          .append(String.format("abs(%f-%f-%f)<1.0e-14:%b%n", 29.7, 29.6, .1, Math.abs(d) < 1.0e-14))
          .append(String.format("abs(%f-%f-%f)<1.0e-15:%b%n", 29.7, 29.6, .1, Math.abs(d) < 1.0e-15));

        add(box, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea(sb.toString())));
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

class BigDecimalSpinnerModel extends SpinnerNumberModel {
    protected BigDecimalSpinnerModel(double value, double minimum, double maximum, double stepSize) {
        super(value, minimum, maximum, stepSize);
    }
    @Override public Object getPreviousValue() {
        return incrValue(-1);
    }
    @Override public Object getNextValue() {
        return incrValue(+1);
    }
    private Number incrValue(int dir) {
        Number v = getNumber();
        BigDecimal value    = new BigDecimal(v.toString());
        BigDecimal stepSize = new BigDecimal(getStepSize().toString());

        BigDecimal newValue = dir > 0 ? value.add(stepSize) : value.subtract(stepSize);

        BigDecimal maximum = new BigDecimal(getMaximum().toString());
        if (maximum.compareTo(newValue) < 0) {
            return null;
        }

        BigDecimal minimum = new BigDecimal(getMinimum().toString());
        if (minimum.compareTo(newValue) > 0) {
            return null;
        }
        return newValue;
    }
}
