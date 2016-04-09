package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextArea area    = new JTextArea();
    private final JButton runButton = new JButton(new RunAction());
    private final JSpinner millisToDecideToPopup;
    private final JSpinner millisToPopup;
    private transient SwingWorker<String, String> worker;
    private transient ProgressMonitor monitor;

    public MainPanel() {
        super(new BorderLayout(5, 5));
        area.setEditable(false);

        monitor = new ProgressMonitor(null, "message", "note", 0, 100);
        millisToDecideToPopup = makeSpinner(monitor.getMillisToDecideToPopup(), 0, 5 * 1000, 100);
        millisToPopup = makeSpinner(monitor.getMillisToPopup(), 0, 5 * 1000, 100);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.anchor  = GridBagConstraints.LINE_END;
        p.add(new JLabel("MillisToDecideToPopup:"), c);
        p.add(new JLabel("MillisToPopup:"), c);
        c.gridx   = 1;
        c.weightx = 1d;
        c.fill    = GridBagConstraints.HORIZONTAL;
        p.add(millisToDecideToPopup, c);
        p.add(millisToPopup, c);

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(runButton);

        add(new JScrollPane(area));
        add(p, BorderLayout.NORTH);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    class RunAction extends AbstractAction {
        protected RunAction() {
            super("run");
        }
        @Override public void actionPerformed(ActionEvent e) {
            Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
            int toDecideToPopup = (int) millisToDecideToPopup.getValue();
            int toPopup         = (int) millisToPopup.getValue();
            int lengthOfTask    = Math.max(10000, toDecideToPopup * 5);
            monitor = new ProgressMonitor(w, "message", "note", 0, 100);
            monitor.setMillisToDecideToPopup(toDecideToPopup);
            monitor.setMillisToPopup(toPopup);

            //System.out.println(monitor.getMillisToDecideToPopup());
            //System.out.println(monitor.getMillisToPopup());

            runButton.setEnabled(false);
            worker = new Task(lengthOfTask) {
                @Override protected void process(List<String> chunks) {
                    if (isCancelled()) {
                        return;
                    }
                    if (!isDisplayable()) {
                        System.out.println("process: DISPOSE_ON_CLOSE");
                        cancel(true);
                        return;
                    }
                    for (String message: chunks) {
                        monitor.setNote(message);
                    }
                }
                @Override public void done() {
                    if (!isDisplayable()) {
                        System.out.println("done: DISPOSE_ON_CLOSE");
                        cancel(true);
                        return;
                    }
                    runButton.setEnabled(true);
                    monitor.close();
                    String text = null;
                    if (isCancelled()) {
                        text = "Cancelled";
                    } else {
                        try {
                            text = get();
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                            text = "Exception";
                        }
                    }
                    area.append(text + "\n");
                    area.setCaretPosition(area.getDocument().getLength());
                }
            };
            worker.addPropertyChangeListener(new ProgressListener(monitor));
            worker.execute();
        }
    }
    private static JSpinner makeSpinner(int num, int min, int max, int step) {
        return new JSpinner(new SpinnerNumberModel(num, min, max, step));
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
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Task extends SwingWorker<String, String> {
    private final int lengthOfTask;
    protected Task(int lengthOfTask) {
        super();
        this.lengthOfTask = lengthOfTask;
    }
    @Override public String doInBackground() {
        int current = 0;
        while (current < lengthOfTask && !isCancelled()) {
            if (current % 10 == 0) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ie) {
                    return "Interrupted";
                }
            }
            int v = 100 * current / lengthOfTask;
            setProgress(v);
            publish(String.format("%d%%", v));
            current++;
        }
        return "Done";
    }
}

class ProgressListener implements PropertyChangeListener {
    private final ProgressMonitor monitor;
    protected ProgressListener(ProgressMonitor monitor) {
        this.monitor = monitor;
        this.monitor.setProgress(0);
    }
    @Override public void propertyChange(PropertyChangeEvent e) {
        Object o = e.getSource();
        String strPropertyName = e.getPropertyName();
        if ("progress".equals(strPropertyName) && o instanceof SwingWorker) {
            SwingWorker task = (SwingWorker) o;
            monitor.setProgress((Integer) e.getNewValue());
            if (monitor.isCanceled() || task.isDone()) {
                task.cancel(true);
            }
        }
    }
}
