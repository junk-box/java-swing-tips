package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    private static URL image = MainPanel.class.getResource("favicon.png");
    private final String htmlText =
      "<html><body>" +
      "span tag: <span style='background:#88ff88;' title='tooltip: span[@title]'>span span span</span><br />" +
      "<div title='tooltip: div[@title]'>div tag: div div div div</div>" +
      "<div style='padding: 2 24;'><img src='"+ image +"' alt='16x16 favicon' />&nbsp;" +
      "<a href='http://terai.xrea.jp/'>Java Swing Tips</a></div>" +
      "</body></html>";

    public MainPanel() {
        super(new BorderLayout());

        JTextPane editor1 = new JTextPane() {
            //private boolean doesElementContainLocation(JEditorPane editor, Element e, int offset, int x, int y) {
            //    if(e != null && offset > 0 && e.getStartOffset() == offset) {
            //        try {
            //            TextUI ui = editor.getUI();
            //            Shape s1 = ui.modelToView(editor, offset, Position.Bias.Forward);
            //            if(s1 == null) {
            //                return false;
            //            }
            //            Rectangle r1 = (s1 instanceof Rectangle) ? (Rectangle)s1 : s1.getBounds();
            //            Shape s2 = ui.modelToView(editor, e.getEndOffset(), Position.Bias.Backward);
            //            if(s2 != null) {
            //                Rectangle r2 = (s2 instanceof Rectangle) ? (Rectangle)s2 : s2.getBounds(); r1.add(r2);
            //            }
            //            return r1.contains(x, y);
            //        }catch(BadLocationException ble) {}
            //    }
            //    return true;
            //}
            private transient Position.Bias[] bias = new Position.Bias[1];
            @Override public String getToolTipText(MouseEvent e) {
                String title = super.getToolTipText(e);
                JEditorPane editor = (JEditorPane) e.getSource();
                HTMLEditorKit kit = (HTMLEditorKit)editor.getEditorKit();
                if(!editor.isEditable()) {
                    Point pt = new Point(e.getX(), e.getY());
                    int pos = editor.getUI().viewToModel(editor, pt, bias);
                    if(bias[0] == Position.Bias.Backward && pos > 0) {
                        pos--;
                    }
                    if(pos >= 0 &&(editor.getDocument() instanceof HTMLDocument)) {
                        HTMLDocument hdoc = (HTMLDocument)editor.getDocument();
                        Element elem = hdoc.getCharacterElement(pos);
                        //if(!doesElementContainLocation(editor, elem, pos, e.getX(), e.getY())) {
                        //    elem = null;
                        //}
                        if(elem != null) {
                            AttributeSet a = elem.getAttributes();
                            AttributeSet span = (AttributeSet)a.getAttribute(HTML.Tag.SPAN);
                            if(span != null) {
                                title = (String)span.getAttribute(HTML.Attribute.TITLE);
                            }
                        }
                    }
                }
                return title;
            }
        };
        editor1.setEditorKit(new HTMLEditorKit());
        editor1.setText(htmlText);
        editor1.setEditable(false);
        ToolTipManager.sharedInstance().registerComponent(editor1);

        JTextPane editor2 = new JTextPane();
        editor2.setEditorKit(new TooltipEditorKit());
        editor2.setText(htmlText);
        editor2.setEditable(false);
        editor2.addHyperlinkListener(new HyperlinkListener() {
            private String tooltip;
            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                JEditorPane editorPane = (JEditorPane)e.getSource();
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    //JOptionPane.showMessageDialog(editorPane, "You click the link with the URL " + e.getURL());
                }else if(e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    tooltip = editorPane.getToolTipText();
                    URL url = e.getURL();
                    editorPane.setToolTipText((url!=null)?url.toExternalForm():null);
                }else if(e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    editorPane.setToolTipText(tooltip);
                }
            }
        });
        ToolTipManager.sharedInstance().registerComponent(editor2);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setResizeWeight(.5);
        sp.setTopComponent(new JScrollPane(editor1));
        sp.setBottomComponent(new JScrollPane(editor2));
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class TooltipEditorKit extends HTMLEditorKit {
    public ViewFactory getViewFactory() {
        return new HTMLFactory() {
            public View create(Element elem) {
                AttributeSet attrs = elem.getAttributes();
                Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
                Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);
                if(o instanceof HTML.Tag) {
                    HTML.Tag kind = (HTML.Tag) o;
                    if(kind == HTML.Tag.DIV) {
                        return new BlockView(elem, View.Y_AXIS) {
                            @Override public String getToolTipText(float x, float y, Shape allocation) {
                                String s = super.getToolTipText(x, y, allocation);
                                if(s==null) {
                                    s = (String)getElement().getAttributes().getAttribute(HTML.Attribute.TITLE);
                                }
                                return s;
                            }
                        };
                    }
                }
                return super.create(elem);
            }
        };
    }
}