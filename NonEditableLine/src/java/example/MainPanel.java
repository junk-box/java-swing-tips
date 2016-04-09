package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final transient Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.GRAY);

    private MainPanel() {
        super(new BorderLayout());
        JTextArea textArea = new JTextArea();
        textArea.setText("aaaaaaaasdfasdfasdfasdf\nasdfasdfasdfasdfasdfasdf\n1234567890\naaaaaaaaaaaaaaaaaasdfasd");
        ((AbstractDocument) textArea.getDocument()).setDocumentFilter(new NonEditableLineDocumentFilter());
        try {
            Highlighter hilite = textArea.getHighlighter();
            Document doc = textArea.getDocument();
            Element root = doc.getDefaultRootElement();
            for (int i = 0; i < 2; i++) { //root.getElementCount(); i++) {
                Element elem = root.getElement(i);
                hilite.addHighlight(elem.getStartOffset(), elem.getEndOffset() - 1, highlightPainter);
            }
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        add(new JScrollPane(textArea));
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
class NonEditableLineDocumentFilter extends DocumentFilter {
    @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (Objects.nonNull(string)) {
            replace(fb, offset, 0, string, attr);
        }
    }
    @Override public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
        replace(fb, offset, length, "", null);
    }
    @Override public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        Document doc = fb.getDocument();
        if (doc.getDefaultRootElement().getElementIndex(offset) < 2) {
            return;
        }
        fb.replace(offset, length, text, attrs);
    }
}
