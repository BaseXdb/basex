package org.basex.gui.view;

import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Layout class.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class ViewAlignment implements ViewLayout {
  /** Alignment (horizontal/vertical). */
  final boolean horiz;
  /** Layout Components. */
  ViewLayout[] comp = {};

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
  private void remove(final int o) {
    comp = Array.delete(comp, o);
  }

  @Override
  public boolean isVisible() {
    for(final ViewLayout c : comp) if(c.isVisible()) return true;
    return false;
  }

  @Override
  public void setVisibility(final boolean db) {
    for(final ViewLayout c : comp) c.setVisibility(db);
  }

  @Override
  public boolean delete(final ViewPanel panel) {
    for(int c = 0; c < comp.length; c++) {
      if(comp[c].delete(panel)) {
        if(comp[c] instanceof ViewPanel) {
          remove(c--);
        } else {
          comp[c] = ((ViewAlignment) comp[c]).comp[0];
        }
      }
    }
    return comp.length < 2;
  }

  @Override
  public void createView(final BaseXBack panel) {
    // skip invisible layouts
    if(!isVisible()) return;

    final BaseXBack split = new BaseXSplit(horiz);
    for(final ViewLayout c : comp) c.createView(split);
    panel.add(split);
  }

  @Override
  public String layoutString(final boolean all) {
    final StringBuilder str = new StringBuilder(horiz ? "H " : "V ");
    for(final ViewLayout c : comp) str.append(c.layoutString(all));
    str.append("- ");
    return str.toString();
  }

  @Override
  public String toString() {
    return (horiz ? "Horizontal" : "Vertical") + " Layout";
  }
}
