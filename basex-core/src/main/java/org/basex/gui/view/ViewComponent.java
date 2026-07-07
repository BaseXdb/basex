package org.basex.gui.view;

import org.basex.gui.layout.*;

/**
 * This is an interface for view layout components.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
interface ViewComponent {
  /**
   * Returns the proportional size (weight) of this component within its parent layout.
   * @return weight
   */
  double weight();

  /**
   * Assigns the proportional size (weight) of this component within its parent layout.
   * @param weight weight
   */
  void weight(double weight);

  /**
   * Formats a weight for the layout string (rounded to four decimal digits).
   * @param weight weight
   * @return string representation
   */
  static String format(final double weight) {
    return Double.toString(Math.round(weight * 10_000) / 10_000.0);
  }

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
