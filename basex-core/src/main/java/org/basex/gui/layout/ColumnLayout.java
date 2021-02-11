package org.basex.gui.layout;

import java.awt.*;

/**
 * This LayoutManager is a simplified version of the the TableLayout. The added components
 * will be rendered as columns.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ColumnLayout implements LayoutManager {
  /** Gap between components. */
  private final int gap;

  /**
   * Default constructor.
   */
  public ColumnLayout() {
    this(0);
  }

  /**
   * Constructor, specifying gap between components.
   * @param gap gap
   */
  public ColumnLayout(final int gap) {
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
        w += gap + d.width;
        h = Math.max(h, d.height);
      }
      final Insets in = cont.getInsets();
      return new Dimension(in.left + Math.max(w - gap, 0) + in.right, in.top + h + in.bottom);
    }
  }

  @Override
  public Dimension minimumLayoutSize(final Container parent) {
    return preferredLayoutSize(parent);
  }

  @Override
  public void layoutContainer(final Container cont) {
    synchronized(cont.getTreeLock()) {
      int x = 0;
      final Insets in = cont.getInsets();
      for(final Component comp : cont.getComponents()) {
        final Dimension d = comp.getPreferredSize();
        comp.setBounds(in.left + x, in.top, d.width, d.height);
        x += gap + d.width;
      }
    }
  }
}
