package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import org.basex.gui.*;
import org.basex.util.list.*;

/**
 * This is a scrollbar implementation, supporting arbitrary
 * panel sizes without increasing the memory consumption.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXScrollBar extends BaseXPanel {
  /** Scrollbar thickness. */
  private static final int SIZE = 14;
  /** Thickness of the slider. */
  private static final int SLIDER = SIZE - 6;
  /** Thickness of the active slider. */
  private static final int SLIDER_ACTIVE = SIZE - 4;
  /** Minimum length of the slider. */
  private static final int MINSIZE = 28;

  /** Reference to the scrolled component. */
  private final BaseXPanel comp;
  /** Horizontal orientation. */
  private final boolean horizontal;

  /** Length of the slider track. */
  private int track;
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
  /** Current panel extent. */
  private int extent;

  /** Document-space y of the search hits ({@code null} if there are none). */
  private IntList markPos;
  /** Hits, rasterized to one flag per pixel row of the slider track (can be {@code null}). */
  private boolean[] markRows;
  /** Track length the hits were rasterized for. */
  private int markHeight;
  /** Panel extent the hits were rasterized for. */
  private int markTotal;

  /**
   * Default constructor. By default, the scrollbar is switched off
   * if the component is completely displayed.
   * @param comp reference to the scrolled component
   */
  public BaseXScrollBar(final BaseXPanel comp) {
    this(comp, false);
  }

  /**
   * Constructor with orientation.
   * @param comp reference to the scrolled component
   * @param horizontal horizontal orientation
   */
  public BaseXScrollBar(final BaseXPanel comp, final boolean horizontal) {
    super(comp.gui);
    this.comp = comp;
    this.horizontal = horizontal;

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
    final Dimension ps = getPreferredSize();
    setPreferredSize(horizontal ? new Dimension(ps.width, SIZE) : new Dimension(SIZE, ps.height));
  }

  /**
   * Sets the scrollbar slider position.
   * @param p position
   */
  public void pos(final int p) {
    final int pp = Math.max(0, Math.min(extent - length(), p));
    if(pos == pp) return;
    pos = pp;
    repaint();
  }

  /**
   * Returns the scrollbar slider position.
   * @return position
   */
  public int pos() {
    return pos;
  }

  /**
   * Returns the length of the scrollbar along its orientation.
   * @return length
   */
  private int length() {
    return horizontal ? getWidth() : getHeight();
  }

  /**
   * Returns the position of a mouse event along the orientation of the scrollbar.
   * @param e mouse event
   * @return position
   */
  private int coord(final MouseEvent e) {
    return horizontal ? e.getX() : e.getY();
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
   * Sets the extent of the scrolled panel.
   * @param e panel extent
   */
  public void extent(final int e) {
    if(extent != e) {
      extent = e;
      repaint();
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    track = length();
    super.paintComponent(g);
    if(track >= extent) return;

    // calculate slider size and position
    barSize = Math.min(track, Math.max(MINSIZE, (int) ((long) track * track / extent)));
    final int travel = track - barSize;
    barPos = travel <= 0 ? 0 :
      (int) ((long) Math.min(pos, extent - track) * travel / (extent - track));
    paintMarks(g);

    // paint slider: grows and darkens while it is hovered or dragged
    BaseXLayout.antiAlias(g);
    final boolean active = sliding || hover;
    final int w = active ? SLIDER_ACTIVE : SLIDER, c = (SIZE - w) / 2;
    g.setColor(active ? GUIConstants.middleGrayA : GUIConstants.grayA);
    if(horizontal) g.fillRoundRect(barPos, c, barSize, w, w / 2, w / 2);
    else g.fillRoundRect(c, barPos, w, barSize, w / 2, w / 2);
  }

  /**
   * Draws a marker for each search hit, rasterized to the pixel rows of the slider track.
   * @param g graphics reference
   */
  private void paintMarks(final Graphics g) {
    if(markPos == null || markPos.isEmpty() || track < 2) return;

    if(markRows == null || markHeight != track || markTotal != extent) {
      markHeight = track;
      markTotal = extent;
      markRows = new boolean[track];
      final int ms = markPos.size();
      for(int m = 0; m < ms; m++) markRows[row(markPos.get(m))] = true;
    }
    g.setColor(GUIConstants.color2);
    for(int r = 0; r < track; r++) {
      if(markRows[r]) g.fillRect(0, Math.min(r, track - 2), SIZE, 2);
    }
  }

  /**
   * Returns the pixel row of the slider track that represents the specified document-space y.
   * @param y document-space y
   * @return pixel row
   */
  private int row(final int y) {
    return Math.max(0, Math.min(track - 1, (int) ((long) y * track / extent)));
  }

  /**
   * Moves the slider to the specified position and scrolls the component.
   * @param bar new slider position
   */
  private void scroll(final int bar) {
    final int travel = track - barSize;
    barPos = Math.max(0, Math.min(travel, bar));
    pos = travel <= 0 ? 0 : (int) ((long) barPos * (extent - track) / travel);
    comp.repaint();
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(extent <= track) return;

    // jump to the clicked position if the slider was not hit
    final int c = coord(e);
    if(c < barPos || c >= barPos + barSize) scroll(c - barSize / 2);
    dragPos = barPos - c;
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
    if(sliding) scroll(coord(e) + dragPos);
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
    if(horizontal) {
      pos(pos + e.getUnitsToScroll() * 20);
      comp.repaint();
    } else {
      comp.mouseWheelMoved(e);
    }
  }
}
