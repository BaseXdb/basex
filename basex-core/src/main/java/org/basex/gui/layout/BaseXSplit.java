package org.basex.gui.layout;

import java.awt.*;
import java.util.function.*;

/**
 * Project specific Split panel implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXSplit extends BaseXBack implements LayoutManager {
  /** Layout: horizontal = true, vertical = false. */
  private final boolean horizontal;

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
  /** Resize listener. */
  private Consumer<double[]> resized;

  /** Index of the panel with a fixed pixel size ({@code -1}: proportional). */
  private int anchor = -1;
  /** Target pixel size of the anchored panel. */
  private int anchorSize;
  /** Minimum pixel size for the anchored panel and its neighbors. */
  private int anchorMin;

  /** Panels have actually been resized during the current drag. */
  private boolean dragged;

  /**
   * Constructor.
   * @param horizontal horizontal/vertical layout
   */
  public BaseXSplit(final boolean horizontal) {
    layout(this);
    this.horizontal = horizontal;
  }

  @Override
  public Component add(final Component comp) {
    if(getComponentCount() != 0) super.add(new BaseXSplitSep(horizontal));
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
   * Sets the proportional panel sizes (sum must be 1.0).
   * @param sizes proportional sizes
   */
  public void sizes(final double[] sizes) {
    propSize = sizes;
  }

  /**
   * Registers a listener.
   * @param listener resize listener
   */
  public void resized(final Consumer<double[]> listener) {
    resized = listener;
  }

  /**
   * Anchors a panel to a fixed pixel size.
   * @param index panel index to anchor
   * @param size target pixel size
   * @param min minimum pixel size
   */
  public void anchor(final int index, final int size, final int min) {
    anchor = index;
    anchorSize = size;
    anchorMin = min;
  }

  /**
   * Returns the current pixel size of the anchored panel.
   * @return pixel size
   */
  public int anchorSize() {
    return anchorSize;
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
    dragged = false;
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
    for(int n = 0; n < r - 1; ++n) {
      if(m[(n << 1) + 1] == sep) q = n + 1;
    }
    final double v = (dragPos - p) / (horizontal ? getWidth() : getHeight());
    final double min = anchor >= 0 ? (double) anchorMin / splitSize() : 0.0001;
    for(int i = 0; i < q; ++i) {
      if(dragSize[i] - v / q < min) return;
    }
    for(int i = q; i < r; ++i) {
      if(dragSize[i] + v / (r - q) < min) return;
    }
    for(int i = 0; i < q; ++i) propSize[i] = dragSize[i] - v / q;
    for(int i = q; i < r; ++i) propSize[i] = dragSize[i] + v / (r - q);
    dragged = true;
    revalidate();
  }

  /**
   * Finishes a splitter drag: snapshots the anchored pixel size and notifies the listener once.
   */
  void endDrag() {
    if(dragged) {
      // remember the dragged pixel size of the anchored panel
      if(anchor >= 0) {
        anchorSize = (int) Math.round(propSize[anchor] * splitSize());
      }
      if(resized != null) resized.accept(propSize);
    }
    dragged = false;
  }

  /**
   * Computes the proportional size of the anchored panel for the given available space.
   * The panel keeps its target pixel size, but shrinks down to {@code min} pixels (and no
   * further) when space runs short, always reserving {@code min} pixels for its neighbors.
   * @param target target pixel size
   * @param min minimum pixel size
   * @param size available space in pixels
   * @return proportional size (between 0 and 1)
   */
  static double anchorFraction(final int target, final int min, final int size) {
    if(size <= 0) return 0;
    int a = Math.min(target, size - min);
    a = Math.max(a, Math.min(min, size));
    return (double) a / size;
  }

  /**
   * Returns the available space for the panels (total size minus the visible separators).
   * @return size in pixels
   */
  private int splitSize() {
    int seps = propSize.length - 1;
    for(final double d : propSize) {
      if(d == 0) --seps;
    }
    return (horizontal ? getWidth() : getHeight()) - seps * SEPARATOR_SIZE;
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
        if((c & 1) == 0) propSize[c >> 1] = 1.0d / panels;
      }
    }
    // count number of invisible panels
    int c = panels - 1;
    for(final double d : propSize) {
      if(d == 0) c--;
    }

    // set bounds of all components
    final int sz = (horizontal ? w : h) - c * SEPARATOR_SIZE;

    // enforce a fixed pixel size for the anchored panel (skip while hidden or dragging)
    if(anchor >= 0 && !dragged && propSize[anchor] != 0 && sz > 0) {
      final double frac = anchorFraction(anchorSize, anchorMin, sz);
      // distribute the remaining space among the other panels, keeping their relative sizes
      double rest = 0;
      for(int i = 0; i < propSize.length; i++) {
        if(i != anchor) rest += propSize[i];
      }
      for(int i = 0; i < propSize.length; i++) {
        propSize[i] = i == anchor ? frac :
          rest > 0 ? propSize[i] / rest * (1 - frac) : (1 - frac) / (propSize.length - 1);
      }
    }

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
        size = invisible ? 0 : SEPARATOR_SIZE;
      }
      final int pos = (int) posD;
      if(horizontal) {
        comps[c].setBounds(pos, 0, size, h);
      } else {
        comps[c].setBounds(0, pos, w, size);
      }
      posD += size;
    }
  }
}
