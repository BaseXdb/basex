package org.basex.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * This LayoutManager is similar to the GridLayout. The added components
 * keep their minimum size even when the parent container is resized.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TableLayout implements LayoutManager {
  /** Number of columns. */
  private int cols;
  /** Number of rows. */
  private int rows;
  /** Horizontal inset. */
  private int insetX;
  /** Vertical inset. */
  private int insetY;
  /** Panel width. */
  private int width;
  /** Panel height. */
  private int height;
  /** Horizontal position. */
  private int[] posX;
  /** Vertical position. */
  private int[] posY;

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
   * @param ix horizontal inset size
   * @param iy vertical inset size
   */
  public TableLayout(final int r, final int c, final int ix, final int iy) {
    rows = r;
    cols = c;
    insetX = ix;
    insetY = iy;
    posX = new int[c];
    posY = new int[r];
  }

  /**
   * Adds the specified component with the specified name to the layout.
   * @param name the component name
   * @param comp the component to be added
   */
  public void addLayoutComponent(final String name, final Component comp) { }

  /**
   * Removes the specified component from the layout.
   * @param comp the component to be removed.
   */
  public void removeLayoutComponent(final Component comp) { }

  /**
   * Determines the preferred size of the container argument using this grid
   * layout.
   * @param parent the layout container
   * @return the preferred dimensions for painting the container
   */
  public Dimension preferredLayoutSize(final Container parent) {
    synchronized(parent.getTreeLock()) {
      final Insets insets = parent.getInsets();
      final int nrComponents = parent.getComponentCount();

      int maxW = 0;
      int maxH = 0;
      for(int i = 0; i < cols; i++) {
        posX[i] = maxW;
        final int w = maxW;
        int h = 0;

        for(int j = 0; j < rows; j++) {
          final int n = j * cols + i;
          if(n >= nrComponents) break;

          final Component comp = parent.getComponent(n);
          final Dimension dim = comp.getPreferredSize();

          if(maxW < w + dim.width) maxW = w + dim.width;

          if(posY[j] < h) posY[j] = h;
          else h = posY[j];

          h += dim.height;
        }
        if(maxH < h) maxH = h;
      }
      width = insets.left + maxW + (cols - 1) * insetX + insets.right;
      height = insets.top + maxH + (rows - 1) * insetY + insets.bottom;

      return new Dimension(width, height);
    }
  }

  /**
   * Determines the minimum size of the container argument using this grid
   * layout.
   * @param parent the layout container
   * @return the preferred dimensions for painting the container
   */
  public Dimension minimumLayoutSize(final Container parent) {
    return preferredLayoutSize(parent);
  }

  /**
   * Lays out the specified container using this layout.
   * @param parent the layout container
   */
  public void layoutContainer(final Container parent) {
    preferredLayoutSize(parent);

    synchronized(parent.getTreeLock()) {
      final Insets insets = parent.getInsets();
      final int nrComponents = parent.getComponentCount();
      for(int j = 0; j < rows; j++) {
        for(int i = 0; i < cols; i++) {
          final int n = j * cols + i;
          if(n >= nrComponents) return;

          final Dimension compSize = parent.getComponent(n).getPreferredSize();

          final int x = insets.left + posX[i] + i * insetX;
          final int y = insets.top + posY[j] + j * insetY;
          final int w = compSize.width > 0 ? compSize.width :
            width - insets.left - insets.right;
          final int h = compSize.height > 0 ? compSize.height :
            height - insets.top - insets.bottom;
          parent.getComponent(n).setBounds(x, y, w, h);
        }
      }
    }
  }
}
