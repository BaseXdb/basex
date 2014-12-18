package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import org.basex.gui.*;
import org.basex.util.*;

/**
 * This is a scrollbar implementation, supporting arbitrary
 * panel heights without increasing the memory consumption.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXScrollBar extends BaseXPanel {
  /** Scaled Scrollbar size. */
  public static final int SIZE = (int) (16 * GUIConstants.SCALE);
  /** Maximum scrolling speed. */
  private static final int MAXSTEP = 15;
  /** Animated scrollbar zooming steps. */
  private static final int[] STEPS = { -MAXSTEP, -14, -11, -8, -6, -4, -3,
      -2, -1, -1, 0, 0, 1, 1, 2, 3, 4, 6, 8, 11, 14, MAXSTEP };
  /** Minimum size for the scrollbar slider. */
  private static final int MINSIZE = 20;

  /** Reference to the scrolled component. */
  private final BaseXPanel comp;
  /** Scrollbar width. */
  private final int ww;

  /** Current scrolling speed. */
  private int step = STEPS.length / 2;
  /** Flag reporting if the scrollbar animation is running. */
  private boolean animated;
  /** Scrollbar height. */
  private int hh;
  /** Scrollbar slider position. */
  private int barPos;
  /** Scrollbar slider size. */
  private int barSize;
  /** Scrollbar dragging position. */
  private int dragPos;
  /** Flag for button clicks. */
  private boolean button;
  /** Flag for scrolling downward. */
  private boolean down;
  /** Flag for sliding the scrollbar. */
  private boolean sliding;
  /** Flag for moving upward. */
  private boolean moving;
  /** Current panel position. */
  private int pos;
  /** Current panel height. */
  private int height;

  /** Flag for scrolling upward. */
  private boolean up;
  /** Scrollbar slider offset. */
  private int barOffset;

  /**
   * Default constructor. By default, the scrollbar is switched off
   * if the component is completely displayed.
   * @param cmp reference to the scrolled component
   */
  public BaseXScrollBar(final BaseXPanel cmp) {
    super(cmp.gui);
    comp = cmp;
    addMouseListener(this);
    addKeyListener(this);
    addMouseMotionListener(this);
    setOpaque(false);
    setPreferredSize(new Dimension(SIZE, getPreferredSize().height));
    ww = SIZE;
  }

  /**
   * Sets the vertical scrollbar slider position.
   * @param p vertical position
   */
  public void pos(final int p) {
    final int pp = Math.max(0, Math.min(height - getHeight(), p));
    if(pos == pp) return;
    pos = pp;
    repaint();
  }

  /**
   * Returns the vertical scrollbar slider position.
   * @return vertical position
   */
  public int pos() {
    return pos;
  }

  /**
   * Sets the panel height.
   * @param h panel height
   */
  public void height(final int h) {
    if(height != h) {
      height = h;
      repaint();
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    hh = getHeight();
    super.paintComponent(g);
    if(hh >= height) return;

    // calculate bar size
    final int barH = hh - ww * 2 + 4;
    final float factor = (barH - barOffset) / (float) height;
    int size = (int) (hh * factor);
    // define minimum size for scrollbar mover
    barOffset = Math.max(0, MINSIZE - size);
    size += barOffset;
    barSize = Math.min(size, barH - 1);
    barPos = (int) Math.max(0, Math.min(pos * factor, barH - barSize));

    // paint scrollbar background
    g.setColor(GUIConstants.lgray);
    g.fillRect(0, 0, ww, hh);

    // draw scroll slider
    int bh = ww - 2 + barPos;
    BaseXLayout.drawCell(g, 0, ww, bh, bh + barSize, false);

    final int d = (int) (2 * GUIConstants.SCALE);
    bh += barSize / 2;
    g.setColor(GUIConstants.dgray);
    g.drawLine(5, bh, ww - 6, bh);
    g.drawLine(5, bh - d, ww - 6, bh - d);
    g.drawLine(5, bh + d, ww - 6, bh + d);
    BaseXLayout.antiAlias(g);

    // draw scroll buttons
    drawButton(g, new int[][] { { 0, 6, 3 }, { 6, 6, 0 } }, 0, button && up);
    drawButton(g, new int[][] { { 0, 6, 3 }, { 0, 0, 6 } }, Math.max(SIZE, hh - ww),
        button && down);

    // paint scrollbar lines
    g.setColor(GUIConstants.gray);
    g.drawLine(0, 0, 0, hh);
    g.drawLine(ww - 1, 0, ww - 1, hh);
  }

  /**
   * Draws the down/up button.
   * @param g graphics reference
   * @param pol polygons
   * @param y vertical start value
   * @param focus focus flag
   */
  private void drawButton(final Graphics g, final int[][] pol, final int y, final boolean focus) {
    BaseXLayout.drawCell(g, 0, ww, y, y + ww, focus);
    final int pl = pol[0].length;
    for(int i = 0; i < pl; ++i) {
      pol[0][i] += SIZE / 2 - 3;
      pol[1][i] += y + SIZE / 2 - 3;
    }
    g.setColor(focus ? GUIConstants.FORE : GUIConstants.dgray);
    g.fillPolygon(pol[0], pol[1], 3);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    final int y = e.getY();
    sliding = y > ww + barPos && y < ww + barPos + barSize;
    moving = !sliding;
    up = y < ww + barPos;
    down = y > ww + barPos + barSize;
    button = y < ww || y > hh - ww;
    if(sliding) dragPos = barPos - y;

    // start dragging
    if(sliding || animated) return;

    new Thread() {
      @Override
      public void run() {
        // scroll up/down/move slider
        animated = moving;
        while(animated) {
          if(moving) step = Math.max(0, Math.min(STEPS.length - 1, step + (down ? 1 : -1)));
          else step += step < STEPS.length / 2 ? 1 : -1;
          int offset = STEPS[step];

          if(!button) offset = offset * hh / MAXSTEP / 4;
          pos = Math.max(0, Math.min(height - hh, pos + offset));
          comp.repaint();
          Performance.sleep(25);
          animated = step != STEPS.length / 2;

          if(y > ww + barPos && y < ww + barPos + barSize) {
            dragPos = barPos - y;
            animated = false;
            sliding = true;
            step = STEPS.length / 2;
          }
        }
      }
    }.start();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    up = false;
    down = false;
    moving = false;
    sliding = false;
    comp.repaint();
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    // no dragging...
    if(!sliding) return;

    pos = (int) ((long) (e.getY() + dragPos) * height / (hh - ww * 2));
    pos = Math.max(0, Math.min(height - hh, pos));
    comp.repaint();
  }
}
