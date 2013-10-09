package org.basex.gui.view;

import java.awt.*;
import java.util.*;

import org.basex.gui.layout.*;

/**
 * This class contains a view reference and adds a {@link ViewMover}
 * on top of the view.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class ViewPanel extends BaseXBack implements ViewLayout {
  /** View reference. */
  private final View view;

  /**
   * Constructor.
   * @param v view to be stored
   */
  ViewPanel(final View v) {
    layout(new BorderLayout());
    add(new ViewMover(v.gui), BorderLayout.NORTH);
    add(v, BorderLayout.CENTER);
    view = v;
  }

  @Override
  public void setVisibility(final boolean db) {
    setVisible(view.visible() && (db || !view.db()));
  }

  @Override
  public boolean delete(final ViewPanel panel) {
    return this == panel;
  }

  @Override
  public void createView(final BaseXBack panel) {
    if(isVisible()) panel.add(this);
  }

  /**
   * Makes the view invisible.
   */
  public void delete() {
    final String name = "SHOW" + view.getName().toUpperCase(Locale.ENGLISH);
    view.gui.gopts.put(view.gui.gopts.option(name), false);
    view.gui.layoutViews();
  }

  @Override
  public String layoutString(final boolean all) {
    return all || isVisible() ? view.getName() + ' ' : "";
  }

  @Override
  public String toString() {
    return view.getName();
  }
}
