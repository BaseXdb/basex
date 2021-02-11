package org.basex.gui.layout;

import java.awt.*;

/**
 * This LayoutManager is a simplified version of the the TableLayout. The added components
 * will be rendered as rows.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RowLayout implements LayoutManager {
  /** Gap between components. */
  private final int gap;

  /**
   * Default constructor.
   */
  public RowLayout() {
    this(0);
  }

  /**
   * Constructor, specifying gap between components.
   * @param gap gap
   */
  public RowLayout(final int gap) {
    this.gap = gap;
  }

  @Override
  public void addLayoutComponent(final String name, final Component comp) { }

  @Override
  public void removeLayoutComponent(final Component comp) { }

  @Override
  public Dimension preferredLayoutSize(final Container cont) {
    synchronized(cont.getTreeLock()) {
      int w = 0, h = 0;
      for(final Component comp : cont.getComponents()) {
        final Dimension d = comp.getPreferredSize();
        w = Math.max(w, d.width);
        h += gap + d.height;
      }
      final Insets in = cont.getInsets();
      return new Dimension(in.left + w + in.right, in.top + Math.max(h - gap, 0) + in.bottom);
    }
  }

  @Override
  public Dimension minimumLayoutSize(final Container parent) {
    return preferredLayoutSize(parent);
  }

  @Override
  public void layoutContainer(final Container cont) {
    synchronized(cont.getTreeLock()) {
      int y = 0;
      final Insets in = cont.getInsets();
      for(final Component comp : cont.getComponents()) {
        final Dimension d = comp.getPreferredSize();
        comp.setBounds(in.left, in.top + y, d.width, d.height);
        y += gap + d.height;
      }
    }
  }
}
