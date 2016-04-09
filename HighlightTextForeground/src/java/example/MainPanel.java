package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final String INIT_TXT =
        "Trail: Creating a GUI with JFC/Swing\n"
      + "http://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n"
      + "Lesson: Learning Swing by Example\n"
      + "This lesson explains the concepts you need to\n"
      + " use Swing components in building a user interface.\n"
      + " First we examine the simplest Swing application you can write.\n"
      + " Then we present several progressively complicated examples of creating\n"
      + " user interfaces using components in the javax.swing package.\n"
      + " We cover several Swing components, such as buttons, labels, and text areas.\n"
      + " The handling of events is also discussed,\n"
      + " as are layout management and accessibility.\n"
      + " This lesson ends with a set of questions and exercises\n"
      + " so you can test yourself on what you've learned.\n"
      + "http://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n";

    private static final Color WARNING_COLOR = new Color(255, 200, 200);
    private final transient Highlighter.HighlightPainter currentPainter   = new DefaultHighlighter.DefaultHighlightPainter(new Color(0xAA006400, true));
    private final transient Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(0x64DDDD00, true));
    private final JTextPane textPane  = new JTextPane();
    private final JTextField field    = new JTextField("Swing");
    private final JButton prevButton  = new JButton("\u22C0");
    private final JButton nextButton  = new JButton("\u22C1");
    private final JCheckBox checkCase = new JCheckBox("Match case");
    private final JCheckBox checkWord = new JCheckBox("Match whole word only");
    private final PlaceholderLayerUI layerUI = new PlaceholderLayerUI();
    private final transient HighlightHandler handler = new HighlightHandler();
    private int current;

    public MainPanel() {
        super(new BorderLayout());

        textPane.setEditable(false);
        textPane.setText(INIT_TXT);
        prevButton.setActionCommand("prev");
        nextButton.setActionCommand("next");

        StyledDocument doc = textPane.getStyledDocument();
        //Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        //Style regular = doc.addStyle("regular", def);
        Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);
        Style htf = doc.addStyle("highlight-text-foreground", def);
        StyleConstants.setForeground(htf, new Color(0xFFDDFF));

        field.getDocument().addDocumentListener(handler);
        for (AbstractButton b: Arrays.asList(prevButton, nextButton, checkCase, checkWord)) {
            b.setFocusable(false);
            b.addActionListener(handler);
        }

        JPanel bp = new JPanel(new GridLayout(1, 2));
        bp.add(prevButton);
        bp.add(nextButton);

        JPanel cp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cp.add(checkCase);
        cp.add(checkWord);

        JPanel sp = new JPanel(new BorderLayout(5, 5));
        sp.setBorder(BorderFactory.createTitledBorder("Search"));
        sp.add(new JLayer<JTextComponent>(field, layerUI));
        sp.add(bp, BorderLayout.EAST);
        sp.add(cp, BorderLayout.SOUTH);

        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                changeHighlight();
            }
        });

        add(sp, BorderLayout.NORTH);
        add(new JScrollPane(textPane));
        setPreferredSize(new Dimension(320, 240));
    }

    private static void scrollToCenter(JTextComponent tc, int pos) throws BadLocationException {
        Rectangle rect = tc.modelToView(pos);
        Container c = SwingUtilities.getAncestorOfClass(JViewport.class, tc);
        if (Objects.nonNull(rect) && c instanceof JViewport) {
            rect.x      = (int) (rect.x - c.getWidth() * .5);
            rect.width  = c.getWidth();
            rect.height = (int) (c.getHeight() * .5);
            tc.scrollRectToVisible(rect);
        }
    }

    private Pattern getPattern() {
        String text = field.getText();
        if (Objects.isNull(text) || text.isEmpty()) {
            return null;
        }
        try {
            String cw = checkWord.isSelected() ? "\\b" : "";
            String pattern = String.format("%s%s%s", cw, text, cw);
            int flags = checkCase.isSelected() ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            return Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException ex) {
            field.setBackground(WARNING_COLOR);
            return null;
        }
    }

    private void changeHighlight() {
        field.setBackground(Color.WHITE);
        StyledDocument doc = textPane.getStyledDocument();
        Style s = doc.getStyle("highlight-text-foreground");
        Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);

        //clear the previous highlight:
        Highlighter highlighter = textPane.getHighlighter();
        for (Highlighter.Highlight h: highlighter.getHighlights()) {
            doc.setCharacterAttributes(h.getStartOffset(), h.getEndOffset() - h.getStartOffset(), def, true);
        }
        highlighter.removeAllHighlights();
        //doc.setCharacterAttributes(0, doc.getLength(), def, true);

        //match highlighting:
        try {
            Pattern pattern = getPattern();
            if (Objects.nonNull(pattern)) {
                Matcher matcher = pattern.matcher(doc.getText(0, doc.getLength()));
                int pos = 0;
                while (matcher.find(pos)) {
                    int start = matcher.start();
                    int end   = matcher.end();
                    highlighter.addHighlight(start, end, highlightPainter);
                    //doc.setCharacterAttributes(start, end - start, red, true);
                    pos = end;
                }
            }
            JLabel label = layerUI.hint;
            Highlighter.Highlight[] array = highlighter.getHighlights();
            int hits = array.length;
            if (hits == 0) {
                current = -1;
                label.setOpaque(true);
            } else {
                current = (current + hits) % hits;
                label.setOpaque(false);
                Highlighter.Highlight hh = highlighter.getHighlights()[current];
                highlighter.removeHighlight(hh);
                highlighter.addHighlight(hh.getStartOffset(), hh.getEndOffset(), currentPainter);
                doc.setCharacterAttributes(hh.getStartOffset(), hh.getEndOffset() - hh.getStartOffset(), s, true);
                scrollToCenter(textPane, hh.getStartOffset());
            }
            label.setText(String.format("%02d / %02d%n", current + 1, hits));
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        field.repaint();
    }

    class HighlightHandler implements DocumentListener, ActionListener {
        @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
        @Override public void insertUpdate(DocumentEvent e) {
            changeHighlight();
        }
        @Override public void removeUpdate(DocumentEvent e) {
            changeHighlight();
        }
        @Override public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if (o instanceof AbstractButton) {
                String cmd = ((AbstractButton) o).getActionCommand();
                if ("prev".equals(cmd)) {
                    current--;
                } else if ("next".equals(cmd)) {
                    current++;
                }
            }
            changeHighlight();
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

class PlaceholderLayerUI extends LayerUI<JTextComponent> {
    public final JLabel hint = new JLabel() {
        @Override public void updateUI() {
            super.updateUI();
            setForeground(UIManager.getColor("TextField.inactiveForeground"));
            setBackground(Color.RED);
        }
    };
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (c instanceof JLayer) {
            JLayer jlayer = (JLayer) c;
            JTextComponent tc = (JTextComponent) jlayer.getView();
            if (!tc.getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(hint.getForeground());
                Insets i = tc.getInsets();
                Dimension d = hint.getPreferredSize();
                int x = tc.getWidth() - i.right - d.width - 2;
                int y = (tc.getHeight() - d.height) / 2;
                g2.translate(x, y);
                SwingUtilities.paintComponent(g2, hint, tc, 0, 0, d.width, d.height);
                g2.dispose();
            }
        }
    }
}
