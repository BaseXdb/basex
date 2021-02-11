package org.basex.gui.view;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.util.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * This class manages all visible and invisible views and allows drag and
 * drop operations inside the panel.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ViewContainer extends BaseXBack {
  /** Dragging stroke. */
  private static final BasicStroke STROKE = new BasicStroke(2);

  /** Orientation enumerator. */
  private enum Location {
    /** North orientation. */ NORTH,
    /** West orientation.  */ WEST,
    /** East orientation.  */ EAST,
    /** South orientation. */ SOUTH
  }

  /** Reference to the main window. */
  private final GUI gui;
  /** View panels. */
  private final ViewPanel[] views;
  /** Temporary rectangle position. */
  private final int[] pos = new int[4];
  /** Logo. */
  private final Image logo;

  /** View Layout. */
  private ViewLayout layout;
  /** Current layout string. */
  private String layoutString;
  /** Source View. */
  private ViewPanel source;
  /** Target View. */
  private ViewPanel target;
  /** Target orientation. */
  private Location location;
  /** Temporary mouse position. */
  private Point sp;

  /**
   * Constructor.
   * @param gui reference to the main window
   * @param view view panels
   */
  public ViewContainer(final GUI gui, final View... view) {
    layout(new BorderLayout());
    setBackground(BACK);

    this.gui = gui;
    logo = BaseXImages.get("logo_256");

    final int vl = view.length;
    views = new ViewPanel[vl];
    for(int v = 0; v < vl; ++v) views[v] = new ViewPanel(view[v]);

    // build layout or use default if something goes wrong
    if(!buildLayout(gui.gopts.get(GUIOptions.VIEWS)) && !buildLayout(VIEWS)) {
      Util.errln(Util.className(this) + ": could not build layout.");
    }
  }

  /**
   * Updates and validates the views.
   */
  public void updateViews() {
    // update visibility of all components
    layout.setVisibility(gui.context.data() != null);

    // check if the layout of visible components has changed
    final String ls = layout.layoutString(false);
    if(ls.equals(layoutString)) return;

    // remember new layout
    gui.gopts.set(GUIOptions.VIEWS, layout.layoutString(true));
    layoutString = ls;

    // rebuild views
    removeAll();
    layout.addTo(this);
    validate();
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    if(getComponentCount() == 0) g.drawImage(logo, (getWidth() - logo.getWidth(this)) / 2,
        (getHeight() - logo.getHeight(this)) / 2, this);
  }

  /**
   * Drags the current view.
   * @param panel panel to be dragged
   * @param p absolute mouse position
   */
  void dragPanel(final ViewPanel panel, final Point p) {
    source = panel;
    sp = p;
    calc();
    repaint();
  }

  /**
   * Drops a view and reorganizes the layout.
   */
  void dropPanel() {
    if(source == null) return;

    if(location != null) {
      final ViewComponent comp = layout.delete(source);
      if(comp instanceof ViewLayout) layout = (ViewLayout) comp;

      if(target == null) layout = addView(layout);
      else add(layout);
      updateViews();
    }
    source = null;
    repaint();
  }

  /**
   * Adds the dragged view after if it has been removed.
   * @param vl layout instance
   * @return {@code true} if component was successfully added
   */
  private boolean add(final ViewLayout vl) {
    for(int c = 0; c < vl.list.size(); c++) {
      final ViewComponent comp = vl.list.get(c);
      if(comp instanceof ViewLayout) {
        if(add((ViewLayout) comp)) return true;
      } else if(comp == target) {
        final boolean west = location == Location.WEST, east = location == Location.EAST;
        if(location == Location.NORTH || west) {
          if(vl.horizontal == west) {
            vl.insert(source, c);
          } else {
            vl.set(new ViewLayout(west, source, target), c);
          }
        } else if(location == Location.SOUTH || east) {
          if(vl.horizontal == east) {
            vl.insert(source, c + 1);
          } else {
            vl.set(new ViewLayout(east, target, source), c);
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Adds the dragged view into the specified layout instance.
   * @param vl layout instance
   * @return resulting layout
   */
  private ViewLayout addView(final ViewLayout vl) {
    final boolean west = location == Location.WEST, east = location == Location.EAST;
    ViewLayout l = vl;
    if(location == Location.NORTH || west) {
      if(l.horizontal == west) {
        l.insert(source, 0);
      } else {
        l = new ViewLayout(west, source, l);
      }
    } else if(location == Location.SOUTH || east) {
      if(l.horizontal == east) {
        l.add(source);
      } else {
        l = new ViewLayout(east, l, source);
      }
    }
    return l;
  }

  /**
   * Finds the view under the mouse position and returns it as possible
   * target for dropping the dragged view.
   * @return view container or {@code null}
   */
  private ViewPanel getTarget() {
    for(final ViewPanel view : views) {
      if(view.isVisible() && new Rectangle(absLoc(view), view.getSize()).contains(sp))
        return view;
    }
    return null;
  }

  /**
   * Calculates the absolute location for the specified point and component.
   * @param comp component
   * @return absolute location
   */
  private Point absLoc(final Component comp) {
    Component c = comp;
    final Point p = c.getLocation();

    do {
      c = c.getParent();
      p.x += c.getX();
      p.y += c.getY();
    } while(c.getParent() != this);
    return p;
  }

  @Override
  public void paint(final Graphics g) {
    super.paint(g);
    if(source == null) return;

    ((Graphics2D) g).setStroke(STROKE);
    if(location != null) {
      g.setColor(dgray);
      g.drawRect(pos[0], pos[1], pos[2] - 1, pos[3] - 1);
      ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g.setColor(lgray);
      g.fillRect(pos[0], pos[1], pos[2], pos[3]);
    }
  }

  /**
   * Calculates the target position.
   */
  private void calc() {
    final int hh = getHeight();
    final int ww = getWidth();

    pos[0] = 1;
    pos[1] = 1;
    pos[2] = ww - 2;
    pos[3] = hh - 2;

    location = null;
    target = getTarget();

    // paint panel which is currently moved somewhere else
    if(target != null && target != source) {
      final Rectangle tr = new Rectangle(absLoc(target), target.getSize());
      final int minx = tr.width >> 1;
      final int miny = tr.height >> 1;

      if(Math.abs(tr.x + tr.width / 2 - sp.x) < tr.width / 3) {
        if(sp.y > tr.y && sp.y < tr.y + miny) {
          pos[0] = tr.x;
          pos[1] = tr.y;
          pos[2] = tr.width;
          pos[3] = miny;
          location = Location.NORTH;
        } else if(sp.y > tr.y + tr.height - miny && sp.y < tr.y + tr.height) {
          pos[0] = tr.x;
          pos[1] = tr.y + tr.height - miny;
          pos[2] = tr.width;
          pos[3] = miny;
          location = Location.SOUTH;
        }
      } else if(Math.abs(tr.y + tr.height / 2 - sp.y) < tr.height / 3) {
        if(sp.x > tr.x && sp.x < tr.x + minx) {
          pos[0] = tr.x;
          pos[1] = tr.y;
          pos[2] = minx;
          pos[3] = tr.height;
          location = Location.WEST;
        } else if(sp.x > tr.x + tr.width - minx && sp.x < tr.x + tr.width) {
          pos[0] = tr.x + tr.width - minx;
          pos[1] = tr.y;
          pos[2] = minx;
          pos[3] = tr.height;
          location = Location.EAST;
        }
      }
    }

    if(location == null) {
      final int minx = ww >> 2;
      final int miny = hh >> 2;
      target = null;
      if(sp.y < miny) {
        pos[3] = miny;
        location = Location.NORTH;
      } else if(sp.y > hh - miny) {
        pos[3] = miny;
        pos[1] = hh - miny;
        location = Location.SOUTH;
      } else if(sp.x < minx) {
        pos[2] = minx;
        location = Location.WEST;
      } else if(sp.x > ww - minx) {
        pos[2] = minx;
        pos[0] = ww - minx;
        location = Location.EAST;
      }
    }
  }

  /**
   * Builds the view layout by parsing the layout string.
   * @param string layout string
   * @return true if everything went alright
   */
  private boolean buildLayout(final String string) {
    try {
      layout = null;
      int nv = 0;
      final Stack<ViewLayout> layouts = new Stack<>();
      final StringTokenizer tokens = new StringTokenizer(string);
      while(tokens.hasMoreTokens()) {
        final String token = tokens.nextToken();
        if(Strings.eq(token, "H", "V")) {
          final ViewLayout view = new ViewLayout("H".equals(token));
          if(layouts.isEmpty()) {
            layout = view;
          } else {
            layouts.peek().add(view);
          }
          layouts.add(view);
        } else if("-".equals(token)) {
          layouts.pop();
        } else {
          final ViewPanel view = getView(token);
          if(view == null) return false;
          layouts.peek().add(view);
          ++nv;
        }
      }
      return nv == views.length;
    } catch(final Exception ex) {
      Util.errln(ex);
    }
    return false;
  }

  /**
   * Returns the view specified by its internal name. The view names
   * are specified in the {@link GUIConstants} class.
   * @param name name of the view
   * @return found view container or {@code null}
   */
  private ViewPanel getView(final String name) {
    for(final ViewPanel view : views) {
      if(view.toString().equals(name)) return view;
    }
    Util.debug(Util.className(this) + ": Unknown view \"%\"", name);
    return null;
  }
}
