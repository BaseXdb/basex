package org.basex.gui.view;

import org.basex.gui.layout.BaseXBack;

/**
 * This is an interface for view layout components.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
interface ViewLayout {
  /**
   * Checks if the view layout is visible.
   * @return true if layout is visible
   */
  boolean isVisible();

  /**
   * Sets the visibility of the view layout.
   */
  void setVisibility();

  /**
   * Constructs a build string.
   * @return build string
   */
  String layoutString();

  /**
   * Removes the specified panel.
   * @param panel panel to be removed
   * @return true if panel was removed
   */
  boolean remove(ViewPanel panel);

  /**
   * Adds a layout to the specified panel.
   * @param panel current panel
   */
  void createView(BaseXBack panel);
}
