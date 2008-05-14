package org.basex.gui.view;

import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import org.basex.gui.GUI;
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
    add(new ViewMover(), BorderLayout.NORTH);
    add(v, BorderLayout.CENTER);
    view = v;
    view.setName(name);
  }

  /**
   * Sets the visibility of the view layout.
   */
  public void setVisibility() {
    final boolean db = GUI.context.db();
    final String name = view.getName();
    if(name.equals(MAPVIEW))        setVisible(GUIProp.showmap);
    else if(name.equals(TREEVIEW))  setVisible(GUIProp.showtree);
    else if(name.equals(TABLEVIEW)) setVisible(GUIProp.showtable);
    else if(name.equals(INFOVIEW))  setVisible(GUIProp.showinfo);
    else if(name.equals(QUERYVIEW)) setVisible(GUIProp.showquery);
    else if(name.equals(REALVIEW))  setVisible(GUIProp.showreal);
    else if(name.equals(XPATHVIEW))  setVisible(GUIProp.showxpath);
    else if(name.equals(HELPVIEW))  setVisible(db ?
        GUIProp.showhelp : GUIProp.showstarthelp);
    else if(name.equals(TEXTVIEW))  setVisible(db ?
        GUIProp.showtext : GUIProp.showstarttext);
  }

  /**
   * Makes the view invisible.
   */
  public void remove() {
    final boolean db = GUI.context.db();
    final String name = view.getName();
    if(name.equals(MAPVIEW)) {
      GUIProp.showmap = false;
    } else if(name.equals(TREEVIEW)) {
      GUIProp.showtree = false;
    } else if(name.equals(TABLEVIEW)) {
      GUIProp.showtable = false;
    } else if(name.equals(INFOVIEW)) {
      GUIProp.showinfo = false;
    } else if(name.equals(QUERYVIEW)) {
      GUIProp.showquery = false;
    } else if(name.equals(REALVIEW)) {
      GUIProp.showreal = false;
    } else if(name.equals(XPATHVIEW)) {
      GUIProp.showxpath = false;
    } else if(name.equals(TEXTVIEW)) {
      if(db) GUIProp.showtext = false;
      else GUIProp.showstarttext = false;
    } else if(name.equals(HELPVIEW)) {
      if(db) GUIProp.showhelp = false;
      else GUIProp.showstarthelp = false;
    }
    GUI.get().layoutViews();
  }

  /**
   * Checks if the specified panel equals the panel instance.
   * @param panel panel to be compared
   * @return result of check
   */
  public boolean remove(final ViewPanel panel) {
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
