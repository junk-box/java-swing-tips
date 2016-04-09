package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.ParserDelegator;

public final class MainPanel extends JPanel {
    private final transient Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    private final JTextArea textArea = new JTextArea();
    private final JEditorPane editorPane = new JEditorPane();
    private final JTextField field = new JTextField("3");
    private final Action elementIDAction = new AbstractAction("Element#getElement(id)") {
        @Override public void actionPerformed(ActionEvent e) {
            textArea.append(String.format("----%n%s%n", getValue(Action.NAME)));
            final String id = field.getText().trim();
            HTMLDocument doc = (HTMLDocument) editorPane.getDocument();
            Element element = doc.getElement(id);
            if (Objects.nonNull(element)) {
                textArea.append(String.format("found: %s%n", element));
                editorPane.requestFocusInWindow();
                editorPane.select(element.getStartOffset(), element.getEndOffset());
            }
        }
    };
    private final Action highlightAction = new AbstractAction("Highlight Element[@id]") {
        @Override public void actionPerformed(ActionEvent e) {
            textArea.append(String.format("----%n%s%n", getValue(Action.NAME)));
            JToggleButton b = (JToggleButton) e.getSource();
            if (b.isSelected()) {
                for (Element root: editorPane.getDocument().getRootElements()) {
                    traverseElementById(root);
                }
            } else {
                Highlighter highlighter = editorPane.getHighlighter();
                highlighter.removeAllHighlights();
            }
        }
    };
    private final Action parserAction = new AbstractAction("ParserDelegator") {
        @Override public void actionPerformed(ActionEvent e) {
            textArea.append(String.format("----%n%s%n", getValue(Action.NAME)));
            final String id = field.getText().trim();
            final String text = editorPane.getText();
            ParserDelegator delegator = new ParserDelegator();
            try {
                delegator.parse(new StringReader(text), new HTMLEditorKit.ParserCallback() {
                    @Override public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
                        Object attrid = a.getAttribute(HTML.Attribute.ID);
                        textArea.append(String.format("%s@id=%s%n", tag, attrid));
                        if (id.equals(attrid)) {
                            textArea.append(String.format("found: pos=%d%n", pos));
                            int endoffs = text.indexOf('>', pos);
                            textArea.append(String.format("%s%n", text.substring(pos, endoffs + 1)));
                        }
                    }
                }, Boolean.TRUE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        editorPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        editorPane.setText("<html>12<span id='2'>345678</span>90<p>1<a href='..'>23</a>45<span class='insert' id='0'>6</span>7<span id='1'>8</span>90<div class='fff' id='3'>123</div>4567890</p></html>");
        DefaultHighlighter dh = (DefaultHighlighter) editorPane.getHighlighter();
        dh.setDrawsLayeredHighlights(false);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setTopComponent(new JScrollPane(editorPane));
        sp.setBottomComponent(new JScrollPane(textArea));

        JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(field);
        p.add(new JButton(elementIDAction));
        p.add(new JToggleButton(highlightAction));
        p.add(new JButton(parserAction));
        add(sp);
        add(p, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private void addHighlight(Element element, boolean isBlock) {
        Highlighter highlighter = editorPane.getHighlighter();
        int start = element.getStartOffset();
        int lf    = isBlock ? 1 : 0;
        int end   = element.getEndOffset() - lf; //lf???, setDrawsLayeredHighlights(false) bug???
        try {
            highlighter.addHighlight(start, end, highlightPainter);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }
    private void traverseElementById(Element element) {
        if (element.isLeaf()) {
            checkID(element);
        } else {
            for (int i = 0; i < element.getElementCount(); i++) {
                Element child = element.getElement(i);
                checkID(child);
                if (!child.isLeaf()) {
                    traverseElementById(child);
                }
            }
        }
    }
    private void checkID(Element element) {
        AttributeSet attrs = element.getAttributes();
        Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
        Object name = Objects.isNull(elementName) ? attrs.getAttribute(StyleConstants.NameAttribute) : null;
        HTML.Tag tag;
        if (name instanceof HTML.Tag) {
            tag = (HTML.Tag) name;
        } else {
            return;
        }
        textArea.append(String.format("%s%n", tag));
        if (tag.isBlock()) { //block
            Object bid = attrs.getAttribute(HTML.Attribute.ID);
            if (Objects.nonNull(bid)) {
                textArea.append(String.format("block: id=%s%n", bid));
                addHighlight(element, true);
            }
        } else { //inline
            Enumeration<?> e = attrs.getAttributeNames();
            while (e.hasMoreElements()) {
                Object obj = attrs.getAttribute(e.nextElement());
                //System.out.println("AttributeNames: " + obj);
                if (obj instanceof AttributeSet) {
                    AttributeSet a = (AttributeSet) obj;
                    Object iid = a.getAttribute(HTML.Attribute.ID);
                    if (Objects.nonNull(iid)) {
                        textArea.append(String.format("inline: id=%s%n", iid));
                        addHighlight(element, false);
                    }
                }
            }
        }
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
