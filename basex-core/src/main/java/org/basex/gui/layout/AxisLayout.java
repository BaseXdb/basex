package org.basex.gui.layout;

import java.awt.*;

/**
 * Layout manager that arranges components in a single row or column.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class AxisLayout implements LayoutManager {
  /** Gap between components. */
  private final int gap;
  /** Horizontal orientation. */
  private final boolean horizontal;

  /**
   * Constructor.
   * @param gap gap between components
   * @param horizontal horizontal orientation
   */
  AxisLayout(final int gap, final boolean horizontal) {
    this.gap = gap;
    this.horizontal = horizontal;
  }

  @Override
  public final void addLayoutComponent(final String name, final Component comp) { }

  @Override
  public final void removeLayoutComponent(final Component comp) { }

  @Override
  public final Dimension preferredLayoutSize(final Container cont) {
    synchronized(cont.getTreeLock()) {
      int main = 0, cross = 0;
      for(final Component comp : cont.getComponents()) {
        final Dimension d = comp.getPreferredSize();
        main += gap + (horizontal ? d.width : d.height);
        cross = Math.max(cross, horizontal ? d.height : d.width);
      }
      main = Math.max(main - gap, 0);
      final Insets in = cont.getInsets();
      return new Dimension(in.left + (horizontal ? main : cross) + in.right,
          in.top + (horizontal ? cross : main) + in.bottom);
    }
  }

  @Override
  public final Dimension minimumLayoutSize(final Container parent) {
    return preferredLayoutSize(parent);
  }

  @Override
  public final void layoutContainer(final Container cont) {
    synchronized(cont.getTreeLock()) {
      int pos = 0;
      final Insets in = cont.getInsets();
      for(final Component comp : cont.getComponents()) {
        final Dimension d = comp.getPreferredSize();
        comp.setBounds(in.left + (horizontal ? pos : 0), in.top + (horizontal ? 0 : pos),
            d.width, d.height);
        pos += gap + (horizontal ? d.width : d.height);
      }
    }
  }
}
