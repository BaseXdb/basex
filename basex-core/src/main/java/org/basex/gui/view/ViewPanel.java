package org.basex.gui.view;

import java.awt.*;
import java.util.*;

import org.basex.gui.layout.*;
import org.basex.util.options.*;

/**
 * This class contains a view and a {@link ViewMover} on top.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ViewPanel extends BaseXBack implements ViewComponent {
  /** View reference. */
  private final View view;

  /**
   * Constructor.
   * @param view view to be stored
   */
  ViewPanel(final View view) {
    this.view = view;
    layout(new BorderLayout());
    add(new ViewMover(view.gui), BorderLayout.NORTH);
    add(view, BorderLayout.CENTER);
  }

  @Override
  public void setVisibility(final boolean db) {
    setVisible(view.visible() && (db || !view.db()));
  }

  @Override
  public void addTo(final BaseXBack panel) {
    if(isVisible()) panel.add(this);
  }

  /**
   * Makes the view invisible.
   */
  public void delete() {
    final String name = "SHOW" + view.getName().toUpperCase(Locale.ENGLISH);
    view.gui.gopts.set((BooleanOption) view.gui.gopts.option(name), false);
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
