package org.basex.gui.layout;

import java.awt.*;

/**
 * Project specific Split panel implementation.
 *
 * @author BaseX Team 2005-15, BSD License
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
  /** Proportions of visible panels. */
  private double[] hiddenSize;
  /** Cached sizes (when panel is hidden). */
  private double[] cachedSize;

  /**
   * Constructor.
   * @param horizontal horizontal/vertical layout
   */
  public BaseXSplit(final boolean horizontal) {
    layout(this);
    horiz = horizontal;
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
   * Sets initial panel sizes (sum must be 1.0).
   * @param vis visible sizes
   * @param hidden hidden sizes
   */
  public void init(final double[] vis, final double[] hidden) {
    propSize = vis;
    hiddenSize = hidden;
  }

  /**
   * Sets proportional panel sizes (sum must be 1.0).
   * @param show show/hide flag
   */
  public void visible(final boolean show) {
    boolean s = true;
    if(propSize != null) {
      for(final double d : propSize) s &= d != 0;
    }
    if(propSize == null || s ^ show) {
      // change state
      if(show) {
        propSize = cachedSize;
      } else {
        cachedSize = propSize;
        propSize = hiddenSize;
      }
      revalidate();
    }
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
    if(dragSize == null) startDrag(p);

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
    final Component[] comps = getComponents();
    final int cl = comps.length;
    final int w = getWidth(), h = getHeight();
    final int panels = comps.length + 1 >> 1;

    // calculate proportional size of panels
    if(propSize == null) {
      propSize = new double[panels];
      for(int c = 0; c < cl; ++c) {
        if((c & 1) == 0) propSize[c >> 1] = 1d / panels;
      }
    }
    // count number of invisible panels
    int c = panels - 1;
    for(final double d : propSize) if(d == 0) c--;

    // set bounds of all components
    final int sz = (horiz ? w : h) - c * BaseXSplitSep.SIZE;
    double posD = 0;
    boolean invisible = false;
    for(c = 0; c < cl; c++) {
      final int size;
      if((c & 1) == 0) {
        // panel
        size = (int) (propSize[c >> 1] * sz);
        invisible = size == 0;
      } else {
        // splitter: hide when last panel was invisible
        size = invisible ? 0 : BaseXSplitSep.SIZE;
      }
      final int pos = (int) posD;
      if(horiz) {
        comps[c].setBounds(pos, 0, size, h);
      } else {
        comps[c].setBounds(0, pos, w, size);
      }
      posD += size;
    }
  }
}
