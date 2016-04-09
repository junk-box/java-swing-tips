package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ResizeMouseListener extends MouseAdapter { //NOPMD Cyclomatic Complexity, Standard Cyclomatic Complexity
    private static final Dimension MIN = new Dimension(50, 50);
    private static final Dimension MAX = new Dimension(500, 500);
    private int cursor;
    private Point startPos;
    private Rectangle startingBounds;
    @Override public void mouseMoved(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        ResizableBorder border = (ResizableBorder) c.getBorder();
        c.setCursor(Cursor.getPredefinedCursor(border.getResizeCursor(e)));
    }
    @Override public void mouseExited(MouseEvent e) {
        Component c = e.getComponent();
        c.setCursor(Cursor.getDefaultCursor());
    }
    @Override public void mousePressed(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        ResizableBorder border = (ResizableBorder) c.getBorder();
        cursor = border.getResizeCursor(e);
        startPos = SwingUtilities.convertPoint(c, e.getX(), e.getY(), null);
        startingBounds = c.getBounds();
        Container parent = SwingUtilities.getAncestorOfClass(JLayeredPane.class, c);
        if (parent instanceof JLayeredPane) {
            ((JLayeredPane) parent).moveToFront(c);
        }
    }
    private int getDeltaX(int dx) {
        int deltaX = dx;
        if (startingBounds.width + deltaX < MIN.width) {
            deltaX = -(startingBounds.width - MIN.width);
        } else if (startingBounds.width + deltaX > MAX.width) {
            deltaX = MAX.width - startingBounds.width;
        }
        if (startingBounds.x - deltaX < 0) {
            deltaX = startingBounds.x;
        }
        return deltaX;
    }
    private int getDeltaX(int dx, Rectangle parentBounds) {
        int deltaX = dx;
        if (startingBounds.width - deltaX < MIN.width) {
            deltaX = startingBounds.width - MIN.width;
        } else if (startingBounds.width - deltaX > MAX.width) {
            deltaX = -(MAX.width - startingBounds.width);
        }
        if (startingBounds.x + startingBounds.width - deltaX > parentBounds.width) {
            deltaX = startingBounds.x + startingBounds.width - parentBounds.width;
        }
        return deltaX;
    }
    private int getDeltaY(int dy) {
        int deltaY = dy;
        if (startingBounds.height + deltaY < MIN.height) {
            deltaY = -(startingBounds.height - MIN.height);
        } else if (startingBounds.height + deltaY > MAX.height) {
            deltaY = MAX.height - startingBounds.height;
        }
        if (startingBounds.y - deltaY < 0) {
            deltaY = startingBounds.y;
        }
        return deltaY;
    }
    private int getDeltaY(int dy, Rectangle parentBounds) {
        int deltaY = dy;
        if (startingBounds.height - deltaY < MIN.height) {
            deltaY = startingBounds.height - MIN.height;
        } else if (startingBounds.height - deltaY > MAX.height) {
            deltaY = -(MAX.height - startingBounds.height);
        }
        if (startingBounds.y + startingBounds.height - deltaY > parentBounds.height) {
            deltaY = startingBounds.y + startingBounds.height - parentBounds.height;
        }
        return deltaY;
    }
    //@see %JAVA_HOME%/src/javax/swing/plaf/basic/BasicInternalFrameUI.java
    @Override public void mouseDragged(MouseEvent e) { //NOPMD Cyclomatic Complexity, Standard Cyclomatic Complexity
        if (startPos == null || startingBounds == null) {
            return;
        }
        Component c = e.getComponent();
        Point p = SwingUtilities.convertPoint(c, e.getX(), e.getY(), null);
        int deltaX = startPos.x - p.x;
        int deltaY = startPos.y - p.y;

        Container parent = SwingUtilities.getUnwrappedParent(c);
        Rectangle parentBounds = parent.getBounds();

        switch (cursor) {
          case Cursor.NW_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX);
              int dy = getDeltaY(deltaY);
              c.setBounds(
                  startingBounds.x - dx,
                  startingBounds.y - dy,
                  startingBounds.width  + dx,
                  startingBounds.height + dy);
              break;
          }
          case Cursor.N_RESIZE_CURSOR: {
              int dy = getDeltaY(deltaY);
              c.setBounds(
                  startingBounds.x,
                  startingBounds.y - dy,
                  startingBounds.width,
                  startingBounds.height + dy);
              break;
          }
          case Cursor.NE_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX, parentBounds);
              int dy = getDeltaY(deltaY);
              c.setBounds(
                  startingBounds.x,
                  startingBounds.y - dy,
                  startingBounds.width  - dx,
                  startingBounds.height + dy);
              break;
          }
          case Cursor.E_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX, parentBounds);
              c.setSize(
                  startingBounds.width - dx,
                  startingBounds.height);
              break;
          }
          case Cursor.SE_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX, parentBounds);
              int dy = getDeltaY(deltaY, parentBounds);
              c.setSize(
                  startingBounds.width  - dx,
                  startingBounds.height - dy);
              break;
          }
          case Cursor.S_RESIZE_CURSOR: {
              int dy = getDeltaY(deltaY, parentBounds);
              c.setSize(
                  startingBounds.width,
                  startingBounds.height - dy);
              break;
          }
          case Cursor.SW_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX);
              int dy = getDeltaY(deltaY, parentBounds);
              c.setBounds(
                  startingBounds.x - dx,
                  startingBounds.y,
                  startingBounds.width  + dx,
                  startingBounds.height - dy);
              break;
          }
          case Cursor.W_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX);
              c.setBounds(
                  startingBounds.x - dx,
                  startingBounds.y,
                  startingBounds.width + dx,
                  startingBounds.height);
              break;
          }
          case Cursor.MOVE_CURSOR: {
              c.setLocation(
                  startingBounds.x - deltaX,
                  startingBounds.y - deltaY);
              break;
          }
          default:
            return;
        }
        //setBounds(newX, newY, newW, newH);
        parent.revalidate();
        //parent.repaint();
    }
    @Override public void mouseReleased(MouseEvent e) {
        startPos = null;
        startingBounds = null;
    }
}
