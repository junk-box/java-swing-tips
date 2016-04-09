package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.*;
//import sun.awt.shell.ShellFolder;

public final class MainPanel extends JPanel {
    private final JLabel smallLabel = new JLabel() {
        @Override public Dimension getPreferredSize() {
            return new Dimension(16 + 1, 16 + 1);
        }
        @Override public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    };
    private final JLabel largeLabel = new JLabel() {
        @Override public Dimension getPreferredSize() {
            return new Dimension(32 + 1, 32 + 1);
        }
        @Override public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    };
    public MainPanel() {
        super();
        smallLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        smallLabel.setAlignmentY(BOTTOM_ALIGNMENT);

        largeLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        largeLabel.setAlignmentY(BOTTOM_ALIGNMENT);

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createTitledBorder("drop File"));
        box.add(smallLabel);
        box.add(Box.createHorizontalStrut(5));
        box.add(largeLabel);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(box);
        add(new JLabel("<html>warning: ShellFolder is internal proprietary API"
                     + "<br> and may be removed in a future release"));

        DropTargetListener dtl = new DropTargetAdapter() {
            @Override public void dragOver(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    return;
                }
                dtde.rejectDrag();
            }
            @Override public void drop(DropTargetDropEvent dtde) {
                try {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Transferable t = dtde.getTransferable();
                        List list = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
                        Object o = list.get(0);
                        if (o instanceof File) {
                            File file = (File) o;
                            smallLabel.setIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
                            largeLabel.setIcon(new ImageIcon(
                                sun.awt.shell.ShellFolder.getShellFolder(file).getIcon(true)));
                        }
                        dtde.dropComplete(true);
                        return;
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
                dtde.rejectDrop();
            }
        };
        setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, dtl, true));
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
