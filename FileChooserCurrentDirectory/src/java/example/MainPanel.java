package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextArea log = new JTextArea();
    private final JTextField field = new JTextField(24);
    private final JCheckBox check1 = new JCheckBox("Change !dir.exists() case");
    private final JCheckBox check2 = new JCheckBox("isParent reset?");
    private final JPanel p = new JPanel(new GridBagLayout());
    private final JFileChooser fc0 = new JFileChooser();
    private final JFileChooser fc1 = new JFileChooser();
    private final JFileChooser fc2 = new JFileChooser() {
        @Override public void setCurrentDirectory(File dir) {
            if (Objects.nonNull(dir) && !dir.exists()) {
                this.setCurrentDirectory(dir.getParentFile());
            }
            super.setCurrentDirectory(dir);
        }
    };
    @Override public void updateUI() {
        super.updateUI();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                SwingUtilities.updateComponentTreeUI(fc0);
                SwingUtilities.updateComponentTreeUI(fc1);
                SwingUtilities.updateComponentTreeUI(fc2);
            }
        });
    }
    public MainPanel() {
        super(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("JFileChooser.DIRECTORIES_ONLY"));
        fc0.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        try {
            field.setText(new File(".").getCanonicalPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 0, 0);

        c.gridwidth = 2;
        p.add(field, c);

        c.gridwidth = 1;
        c.gridy = 1;
        p.add(new JButton(new AbstractAction("setCurrentDirectory") {
            @Override public void actionPerformed(ActionEvent e) {
                File f = new File(field.getText().trim());
                JFileChooser fc = check1.isSelected() ? fc2 : fc0;
                fc.setCurrentDirectory(f);
                int retvalue = fc.showOpenDialog(p);
                if (retvalue == JFileChooser.APPROVE_OPTION) {
                    log.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        }), c);
        p.add(check1, c);

        c.gridy = 2;
        p.add(new JButton(new AbstractAction("setSelectedFile") {
            @Override public void actionPerformed(ActionEvent e) {
                File f = new File(field.getText().trim());
                JFileChooser fc = fc1;
                System.out.format("isAbsolute: %s, isParent: %s%n",
                                  f.isAbsolute(),
                                  !fc.getFileSystemView().isParent(fc.getCurrentDirectory(), f));
                fc.setSelectedFile(f);
                int retvalue = fc.showOpenDialog(p);
                if (retvalue == JFileChooser.APPROVE_OPTION) {
                    log.setText(fc.getSelectedFile().getAbsolutePath());
                }
                if (check2.isSelected()) {
                    fc.setSelectedFile(f.getParentFile()); //XXX: reset???
                }
            }
        }), c);
        p.add(check2, c);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(log));
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
            //UIManager.put("FileChooser.readOnly", Boolean.TRUE);
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
