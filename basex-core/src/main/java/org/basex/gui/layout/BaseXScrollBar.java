package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import org.basex.gui.*;
import org.basex.util.list.*;

/**
 * This is a scrollbar implementation, supporting arbitrary
 * panel heights without increasing the memory consumption.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXScrollBar extends BaseXPanel {
  /** Scrollbar width. */
  private static final int SIZE = 14;
  /** Width of the slider. */
  private static final int SLIDER = SIZE - 6;
  /** Width of the active slider. */
  private static final int SLIDER_ACTIVE = SIZE - 4;
  /** Minimum height of the slider. */
  private static final int MINSIZE = 28;

  /** Reference to the scrolled component. */
  private final BaseXPanel comp;

  /** Scrollbar height. */
  private int hh;
  /** Scrollbar slider position. */
  private int barPos;
  /** Scrollbar slider size. */
  private int barSize;
  /** Scrollbar dragging offset. */
  private int dragPos;
  /** Flag for dragging the slider. */
  private boolean sliding;
  /** Flag for hovering the scrollbar. */
  private boolean hover;
  /** Current panel position. */
  private int pos;
  /** Current panel height. */
  private int height;

  /** Document-space y of the search hits ({@code null} if there are none). */
  private IntList markPos;
  /** Hits, rasterized to one flag per pixel row of the slider track. */
  private boolean[] markRows;
  /** Track height the hits were rasterized for. */
  private int markHeight;
  /** Panel height the hits were rasterized for. */
  private int markTotal;

  /**
   * Default constructor. By default, the scrollbar is switched off
   * if the component is completely displayed.
   * @param comp reference to the scrolled component
   */
  public BaseXScrollBar(final BaseXPanel comp) {
    super(comp.gui);
    this.comp = comp;

    addMouseListener(this);
    addKeyListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    setOpaque(false);
    refreshLayout();
  }

  /**
   * Refreshes the layout.
   */
  public void refreshLayout() {
    setPreferredSize(new Dimension(SIZE, getPreferredSize().height));
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
   * Assigns the positions of the search hits.
   * @param ys ascending document-space y of the hits (can be {@code null})
   */
  public void marks(final IntList ys) {
    markPos = ys;
    markRows = null;
    repaint();
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

    // calculate slider size and position
    barSize = Math.min(hh, Math.max(MINSIZE, (int) ((long) hh * hh / height)));
    final int travel = hh - barSize;
    barPos = travel <= 0 ? 0 : (int) ((long) Math.min(pos, height - hh) * travel / (height - hh));
    paintMarks(g);

    // paint slider: grows and darkens while it is hovered or dragged
    BaseXLayout.antiAlias(g);
    final boolean active = sliding || hover;
    final int w = active ? SLIDER_ACTIVE : SLIDER;
    g.setColor(active ? GUIConstants.middleGrayA : GUIConstants.grayA);
    g.fillRoundRect((SIZE - w) / 2, barPos, w, barSize, w / 2, w / 2);
  }

  /**
   * Draws a marker for each search hit, rasterized to the pixel rows of the slider track.
   * @param g graphics reference
   */
  private void paintMarks(final Graphics g) {
    if(markPos == null || markPos.isEmpty() || hh < 2) return;

    if(markRows == null || markHeight != hh || markTotal != height) {
      markHeight = hh;
      markTotal = height;
      markRows = new boolean[hh];
      final int ms = markPos.size();
      for(int m = 0; m < ms; m++) markRows[row(markPos.get(m))] = true;
    }
    g.setColor(GUIConstants.color2);
    for(int r = 0; r < hh; r++) {
      if(markRows[r]) g.fillRect(0, Math.min(r, hh - 2), SIZE, 2);
    }
  }

  /**
   * Returns the pixel row of the slider track that represents the specified document-space y.
   * @param y document-space y
   * @return pixel row
   */
  private int row(final int y) {
    return Math.max(0, Math.min(hh - 1, (int) ((long) y * hh / height)));
  }

  /**
   * Moves the slider to the specified position and scrolls the component.
   * @param barY new slider position
   */
  private void scroll(final int barY) {
    final int travel = hh - barSize;
    barPos = Math.max(0, Math.min(travel, barY));
    pos = travel <= 0 ? 0 : (int) ((long) barPos * (height - hh) / travel);
    comp.repaint();
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(height <= hh) return;

    // jump to the clicked position if the slider was not hit
    final int y = e.getY();
    if(y < barPos || y >= barPos + barSize) scroll(y - barSize / 2);
    dragPos = barPos - y;
    sliding = true;
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    sliding = false;
    hover = contains(e.getX(), e.getY());
    comp.repaint();
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(sliding) scroll(e.getY() + dragPos);
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    hover = true;
    repaint();
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    hover = false;
    repaint();
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    comp.mouseWheelMoved(e);
  }
}
