package org.basex.gui.layout;

import java.awt.*;

/**
 * This LayoutManager is similar to the GridLayout. The added components
 * keep their minimum size even when the parent container is resized.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class TableLayout implements LayoutManager {
  /** Number of columns. */
  private final int cols;
  /** Number of rows. */
  private final int rows;
  /** Horizontal inset. */
  private final int insetX;
  /** Vertical inset. */
  private final int insetY;
  /** Panel width. */
  private int width;
  /** Panel height. */
  private int height;
  /** Horizontal position. */
  private final int[] posX;
  /** Vertical position. */
  private final int[] posY;

  /**
   * Creates a grid layout with the specified number of rows and columns.
   * When displayed, the grid has the minimum size.
   * @param r number of rows
   * @param c number of columns
   */
  public TableLayout(final int r, final int c) {
    this(r, c, 0, 0);
  }

  /**
   * Creates a grid layout with the specified number of rows and columns.
   * When displayed, the grid has the minimum size.
   * @param r number of rows
   * @param c number of columns
   * @param ix horizontal gap
   * @param iy vertical gap
   */
  public TableLayout(final int r, final int c, final int ix, final int iy) {
    rows = r;
    cols = c;
    insetX = ix;
    insetY = iy;
    posX = new int[c];
    posY = new int[r];
  }

  @Override
  public void addLayoutComponent(final String name, final Component comp) { }

  @Override
  public void removeLayoutComponent(final Component comp) { }

  @Override
  public Dimension preferredLayoutSize(final Container parent) {
    synchronized(parent.getTreeLock()) {
      final Insets in = parent.getInsets();
      final int nr = parent.getComponentCount();

      int maxW = 0;
      int maxH = 0;
      for(int i = 0; i < cols; ++i) {
        posX[i] = maxW;
        final int w = maxW;
        int h = 0;

        for(int j = 0; j < rows; ++j) {
          final int n = j * cols + i;
          if(n >= nr) break;

          final Component c = parent.getComponent(n);
          final Dimension d = c.getPreferredSize();
          if(maxW < w + d.width) maxW = w + d.width;
          if(posY[j] < h) posY[j] = h;
          else h = posY[j];
          h += d.height;
        }
        if(maxH < h) maxH = h;
      }
      width = in.left + maxW + (cols - 1) * insetX + in.right;
      height = in.top + maxH + (rows - 1) * insetY + in.bottom;

      return new Dimension(width, height);
    }
  }

  @Override
  public Dimension minimumLayoutSize(final Container parent) {
    return preferredLayoutSize(parent);
  }

  @Override
  public void layoutContainer(final Container p) {
    preferredLayoutSize(p);
    synchronized(p.getTreeLock()) {
      final Insets in = p.getInsets();
      final int nr = p.getComponentCount();
      for(int j = 0; j < rows; ++j) {
        for(int i = 0; i < cols; ++i) {
          final int n = j * cols + i;
          if(n >= nr) return;
          final Dimension cs = p.getComponent(n).getPreferredSize();
          final int x = in.left + posX[i] + i * insetX;
          final int y = in.top + posY[j] + j * insetY;
          final int w = cs.width > 0 ? cs.width : width - in.left - in.right;
          final int h = cs.height > 0 ? cs.height : height - in.top - in.bottom;
          p.getComponent(n).setBounds(x, y, w, h);
        }
      }
    }
  }
}
