package org.basex.gui.view;

import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXSplit;

/**
 * Layout class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class ViewAlignment implements ViewLayout {
  /** Alignment (horizontal/vertical). */
  boolean horiz;
  /** Layout Components. */
  ViewLayout[] comp = new ViewLayout[0];

  /**
   * Constructor.
   * @param h alignment (horizontal/vertical)
   */
  ViewAlignment(final boolean h) {
    horiz = h;
  }

  /**
   * Adds a single object.
   * @param obj object to be added
   */
  void add(final ViewLayout obj) {
    add(obj, comp.length);
  }

  /**
   * Adds a single object at the specified position.
   * @param obj object to insert
   * @param o index
   */
  void add(final ViewLayout obj, final int o) {
    final int n = comp.length;
    final ViewLayout[] tmp = new ViewLayout[n + 1];

    System.arraycopy(comp, 0, tmp, 0, o);
    System.arraycopy(comp, o, tmp, o + 1, n - o);
    comp = tmp;
    comp[o] = obj;
  }

  /**
   * Removes a single object.
   * @param o index
   */
  void remove(final int o) {
    final int n = comp.length - 1;
    final ViewLayout[] tmp = new ViewLayout[n];
    System.arraycopy(comp, 0, tmp, 0, o);
    System.arraycopy(comp, o + 1, tmp, o, n - o);
    comp = tmp;
  }

  /**
   * Checks if the view layout is visible.
   * @return true if layout is visible
   */
  public boolean isVisible() {
    for(final ViewLayout c : comp) if(c.isVisible()) return true;
    return false;
  }

  /**
   * Sets the visibility of the views to the property values.
   */
  public void setVisibility() {
    for(final ViewLayout c : comp) c.setVisibility();
  }

  /**
   * Deletes the specified panel.
   * @param panel panel to be removed
   * @return true if only one view is left in the current level
   */
  public boolean delete(final ViewPanel panel) {
    for(int o = 0; o < comp.length; o++) {
      if(comp[o].delete(panel)) {
        if(comp[o] instanceof ViewPanel) {
          remove(o--);
        } else {
          comp[o] = ((ViewAlignment) comp[o]).comp[0];
        }
      }
    }
    return comp.length < 2;
  }

  /**
   * Adds a layout to the specified panel.
   * @param panel current panel
   */
  public void createView(final BaseXBack panel) {
    // skip invisible layouts
    if(!isVisible()) return;

    final BaseXBack split = new BaseXSplit(horiz);
    for(final ViewLayout c : comp) c.createView(split);
    panel.add(split);
  }

  /**
   * Constructs a build string.
   * @return build string
   */
  public String layoutString() {
    final StringBuilder str = new StringBuilder(horiz ? "H " : "V ");
    for(final ViewLayout c : comp) str.append(c.layoutString());
    str.append("- ");
    return str.toString();
  }

  @Override
  public String toString() {
    return (horiz ? "Horizontal" : "Vertical") + " Layout";
  }
}
