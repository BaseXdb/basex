package org.basex.gui.view;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * This class manages all visible and invisible views and allows drag and
 * drop operations inside the panel.
 *
 * @author BaseX Team, BSD License
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
  private final Rectangle pos = new Rectangle();
  /** Logo. */
  private final Image logo;

  /** View Layout. */
  private ViewLayout layout;
  /** Current layout string. */
  private String layoutString;
  /** Source View (can be {@code null}). */
  private ViewPanel source;
  /** Target View (can be {@code null}). */
  private ViewPanel target;
  /** Target orientation (can be {@code null}). */
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
    setBackground(backColor);

    this.gui = gui;
    logo = BaseXImages.get("logo_large");

    final int vl = view.length;
    views = new ViewPanel[vl];
    for(int v = 0; v < vl; v++) views[v] = new ViewPanel(view[v]);

    // build layout or use default if something goes wrong
    if(!buildLayout(gui.gopts.get(GUIOptions.LAYOUT)) && !buildLayout(VIEWS)) {
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
    persistLayout();
    layoutString = ls;

    // rebuild views
    removeAll();
    layout.persister = this::persistLayout;
    layout.addTo(this);
    validate();
    repaint();
  }

  /**
   * Stores the current layout, including the proportional view sizes, in the GUI options.
   * Invoked on structural changes and whenever the user drags a splitter (see
   * {@link ViewLayout#persister}).
   */
  private void persistLayout() {
    gui.gopts.set(GUIOptions.LAYOUT, layout.layoutString(true));
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
      // the model already holds the current sizes (kept in sync on every splitter drag),
      // so the moved components carry their weights through the restructuring
      final ViewComponent comp = layout.delete(source);
      if(comp instanceof final ViewLayout vl) layout = vl;

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
      if(comp instanceof final ViewLayout vlayout) {
        if(add(vlayout)) return true;
      } else if(comp == target) {
        final ViewLayout nl = insert(vl, c, c + 1, target);
        if(nl != null) vl.set(nl, c);
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
    final ViewLayout nl = insert(vl, 0, vl.list.size(), vl);
    return nl != null ? nl : vl;
  }

  /**
   * Inserts the dragged view into the specified layout, or combines it with a component.
   * @param vl     layout instance
   * @param before insertion index for a north or west location
   * @param after  insertion index for a south or east location
   * @param comp   component to be combined with the dragged view
   * @return new layout, or {@code null} if the view was inserted into the specified layout
   */
  private ViewLayout insert(final ViewLayout vl, final int before, final int after,
      final ViewComponent comp) {
    final boolean first = location == Location.NORTH || location == Location.WEST;
    final boolean horizontal = location == Location.WEST || location == Location.EAST;
    if(vl.horizontal == horizontal) {
      vl.insert(source, first ? before : after);
      return null;
    }
    return first ? new ViewLayout(horizontal, source, comp) :
      new ViewLayout(horizontal, comp, source);
  }

  /**
   * Finds the view under the mouse position and returns it as possible
   * target for dropping the dragged view.
   * @return view container or {@code null}
   */
  private ViewPanel getTarget() {
    for(final ViewPanel view : views) {
      if(view.isVisible() && rect(view).contains(sp)) return view;
    }
    return null;
  }

  /**
   * Returns the bounds of the specified component relative to this container.
   * @param comp component
   * @return bounds
   */
  private Rectangle rect(final Component comp) {
    return SwingUtilities.convertRectangle(comp.getParent(), comp.getBounds(), this);
  }

  @Override
  public void paint(final Graphics g) {
    super.paint(g);
    if(source == null) return;

    ((Graphics2D) g).setStroke(STROKE);
    if(location != null) {
      g.setColor(darkGray);
      g.drawRect(pos.x, pos.y, pos.width - 1, pos.height - 1);
      ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g.setColor(lightGray);
      g.fillRect(pos.x, pos.y, pos.width, pos.height);
    }
  }

  /**
   * Calculates the target position.
   */
  private void calc() {
    final int hh = getHeight();
    final int ww = getWidth();

    pos.setBounds(1, 1, ww - 2, hh - 2);

    location = null;
    target = getTarget();

    // paint panel which is currently moved somewhere else
    if(target != null && target != source) {
      final Rectangle tr = rect(target);
      final int minx = tr.width / 2;
      final int miny = tr.height / 2;

      if(Math.abs(tr.x + tr.width / 2 - sp.x) < tr.width / 3) {
        if(sp.y > tr.y && sp.y < tr.y + miny) {
          pos.setBounds(tr.x, tr.y, tr.width, miny);
          location = Location.NORTH;
        } else if(sp.y > tr.y + tr.height - miny && sp.y < tr.y + tr.height) {
          pos.setBounds(tr.x, tr.y + tr.height - miny, tr.width, miny);
          location = Location.SOUTH;
        }
      } else if(Math.abs(tr.y + tr.height / 2 - sp.y) < tr.height / 3) {
        if(sp.x > tr.x && sp.x < tr.x + minx) {
          pos.setBounds(tr.x, tr.y, minx, tr.height);
          location = Location.WEST;
        } else if(sp.x > tr.x + tr.width - minx && sp.x < tr.x + tr.width) {
          pos.setBounds(tr.x + tr.width - minx, tr.y, minx, tr.height);
          location = Location.EAST;
        }
      }
    }

    if(location == null) {
      final int minx = ww / 4;
      final int miny = hh / 4;
      target = null;
      if(sp.y < miny) {
        pos.height = miny;
        location = Location.NORTH;
      } else if(sp.y > hh - miny) {
        pos.height = miny;
        pos.y = hh - miny;
        location = Location.SOUTH;
      } else if(sp.x < minx) {
        pos.width = minx;
        location = Location.WEST;
      } else if(sp.x > ww - minx) {
        pos.width = minx;
        pos.x = ww - minx;
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
      final ArrayDeque<ViewLayout> layouts = new ArrayDeque<>();
      // collect all tokens (allows look-ahead for the optional weight tokens)
      final String[] tokens = string.trim().split("\\s+");

      final int ts = tokens.length;
      for(int t = 0; t < ts; t++) {
        final String token = tokens[t];
        final ViewComponent comp;
        if("-".equals(token)) {
          layouts.pop();
          continue;
        } else if(Strings.eq(token, "H", "V")) {
          final ViewLayout view = new ViewLayout("H".equals(token));
          if(layouts.isEmpty()) {
            layout = view;
          } else {
            layouts.peek().add(view);
          }
          layouts.push(view);
          comp = view;
        } else {
          final ViewPanel view = getView(token);
          if(view == null) return false;
          layouts.peek().add(view);
          ++nv;
          comp = view;
        }
        // parse the optional proportional size that may follow a layout or view token
        if(t + 1 < ts) {
          final String next = tokens[t + 1];
          final char ch = next.charAt(0);
          if(ch >= '0' && ch <= '9' || ch == '.') {
            comp.weight(Double.parseDouble(next));
            ++t;
          }
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
    Util.debugln(Util.className(this) + ": Unknown view \"%\"", name);
    return null;
  }
}
