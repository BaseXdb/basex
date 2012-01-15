package org.basex.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * Project specific Split panel implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXSplit extends BaseXBack implements LayoutManager {
  /** Layout: horizontal = true, vertical = false. */
  private final boolean l;
  /** Panel positions. */
  private double[] s;
  /** Temporary panel positions. */
  private double[] t;
  /** Temporary drag position. */
  private double d;

  /**
   * Constructor.
   * @param lay layout: horizontal = true, vertical = false
   */
  public BaseXSplit(final boolean lay) {
    layout(this);
    l = lay;
  }

  @Override
  public Component add(final Component comp) {
    if(getComponentCount() != 0) super.add(new BaseXSplitSep(l));
    super.add(comp);
    s = null;
    return comp;
  }

  @Override
  public void removeAll() {
    super.removeAll();
    s = null;
  }

  /**
   * Starts split pane dragging.
   * @param p position
   */
  void startDrag(final double p) {
    d = p;
    t = s.clone();
  }

  /**
   * Reacts on splitter drags.
   * @param sep separator
   * @param p current position
   */
  void drag(final BaseXSplitSep sep, final double p) {
    final Component[] m = getComponents();
    final int r = s.length;
    int q = 0;
    for(int n = 0; n < r - 1; ++n) if(m[(n << 1) + 1] == sep) q = n + 1;
    final double v = (d - p) / (l ? getWidth() : getHeight());
    for(int i = 0; i < q; ++i) if(t[i] - v / q < .0001) return;
    for(int i = q; i < r; ++i) if(t[i] + v / (r - q) < .0001) return;
    for(int i = 0; i < q; ++i) s[i] = t[i] - v / q;
    for(int i = q; i < r; ++i) s[i] = t[i] + v / (r - q);
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
      final int h = getHeight();
      final int w = getWidth();
      final int m = c.length + 1 >> 1;
      final double p = (l ? w : h) - (m - 1) * BaseXSplitSep.SIZE;

      final boolean a = s == null;
      if(a) s = new double[m];

      double v = 0;
      for(int n = 0; n < c.length; ++n) {
        final boolean b = (n & 1) == 0;
        double z = BaseXSplitSep.SIZE;
        if(b) z = s[n >> 1] == 0 ? (int) (p / m) : s[n >> 1] * p;
        final int y = (int) v;
        c[n].setBounds(l ? y : 0, l ? 0 : y, l ? (int) z : w, l ? h : (int) z);
        if(a && b) s[n >> 1] = z / p;
        v += z;
      }
  }
}
