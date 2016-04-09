package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

// http://docs.oracle.com/javase/7/docs/api/javax/swing/plaf/multi/doc-files/multi_tsc.html
//???: Don't extend visual look and feels.
public class AuxiliaryWindowsComboBoxUI extends WindowsComboBoxUI {
    public static ComponentUI createUI(JComponent c) {
        return new AuxiliaryWindowsComboBoxUI();
    }
    @Override protected ComboPopup createPopup() {
        //System.out.println("AuxiliaryWindowsComboBoxUI#createPopup");
        return new BasicComboPopup2(comboBox);
    }
//     //???: Use the installUI method to perform all initialization, and the uninstallUI method to perform all cleanup.
//     @Override public void installUI(JComponent c) {
//         //super.installUI(c);
//     }
//     @Override public void uninstallUI(JComponent c) {
//         //super.uninstallUI(c);
//     }
    //Override all UI-specific methods your UI classes inherit.
    @Override protected void configureEditor()   { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override protected void unconfigureEditor() { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void removeEditor()         { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void addEditor() {
        removeEditor();
        ComboBoxEditor cbe = comboBox.getEditor();
        if (Objects.nonNull(cbe)) {
            editor = cbe.getEditorComponent();
            if (Objects.nonNull(editor)) {
                configureEditor();
                comboBox.add(editor);
                if (comboBox.isFocusOwner()) {
                    // Switch focus to the editor component
                    editor.requestFocusInWindow();
                }
            }
        }
    }
//     @Override public void unconfigureArrowButton() {}
//     @Override public void configureArrowButton() {}
    @Override public void update(Graphics g, JComponent c) { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void paint(Graphics g, JComponent c)  { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus)           { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) { /* Override all UI-specific methods your UI classes inherit. */ }
}

class BasicComboPopup2 extends BasicComboPopup {
    private transient Handler2 handler2;
    protected BasicComboPopup2(JComboBox combo) {
        super(combo);
    }
    @Override public void uninstallingUI() {
        super.uninstallingUI();
        handler2 = null;
    }
    @Override protected MouseListener createListMouseListener() {
        if (Objects.isNull(handler2)) {
            handler2 = new Handler2();
        }
        return handler2;
    }
    private class Handler2 extends MouseAdapter {
        @Override public void mouseReleased(MouseEvent e) {
            if (Objects.equals(e.getSource(), list)) {
                if (list.getModel().getSize() > 0) {
                    // <ins>
                    if (!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled()) {
                        return;
                    }
                    // </ins>
                    // JList mouse listener
                    if (comboBox.getSelectedIndex() == list.getSelectedIndex()) {
                        comboBox.getEditor().setItem(list.getSelectedValue());
                    }
                    comboBox.setSelectedIndex(list.getSelectedIndex());
                }
                comboBox.setPopupVisible(false);
                // workaround for cancelling an edited item (bug 4530953)
                if (comboBox.isEditable() && Objects.nonNull(comboBox.getEditor())) {
                    comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
                }
            }
        }
    }
}
