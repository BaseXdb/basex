package org.basex.gui.layout;

import java.awt.*;

import org.basex.util.*;

/**
 * This LayoutManager is similar to the GridLayout. The added components
 * keep their minimum size even when the parent container is resized.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class TableLayout implements LayoutManager {
  /** Number of columns. */
  private final int cols;
  /** Number of rows. */
  private final int rows;
  /** Horizontal gap. */
  private final int hgap;
  /** Vertical gap. */
  private final int vgap;
  /** Horizontal position. */
  private final int[] posX;
  /** Vertical position. */
  private final int[] posY;

  /**
   * Creates a table layout with the specified number of rows and columns.
   * @param rows number of rows
   * @param cols number of columns
   */
  public TableLayout(final int rows, final int cols) {
    this(rows, cols, 0, 0);
  }

  /**
   * Creates a table layout with the specified number of rows and columns.
   * @param rows number of rows
   * @param cols number of columns
   * @param hgap horizontal gap
   * @param vgap vertical gap
   */
  public TableLayout(final int rows, final int cols, final int hgap, final int vgap) {
    this.rows = rows;
    this.cols = cols;
    this.hgap = hgap;
    this.vgap = vgap;
    posX = new int[cols];
    posY = new int[rows];
  }

  @Override
  public void addLayoutComponent(final String name, final Component comp) { }

  @Override
  public void removeLayoutComponent(final Component comp) { }

  @Override
  public Dimension preferredLayoutSize(final Container cont) {
    synchronized(cont.getTreeLock()) {
      final Insets in = cont.getInsets();
      final int nr = cont.getComponentCount();
      if(nr > cols * rows) Util.errln("Too many components specified in TableLayout (%/%): %",
          nr, cols, cont.getComponent(cols * rows));

      int maxW = 0, maxH = 0;
      for(int c = 0; c < cols; c++) {
        posX[c] = maxW;
        final int w = maxW;
        int h = 0;

        for(int r = 0; r < rows; r++) {
          final int n = r * cols + c;
          if(n >= nr) break;

          final Dimension d = cont.getComponent(n).getPreferredSize();
          maxW = Math.max(maxW, w + d.width);
          if(posY[r] < h) posY[r] = h;
          else h = posY[r];
          h += d.height;
        }
        maxH = Math.max(maxH, h);
      }
      final int w = in.left + maxW + (cols - 1) * hgap + in.right;
      final int h = in.top + maxH + (rows - 1) * vgap + in.bottom;
      return new Dimension(w, h);
    }
  }

  @Override
  public Dimension minimumLayoutSize(final Container parent) {
    return preferredLayoutSize(parent);
  }

  @Override
  public void layoutContainer(final Container cont) {
    synchronized(cont.getTreeLock()) {
      preferredLayoutSize(cont);
      final Insets in = cont.getInsets();
      final int nr = cont.getComponentCount();
      for(int r = 0; r < rows; r++) {
        for(int c = 0; c < cols; c++) {
          final int n = r * cols + c;
          if(n >= nr) return;

          final Component comp = cont.getComponent(n);
          final Dimension cs = comp.getPreferredSize();
          final int x = in.left + posX[c] + c * hgap, y = in.top + posY[r] + r * vgap;
          comp.setBounds(x, y, cs.width, cs.height);
        }
      }
    }
  }
}
