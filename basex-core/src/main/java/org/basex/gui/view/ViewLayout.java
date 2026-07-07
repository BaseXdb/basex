package org.basex.gui.view;

import java.util.*;

import org.basex.gui.layout.*;

/**
 * Layout class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ViewLayout implements ViewComponent {
  /** Alignment (horizontal/vertical). */
  final boolean horizontal;
  /** Layout Components. */
  final ArrayList<ViewComponent> list = new ArrayList<>();
  /** Proportional size within the parent layout. */
  private double weight = 1;
  /** Callback that persists the layout after a resize (set by the container, propagated down). */
  Runnable persister;

  /**
   * Constructor.
   * @param horizontal alignment (horizontal/vertical)
   * @param components initial components
   */
  ViewLayout(final boolean horizontal, final ViewComponent... components) {
    this.horizontal = horizontal;
    list.addAll(Arrays.asList(components));
  }

  @Override
  public double weight() {
    return weight;
  }

  @Override
  public void weight(final double wght) {
    weight = wght;
  }

  /**
   * Adds a component.
   * @param comp component to be added
   */
  void add(final ViewComponent comp) {
    list.add(comp);
  }

  /**
   * Replaces a component.
   * @param comp component to be set
   * @param i index
   */
  void set(final ViewComponent comp, final int i) {
    list.set(i, comp);
  }

  /**
   * Inserts a component at the specified position.
   * @param comp component to insert
   * @param i index
   */
  void insert(final ViewComponent comp, final int i) {
    list.add(i, comp);
  }

  /**
   * Removes the specified panel.
   * @param panel panel to be removed
   * @return left over component, {@code null} otherwise
   */
  ViewComponent delete(final ViewPanel panel) {
    // number of components changes during iteration
    for(int c = 0; c < list.size(); c++) {
      ViewComponent comp = list.get(c);
      if(comp instanceof final ViewLayout vl) {
        comp = vl.delete(panel);
        if(comp != null) list.set(c, comp);
      } else if(comp == panel) {
        list.remove(c--);
      }
    }
    // return component if a single one is left over
    return list.size() == 1 ? list.getFirst() : null;
  }

  @Override
  public boolean isVisible() {
    for(final ViewComponent comp : list) {
      if(comp.isVisible()) return true;
    }
    return false;
  }

  @Override
  public void setVisibility(final boolean db) {
    for(final ViewComponent comp : list) comp.setVisibility(db);
  }

  @Override
  public void addTo(final BaseXBack panel) {
    // skip invisible layouts
    if(!isVisible()) return;

    // sum up the weights of all visible components
    double total = 0;
    int visible = 0;
    for(final ViewComponent comp : list) {
      if(comp.isVisible()) {
        total += comp.weight();
        ++visible;
      }
    }
    // add visible components and assign their proportional sizes
    final BaseXSplit sp = new BaseXSplit(horizontal);
    final double[] sizes = new double[visible];
    int s = 0;
    for(final ViewComponent comp : list) {
      if(comp.isVisible()) {
        // propagate the persister to nested layouts before they build their own splits
        if(comp instanceof final ViewLayout vl) vl.persister = persister;
        comp.addTo(sp);
        sizes[s++] = total > 0 ? comp.weight() / total : 1.0d / visible;
      }
    }
    sp.sizes(sizes);
    // keep the model in sync whenever the user drags a splitter of this level
    sp.resized(this::resize);
    panel.add(sp);
  }

  /**
   * Updates the component weights from the on-screen proportions after a splitter drag.
   * @param sizes on-screen proportions of the visible components (sum is 1.0)
   */
  private void resize(final double[] sizes) {
    double total = 0;
    for(final ViewComponent comp : list) {
      if(comp.isVisible()) total += comp.weight();
    }
    int s = 0;
    for(final ViewComponent comp : list) {
      if(comp.isVisible()) comp.weight(sizes[s++] * total);
    }
    // persist the updated layout
    if(persister != null) persister.run();
  }

  @Override
  public String layoutString(final boolean all) {
    final StringBuilder sb = new StringBuilder(horizontal ? "H " : "V ");
    if(all) sb.append(ViewComponent.format(weight)).append(' ');
    for(final ViewComponent comp : list) sb.append(comp.layoutString(all));
    return sb.append("- ").toString();
  }

  @Override
  public String toString() {
    return (horizontal ? "Horizontal" : "Vertical") + " Layout";
  }
}
