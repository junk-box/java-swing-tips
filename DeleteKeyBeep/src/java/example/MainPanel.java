package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2, 1));
        JTextField tf = new JTextField(12);

        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new SizeFilter());
        //((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentSizeFilter(5));

        ActionMap am = tf.getActionMap();

        String key = DefaultEditorKit.deletePrevCharAction; //"delete-previous";
        am.put(key, new SilentDeleteTextAction(key, am.get(key)));

        key = DefaultEditorKit.deleteNextCharAction; //"delete-next";
        am.put(key, new SilentDeleteTextAction(key, am.get(key)));

        add(makeTitlePanel(new JTextField(), "Defalut"));
        add(makeTitlePanel(tf, "Override delete-previous, delete-next beep"));
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

class SilentDeleteTextAction extends TextAction {
    private final Action deleteAction;
    protected SilentDeleteTextAction(String name, Action deleteAction) {
        super(name);
        this.deleteAction = deleteAction;
    }
    @Override public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (Objects.nonNull(target) && target.isEditable()) {
            Caret caret = target.getCaret();
            int dot  = caret.getDot();
            int mark = caret.getMark();
            if (DefaultEditorKit.deletePrevCharAction.equals(getValue(Action.NAME))) {
                //@see javax/swing/text/DefaultEditorKit.java DeletePrevCharAction
                if (dot == 0 && mark == 0) {
                    return;
                }
            } else {
                //@see javax/swing/text/DefaultEditorKit.java DeleteNextCharAction
                Document doc = target.getDocument();
                if (dot == mark && doc.getLength() == dot) {
                    return;
                }
            }
        }
        deleteAction.actionPerformed(e);
    }
}

class SizeFilter extends DocumentFilter {
    private static final int MAX = 5;
    @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        int len = fb.getDocument().getLength();
        if (len + string.length() > MAX) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        fb.insertString(offset, string, attr);
    }
    @Override public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
        fb.remove(offset, length);
    }
    @Override public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        int len = fb.getDocument().getLength();
        if (len - length + text.length() > MAX) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        fb.replace(offset, length, text, attrs);
    }
}

// //http://docs.oracle.com/javase/tutorial/uiswing/components/generaltext.html
// //Text Component Features (The Java? Tutorials > Creating a GUI With JFC/Swing > Using Swing Components)
// //http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TextComponentDemoProject/src/components/DocumentSizeFilter.java
// //DocumentSizeFilter.java]
// class DocumentSizeFilter extends DocumentFilter {
//     int maxCharacters;
//     protected DocumentSizeFilter(int maxChars) {
//         maxCharacters = maxChars;
//     }
//     @Override public void insertString(DocumentFilter.FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
//         //This rejects the entire insertion if it would make
//         //the contents too long. Another option would be
//         //to truncate the inserted string so the contents
//         //would be exactly maxCharacters in length.
//         if ((fb.getDocument().getLength() + str.length()) <= maxCharacters) {
//             super.insertString(fb, offs, str, a);
//         } else {
//             Toolkit.getDefaultToolkit().beep();
//         }
//     }
//     @Override public void replace(DocumentFilter.FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException {
//         //This rejects the entire replacement if it would make
//         //the contents too long. Another option would be
//         //to truncate the replacement string so the contents
//         //would be exactly maxCharacters in length.
//         if ((fb.getDocument().getLength() + str.length() - length) <= maxCharacters) {
//             super.replace(fb, offs, length, str, a);
//         } else {
//             Toolkit.getDefaultToolkit().beep();
//         }
//     }
// }
