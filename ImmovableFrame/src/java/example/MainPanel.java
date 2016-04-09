package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.plaf.basic.*;

// How to Use Internal Frames
// http://docs.oracle.com/javase/tutorial/uiswing/components/internalframe.html
// https://community.oracle.com/thread/1392111 Lock JInternalPane
public final class MainPanel extends JPanel {
    private static final int XOFFSET = 30;
    private static final int YOFFSET = 30;
    private static AtomicInteger openFrameCount = new AtomicInteger();
    private final JDesktopPane desktop;
    private final JInternalFrame immovableFrame;
    public MainPanel() {
        super(new BorderLayout());
        desktop = new JDesktopPane();
        //title, resizable, closable, maximizable, iconifiable
        immovableFrame = new JInternalFrame("immovable", false, false, true, true);
        Component north = ((BasicInternalFrameUI) immovableFrame.getUI()).getNorthPane();
        MouseMotionListener[] actions = (MouseMotionListener[]) north.getListeners(MouseMotionListener.class);
        for (int i = 0; i < actions.length; i++) {
            north.removeMouseMotionListener(actions[i]);
        }
        //immovableFrame.setLocation(0, 0);
        immovableFrame.setSize(160, 0);
        desktop.add(immovableFrame);
        immovableFrame.setVisible(true);

        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                //System.out.println(e.toString());
                immovableFrame.setSize(immovableFrame.getSize().width, desktop.getSize().height);
            }
        });

        add(desktop);
        add(createMenuBar(), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Window");
        menu.setMnemonic(KeyEvent.VK_W);
        menuBar.add(menu);
        JMenuItem menuItem = new JMenuItem("New");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
        menuItem.setActionCommand("new");
        menuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JInternalFrame frame = createInternalFrame();
                desktop.add(frame);
                frame.setVisible(true);
                //desktop.getDesktopManager().activateFrame(frame);
            }
        });
        menu.add(menuItem);
        return menuBar;
    }

    private static JInternalFrame createInternalFrame() {
        JInternalFrame f = new JInternalFrame(String.format("Document #%s", openFrameCount.getAndIncrement()), true, true, true, true);
        f.setSize(160, 100);
        f.setLocation(XOFFSET * openFrameCount.intValue(), YOFFSET * openFrameCount.intValue());
        return f;
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
