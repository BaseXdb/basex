package org.basex.gui.view;

import java.awt.BorderLayout;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;

/**
 * This class contains a view reference and adds a {@link ViewMover}
 * on top of the view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ViewPanel extends BaseXBack implements ViewLayout {
  /** View reference. */
  private final View view;

  /**
   * Constructor.
   * @param v view to be stored
   * @param name name of the view
   */
  public ViewPanel(final View v, final String name) {
    setLayout(new BorderLayout());
    add(new ViewMover(v.gui), BorderLayout.NORTH);
    add(v, BorderLayout.CENTER);
    view = v;
    view.setName(name);
  }

  /**
   * Sets the visibility of the view layout.
   */
  public void setVisibility() {
    try {
      setVisible(GUIProp.class.getField(prop()).getBoolean(null));
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Makes the view invisible.
   */
  public void delete() {
    try {
      GUIProp.class.getField(prop()).setBoolean(null, false);
      view.gui.layoutViews();
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Returns the name of the view property.
   * @return property
   */
  public String prop() {
    return "show" + (view.gui.context.db() ? "" : "start") + view;
  }

  /**
   * Checks if the specified panel equals the panel instance.
   * @param panel panel to be compared
   * @return result of check
   */
  public boolean delete(final ViewPanel panel) {
    return this == panel;
  }

  /**
   * Adds a layout to the specified panel.
   * @param panel current panel
   */
  public void createView(final BaseXBack panel) {
    if(isVisible()) panel.add(this);
  }

  /**
   * Constructs a build string.
   * @return build string
   */
  public String layoutString() {
    return view.getName() + " ";
  }

  @Override
  public String toString() {
    return view.getName();
  }
}
