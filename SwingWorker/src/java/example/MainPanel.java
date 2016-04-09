package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.Timer;

public final class MainPanel extends JPanel {
    private final JTextArea area     = new JTextArea();
    private final JPanel statusPanel = new JPanel(new BorderLayout());
    private final JButton runButton  = new JButton(new RunAction());
    private final JButton canButton  = new JButton(new CancelAction());
    private final JProgressBar bar   = new JProgressBar(0, 100);
    private final AnimatedLabel anil = new AnimatedLabel();
    private SwingWorker<String, String> worker;

    public MainPanel() {
        super(new BorderLayout(5, 5));
        area.setEditable(false);
        area.setLineWrap(true);
        Box box = Box.createHorizontalBox();
        box.add(anil);
        box.add(Box.createHorizontalGlue());
        box.add(runButton);
        box.add(Box.createHorizontalStrut(2));
        box.add(canButton);
        add(new JScrollPane(area));
        add(box, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
        statusPanel.add(bar);
        statusPanel.setVisible(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }

    class UIUpdateTask extends Task {
        @Override protected void process(List<String> chunks) {
            //System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
            if (isCancelled()) {
                return;
            }
            if (!isDisplayable()) {
                System.out.println("process: DISPOSE_ON_CLOSE");
                cancel(true);
                return;
            }
            for (String message: chunks) {
                if (!isCancelled()) {
                    appendText(message);
                }
            }
        }
        @Override public void done() {
            System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
            if (!isDisplayable()) {
                System.out.println("done: DISPOSE_ON_CLOSE");
                cancel(true);
                return;
            }
            anil.stopAnimation();
            runButton.setEnabled(true);
            canButton.setEnabled(false);
            statusPanel.setVisible(false);
            try {
                if (isCancelled()) {
                    appendText("\nCancelled\n");
                } else {
                    appendText("\n" + get() + "\n");
                }
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                appendText("\nException\n");
            }
        }
    }

    class RunAction extends AbstractAction {
        protected RunAction() {
            super("run");
        }
        @Override public void actionPerformed(ActionEvent e) {
            System.out.println("actionPerformed() is EDT?: " + EventQueue.isDispatchThread());
            runButton.setEnabled(false);
            canButton.setEnabled(true);
            anil.startAnimation();
            statusPanel.setVisible(true);
            bar.setIndeterminate(true);
            worker = new UIUpdateTask();
            worker.addPropertyChangeListener(new ProgressListener(bar));
            worker.execute();
        }
    }

    class CancelAction extends AbstractAction {
        protected CancelAction() {
            super("cancel");
        }
        @Override public void actionPerformed(ActionEvent e) {
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
            }
        }
    }

    private void appendText(String str) {
        area.append(str);
        area.setCaretPosition(area.getDocument().getLength());
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
    @Override public String doInBackground() {
        System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            return "Interrupted";
        }
        int current = 0;
        int lengthOfTask = 120; //list.size();
        publish("Length Of Task: " + lengthOfTask);
        publish("\n------------------------------\n");

        while (current < lengthOfTask && !isCancelled()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                //return "Interrupted";
                break;
            }
            setProgress(100 * current / lengthOfTask);
            publish(".");
            current++;
        }
        return "Done";
    }
}

class ProgressListener implements PropertyChangeListener {
    private final JProgressBar progressBar;
    protected ProgressListener(JProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setValue(0);
    }
    @Override public void propertyChange(PropertyChangeEvent e) {
        if (!progressBar.isDisplayable() && e.getSource() instanceof SwingWorker) {
            System.out.println("progress: DISPOSE_ON_CLOSE");
            ((SwingWorker) e.getSource()).cancel(true);
        }
        String strPropertyName = e.getPropertyName();
        if ("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer) e.getNewValue();
            progressBar.setValue(progress);
        }
    }
}

class AnimatedLabel extends JLabel implements ActionListener, HierarchyListener {
    private final Timer animator;
    private final transient AnimeIcon icon = new AnimeIcon();
    protected AnimatedLabel() {
        super();
        animator = new Timer(100, this);
        setIcon(icon);
        addHierarchyListener(this);
    }
    @Override public void actionPerformed(ActionEvent e) {
        icon.next();
        repaint();
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
            animator.stop();
        }
    }
    public void startAnimation() {
        icon.setRunning(true);
        animator.start();
    }
    public void stopAnimation() {
        icon.setRunning(false);
        animator.stop();
    }
}

class AnimeIcon implements Icon {
    private static final Color ELLIPSE_COLOR = new Color(.5f, .5f, .5f);
    private static final double R  = 2d;
    private static final double SX = 1d;
    private static final double SY = 1d;
    private static final int WIDTH  = (int) (R * 8 + SX * 2);
    private static final int HEIGHT = (int) (R * 8 + SY * 2);
    private final List<Shape> list = new ArrayList<Shape>(Arrays.asList(
        new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));

    private boolean isRunning;
    public void next() {
        if (isRunning) {
            list.add(list.remove(0));
        }
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(Objects.nonNull(c) ? c.getBackground() : Color.WHITE);
        g2.fillRect(0, 0, getIconWidth(), getIconHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(ELLIPSE_COLOR);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            float alpha = isRunning ? (i + 1) / (float) size : .5f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.fill(list.get(i));
        }
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return WIDTH;
    }
    @Override public int getIconHeight() {
        return HEIGHT;
    }
}
