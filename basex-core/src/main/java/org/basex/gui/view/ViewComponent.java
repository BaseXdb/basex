package org.basex.gui.view;

import org.basex.gui.layout.*;

/**
 * This is an interface for view layout components.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
interface ViewComponent {
  /**
   * Checks if the view layout is visible.
   * @return true if layout is visible
   */
  boolean isVisible();

  /**
   * Sets the visibility of the view layout.
   * @param db database flag
   */
  void setVisibility(boolean db);

  /**
   * Adds a layout to the specified panel.
   * @param panel current panel
   */
  void addTo(BaseXBack panel);

  /**
   * Constructs a build string.
   * @param all also include invisible components
   * @return build string
   */
  String layoutString(boolean all);
}
