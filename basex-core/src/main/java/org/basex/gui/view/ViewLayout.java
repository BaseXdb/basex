package org.basex.gui.view;

import java.util.*;

import org.basex.gui.layout.*;

/**
 * Layout class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ViewLayout implements ViewComponent {
  /** Alignment (horizontal/vertical). */
  final boolean horizontal;
  /** Layout Components. */
  final ArrayList<ViewComponent> list = new ArrayList<>();

  /**
   * Constructor.
   * @param horizontal alignment (horizontal/vertical)
   * @param components initial components
   */
  ViewLayout(final boolean horizontal, final ViewComponent... components) {
    this.horizontal = horizontal;
    list.addAll(Arrays.asList(components));
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
      if(comp instanceof ViewLayout) {
        comp = ((ViewLayout) comp).delete(panel);
        if(comp != null) list.set(c, comp);
      } else if(comp == panel) {
        list.remove(c--);
      }
    }
    // return component if a single one is left over
    return list.size() == 1 ? list.get(0) : null;
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

    final BaseXBack split = new BaseXSplit(horizontal);
    for(final ViewComponent comp : list) comp.addTo(split);
    panel.add(split);
  }

  @Override
  public String layoutString(final boolean all) {
    final StringBuilder sb = new StringBuilder(horizontal ? "H " : "V ");
    for(final ViewComponent comp : list) sb.append(comp.layoutString(all));
    return sb.append("- ").toString();
  }

  @Override
  public String toString() {
    return (horizontal ? "Horizontal" : "Vertical") + " Layout";
  }
}
