package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;

import org.basex.gui.*;

/**
 * This separator splits several panels and allows panel resizing.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class BaseXSplitSep extends BaseXBack {
  /** Size of splitter. */
  static final int SIZE = 8;
  /** Layout: horizontal = true, vertical = false. */
  private final boolean l;

  /**
   * Constructor.
   * @param lay layout: horizontal = true, vertical = false
   */
  BaseXSplitSep(final boolean lay) {
    setCursor(lay ? GUIConstants.CURSORMOVEH : GUIConstants.CURSORMOVEV);
    final MouseInputAdapter mouse = new MouseInputAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        ((BaseXSplit) getParent()).startDrag(pos(e));
      }
      @Override
      public void mouseDragged(final MouseEvent e) {
        ((BaseXSplit) getParent()).drag(BaseXSplitSep.this, pos(e));
      }
    };
    addMouseListener(mouse);
    addMouseMotionListener(mouse);
    l = lay;
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final int w = getWidth();
    final int h = getHeight();

    g.setColor(GUIConstants.PANEL);
    g.fillRect(0, 0, w, h);
    g.setColor(GUIConstants.gray);
    g.drawLine(0, 0, l ? 0 : w, l ? h : 0);
    g.drawLine(l ? w - 1 : 0, l ? 0 : h - 1, l ? w - 1 : w, l ? h : h - 1);
  }

  /**
   * Returns absolute cursor position.
   * @param e mouse event
   * @return absolute cursor position
   */
  private double pos(final MouseEvent e) {
    final Point p = getLocationOnScreen();
    return l ? p.x + e.getX() : p.y + e.getY();
  }
}
