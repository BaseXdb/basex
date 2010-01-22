package org.basex.gui.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import org.basex.gui.GUIConstants;

/**
 * This separator splits several panels and allows panel resizing.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BaseXSplitSep extends BaseXBack {
  /** Size of splitter. */
  public static final int SIZE = 6;
  /** Color foreground. */
  public static Color fore;
  /** Color background. */
  public static Color back;
  /** Layout: horizontal = true, vertical = false. */
  private final boolean l;

  /**
   * Constructor.
   * @param lay layout: horizontal = true, vertical = false
   */
  public BaseXSplitSep(final boolean lay) {
    setCursor(lay ? GUIConstants.CURSORMOVEH : GUIConstants.CURSORMOVEV);
    fore = getBackground();
    back = fore.darker();
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

    g.setColor(fore);
    g.fillRect(0, 0, w, h);
    g.setColor(back);
    g.drawLine(0, 0, l ? 0 : w, l ? h : 0);
    g.drawLine(l ? w - 1 : 0, l ? 0 : h - 1, l ? w - 1 : w, l ? h : h - 1);
  }

  /**
   * Returns absolute cursor position.
   * @param e mouse event
   * @return absolute cursor position
   */
  protected double pos(final MouseEvent e) {
    final Point p = getLocationOnScreen();
    return l ? p.x + e.getX() : p.y + e.getY();
  }
}
