package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private final JSlider slider = new JSlider(0, 100, 50);
    private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(500, 0, 1000, 10));

    public MainPanel() {
        super(new GridLayout(2, 1));

        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int intValue = (int) source.getValue() * 10;
            spinner.setValue(intValue);
        });
        slider.addMouseWheelListener(e -> {
            JSlider source = (JSlider) e.getComponent();
            int intValue = (int) source.getValue() - e.getWheelRotation();
            BoundedRangeModel model = source.getModel();
            if (model.getMaximum() >= intValue && model.getMinimum() <= intValue) {
                slider.setValue(intValue);
            }
        });

        spinner.addChangeListener(e -> {
            JSpinner source = (JSpinner) e.getSource();
            Integer newValue = (Integer) source.getValue();
            slider.setValue((int) newValue.intValue() / 10);
        });
        spinner.addMouseWheelListener(e -> {
            JSpinner source = (JSpinner) e.getComponent();
            SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
            Integer oldValue = (Integer) source.getValue();
            int intValue = oldValue.intValue() - e.getWheelRotation() * model.getStepSize().intValue();
            int max = ((Integer) model.getMaximum()).intValue(); //1000
            int min = ((Integer) model.getMinimum()).intValue(); //0
            if (min <= intValue && intValue <= max) {
                source.setValue(intValue);
            }
        });

        add(makeTitlePanel(spinner, "MouseWheel+JSpinner"));
        add(makeTitlePanel(slider,  "MouseWheel+JSlider"));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1d;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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
