package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;

import org.basex.gui.*;

/**
 * This separator splits several panels and allows panel resizing.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class BaseXSplitSep extends BaseXBack {
  /** Layout: horizontal = true, vertical = false. */
  private final boolean horizontal;

  /**
   * Constructor.
   * @param horizontal horizontal/vertical layout
   */
  BaseXSplitSep(final boolean horizontal) {
    setCursor(horizontal ? GUIConstants.CURSORMOVEH : GUIConstants.CURSORMOVEV);
    final MouseInputAdapter mouse = new MouseInputAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        ((BaseXSplit) getParent()).startDrag(pos(e));
      }
      @Override
      public void mouseDragged(final MouseEvent e) {
        ((BaseXSplit) getParent()).drag(BaseXSplitSep.this, pos(e));
      }

      private double pos(final MouseEvent e) {
        final Point p = getLocationOnScreen();
        return horizontal ? p.x + e.getX() : p.y + e.getY();
      }
    };
    addMouseListener(mouse);
    addMouseMotionListener(mouse);
    this.horizontal = horizontal;
  }

  @Override
  public void paintComponent(final Graphics g) {
    final int w = getWidth(), h = getHeight();
    g.setColor(GUIConstants.PANEL);
    g.fillRect(0, 0, w, h);
    g.setColor(GUIConstants.gray);
    g.drawLine(0, 0, horizontal ? 0 : w, horizontal ? h : 0);
    g.drawLine(horizontal ? w - 1 : 0, horizontal ? 0 : h - 1,
               horizontal ? w - 1 : w, horizontal ? h : h - 1);
  }
}
