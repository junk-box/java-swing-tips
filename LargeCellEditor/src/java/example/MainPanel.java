package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        ListModel<IconItem> list = makeIconList();
        TableModel model = makeIconTableModel(list);
        JTable table = new IconTable(model, list);
        JPanel p = new JPanel(new GridBagLayout());
        p.add(table, new GridBagConstraints());
        p.setBackground(Color.WHITE);
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }
    private ListModel<IconItem> makeIconList() {
        DefaultListModel<IconItem> list = new DefaultListModel<>();
        list.addElement(new IconItem("wi0009"));
        list.addElement(new IconItem("wi0054"));
        list.addElement(new IconItem("wi0062"));
        list.addElement(new IconItem("wi0063"));
        list.addElement(new IconItem("wi0064"));
        list.addElement(new IconItem("wi0096"));
        list.addElement(new IconItem("wi0111"));
        list.addElement(new IconItem("wi0122"));
        list.addElement(new IconItem("wi0124"));
        return list;
    }
    private static TableModel makeIconTableModel(ListModel<?> list) {
        Object[][] data = {
            {list.getElementAt(0), list.getElementAt(1), list.getElementAt(2)},
            {list.getElementAt(3), list.getElementAt(4), list.getElementAt(5)},
            {list.getElementAt(6), list.getElementAt(7), list.getElementAt(8)}
        };
        return new DefaultTableModel(data, null) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override public int getColumnCount() {
                return 3;
            }
        };
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
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class IconItem {
    public final ImageIcon large;
    public final ImageIcon small;
    protected IconItem(String str) {
        large = new ImageIcon(getClass().getResource(str + "-48.png"));
        small = new ImageIcon(getClass().getResource(str + "-24.png"));
    }
}

class IconTableCellRenderer extends DefaultTableCellRenderer {
    @Override public void updateUI() {
        super.updateUI();
        setHorizontalAlignment(SwingConstants.CENTER);
        //setOpaque(true);
        //setBorder(BorderFactory.createEmptyBorder());
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setIcon(((IconItem) value).large);
        return this;
    }
}

class IconTable extends JTable {
    private static final int XOFF = 4;
    private final JPanel panel = new TableGlassPane() {
        @Override protected void paintComponent(Graphics g) {
            g.setColor(new Color(0x64FFFFFF, true));
            g.fillRect(0, 0, getWidth(), getHeight());
            BufferedImage bufimg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufimg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .15f));
            g2.setPaint(Color.BLACK);
            for (int i = 0; i < XOFF; i++) {
                g2.fillRoundRect(rect.x - i, rect.y + XOFF, rect.width + i + i, rect.height - XOFF + i, 5, 5);
            }
            g2.dispose();
            g.drawImage(bufimg, 0, 0, null);
        }
    };
    private final EditorFromList editor;
    private Rectangle rect;

    protected IconTable(TableModel model, ListModel<IconItem> list) {
        super(model);
        setDefaultRenderer(Object.class, new IconTableCellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        initCellSize(50);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                startEditing();
            }
        });

        editor = new EditorFromList(list);
        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelEditing();
                }
            }
        });
        editor.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                setEditorSelectedIconAt(e.getPoint());
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (Objects.isNull(rect) || rect.contains(e.getPoint())) {
                    return;
                }
                setEditorSelectedIconAt(e.getPoint());
            }
        });
        panel.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
            @Override public boolean accept(Component c) {
                return Objects.equals(c, editor);
            }
        });
        panel.add(editor);
        panel.setVisible(false);
    }
    private void initCellSize(int size) {
        setRowHeight(size);
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        TableColumnModel m = getColumnModel();
        for (int i = 0; i < m.getColumnCount(); i++) {
            TableColumn col = m.getColumn(i);
            col.setMinWidth(size);
            col.setMaxWidth(size);
        }
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    public void startEditing() {
        //JFrame f = (JFrame) getTopLevelAncestor();
        //f.setGlassPane(panel);
        getRootPane().setGlassPane(panel);
        Dimension dim = editor.getPreferredSize();
        rect = getCellRect(getSelectedRow(), getSelectedColumn(), true);
        int iv = (dim.width - rect.width) / 2;
        Point p = SwingUtilities.convertPoint(this, rect.getLocation(), panel);
        rect.setRect(p.x - iv, p.y - iv, dim.width, dim.height);
        editor.setBounds(rect);
        editor.setSelectedValue(getValueAt(getSelectedRow(), getSelectedColumn()), true);
        panel.setVisible(true);
        editor.requestFocusInWindow();
    }
    private void cancelEditing() {
        panel.setVisible(false);
    }
    private void setEditorSelectedIconAt(Point p) {
        Object o = editor.getModel().getElementAt(editor.locationToIndex(p));
        if (Objects.nonNull(o)) {
            setValueAt(o, getSelectedRow(), getSelectedColumn());
        }
        panel.setVisible(false);
    }
}

class EditorFromList extends JList<IconItem> {
    private static final int INS = 2;
    private transient RollOverListener handler;
    private int rollOverRowIndex = -1;
    private final Dimension dim;

    protected EditorFromList(ListModel<IconItem> model) {
        super(model);
        ImageIcon icon = model.getElementAt(0).small;
        int iw = INS + icon.getIconWidth();
        int ih = INS + icon.getIconHeight();

        dim = new Dimension(iw * 3 + INS, ih * 3 + INS);
        setFixedCellWidth(iw);
        setFixedCellHeight(ih);
    }

    @Override public Dimension getPreferredSize() {
        return dim;
    }

    @Override public void updateUI() {
        removeMouseMotionListener(handler);
        removeMouseListener(handler);
        super.updateUI();
        handler = new RollOverListener();
        addMouseMotionListener(handler);
        addMouseListener(handler);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(0);
        setCellRenderer(new ListCellRenderer<IconItem>() {
            private final JLabel label = new JLabel();
            private final Color selctedColor = new Color(0xC8C8FF);
            @Override public Component getListCellRendererComponent(JList<? extends IconItem> list, IconItem value, int index, boolean isSelected, boolean cellHasFocus) {
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                if (index == rollOverRowIndex) {
                    label.setBackground(getSelectionBackground());
                } else if (isSelected) {
                    label.setBackground(selctedColor);
                } else {
                    label.setBackground(getBackground());
                }
                label.setIcon(value.small);
                return label;
            }
        });
    }

    private class RollOverListener extends MouseAdapter {
        @Override public void mouseExited(MouseEvent e) {
            rollOverRowIndex = -1;
            repaint();
        }
        @Override public void mouseMoved(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if (row != rollOverRowIndex) {
                rollOverRowIndex = row;
                repaint();
            }
        }
    }
}

class TableGlassPane extends JPanel {
    protected TableGlassPane() {
        super((LayoutManager) null);
        setOpaque(false);
    }
    @Override public void setVisible(boolean flag) {
        super.setVisible(flag);
        setFocusTraversalPolicyProvider(flag);
        setFocusCycleRoot(flag);
    }
}
