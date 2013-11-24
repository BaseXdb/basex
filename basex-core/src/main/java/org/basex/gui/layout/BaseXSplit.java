package org.basex.gui.layout;

import java.awt.*;

/**
 * Project specific Split panel implementation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class BaseXSplit extends BaseXBack implements LayoutManager {
  /** Layout: horizontal = true, vertical = false. */
  private final boolean horiz;
  /** Proportional panel sizes. */
  private double[] propSize;
  /** Panel positions; assigned when a drag operation starts. */
  private double[] dragSize;
  /** Current drag position. */
  private double dragPos;

  /**
   * Constructor.
   * @param lay layout: horizontal = true, vertical = false
   */
  public BaseXSplit(final boolean lay) {
    layout(this);
    horiz = lay;
  }

  @Override
  public Component add(final Component comp) {
    if(getComponentCount() != 0) super.add(new BaseXSplitSep(horiz));
    super.add(comp);
    propSize = null;
    return comp;
  }

  @Override
  public void removeAll() {
    super.removeAll();
    propSize = null;
  }

  /**
   * Sets proportional panel size (sum must be 1.0).
   * @param sz sizes
   * @return old sizes
   */
  public double[] sizes(final double[] sz) {
    final double[] old = propSize;
    propSize = sz;
    revalidate();
    return old;
  }

  /**
   * Starts split pane dragging.
   * @param p position
   */
  void startDrag(final double p) {
    dragPos = p;
    dragSize = propSize.clone();
  }

  /**
   * Reacts on splitter drags.
   * @param sep separator
   * @param p current position
   */
  void drag(final BaseXSplitSep sep, final double p) {
    final Component[] m = getComponents();
    final int r = propSize.length;
    int q = 0;
    for(int n = 0; n < r - 1; ++n) if(m[(n << 1) + 1] == sep) q = n + 1;
    final double v = (dragPos - p) / (horiz ? getWidth() : getHeight());
    for(int i = 0; i < q; ++i) if(dragSize[i] - v / q < .0001) return;
    for(int i = q; i < r; ++i) if(dragSize[i] + v / (r - q) < .0001) return;
    for(int i = 0; i < q; ++i) propSize[i] = dragSize[i] - v / q;
    for(int i = q; i < r; ++i) propSize[i] = dragSize[i] + v / (r - q);
    revalidate();
  }

  @Override
  public void addLayoutComponent(final String name, final Component comp) { }

  @Override
  public void removeLayoutComponent(final Component comp) { }

  @Override
  public Dimension preferredLayoutSize(final Container parent) {
    return getSize();
  }

  @Override
  public Dimension minimumLayoutSize(final Container parent) {
    return preferredLayoutSize(parent);
  }

  @Override
  public void layoutContainer(final Container parent) {
    final Component[] c = getComponents();
    final int h = getHeight(), w = getWidth();
    final double m = c.length + 1 >> 1;

    // calculate proportional size of panels
    if(propSize == null) {
      propSize = new double[(int) m];
      for(int n = 0; n < c.length; ++n) {
        if((n & 1) == 0) propSize[n >> 1] = 1 / m;
      }
    }

    // set bounds
    final double sz = (horiz ? w : h) - (m - 1) * BaseXSplitSep.SIZE;
    double v = 0;
    for(int n = 0; n < c.length; ++n) {
      final boolean sep = (n & 1) != 0;
      final int z = sep ? BaseXSplitSep.SIZE : (int) (propSize[n >> 1] * sz);
      final int y = (int) v;
      if(horiz) {
        c[n].setBounds(y, 0, z, h);
      } else {
        c[n].setBounds(0, y, w, z);
      }
      v += z;
    }
  }
}
