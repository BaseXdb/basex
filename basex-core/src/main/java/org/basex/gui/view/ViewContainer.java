package org.basex.gui.view;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.util.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * This class manages all visible and invisible views and allows drag and
 * drop operations inside the panel.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class ViewContainer extends BaseXBack {
  /** Dragging stroke. */
  private static final BasicStroke STROKE = new BasicStroke(2);

  /** Orientation enumerator. */
  private enum Target {
    /** North orientation. */ NORTH,
    /** West orientation.  */ WEST,
    /** East orientation.  */ EAST,
    /** South orientation. */ SOUTH
  }

  /** Reference to main window. */
  private final AGUI gui;
  /** View Layout. */
  private ViewAlignment layout;
  /** Current layout string. */
  private String layoutString;
  /** View panels. */
  private final ViewPanel[] views;
  /** Source View. */
  private ViewPanel source;
  /** Target View. */
  private ViewPanel target;
  /** Logo reference. */
  private final Image logo;
  /** Target orientation. */
  private Target orient;
  /** Temporary mouse position. */
  private Point sp;
  /** Temporary rectangle position. */
  private final int[] pos = new int[4];

  /**
   * Constructor.
   * @param main reference to the main window
   * @param v view panels
   */
  public ViewContainer(final AGUI main, final View... v) {
    layout(new BorderLayout()).mode(Fill.PLAIN);
    logo = BaseXLayout.image("logo");
    setBackground(Color.white);

    views = new ViewPanel[v.length];
    for(int i = 0; i < v.length; ++i) views[i] = new ViewPanel(v[i]);
    gui = main;
    // build layout or use default if something goes wrong
    if(!buildLayout(gui.gopts.get(GUIOptions.VIEWS)) && !buildLayout(VIEWS)) {
      Util.errln(Util.className(this) + ": could not build layout \"%\"", VIEWS);
    }
  }

  /**
   * Updates and validates the views.
   */
  public void updateViews() {
    // update visibility of all components, dependent of the existence of a database
    layout.setVisibility(gui.context.data() != null);

    // check if the layout of visible components has changed
    final String ls = layout.layoutString(false);
    if(ls.equals(layoutString)) return;

    // rebuild views
    removeAll();
    layout.createView(this);
    validate();
    repaint();
    gui.gopts.set(GUIOptions.VIEWS, layout.layoutString(true));
    layoutString = ls;
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    if(getComponentCount() != 0) return;

    final int w = getWidth();
    final int h = getHeight();
    final int hh = Math.max(220, Math.min(700, h));
    final Insets i = getInsets();

    if(gui.gopts.get(GUIOptions.GRADIENT)) {
      BaseXLayout.fill(g, WHITE, color1, i.left, i.top, w - i.right, h - i.bottom);
    }
    if(w < 150 || h < 160) return;

    final int lh = logo.getHeight(this);
    final int y = (hh - lh - 60) / 2;
    g.drawImage(logo, (w - logo.getWidth(this)) / 2, y, this);
    if(w < 200 || h < 200) return;

    g.setColor(DGRAY);
    g.setFont(lfont);
    BaseXLayout.drawCenter(g, VERSINFO + ' ' + Prop.VERSION, w, y + 20 + lh);
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

    if(orient != null) {
      if(layout.delete(source) && !(layout.comp[0] instanceof ViewPanel))
        layout = (ViewAlignment) layout.comp[0];

      if(target == null) layout = addView(layout);
      else add(layout);
      updateViews();
    }
    source = null;
    repaint();
  }

  /**
   * Adds the dragged view after if it has been removed.
   * @param lay layout instance
   * @return true if component was successfully added
   */
  private boolean add(final ViewAlignment lay) {
    for(int o = 0; o < lay.comp.length; ++o) {
      final ViewLayout comp = lay.comp[o];
      if(comp instanceof ViewAlignment) {
        if(add((ViewAlignment) comp)) return true;
      } else if(comp == target) {
        final boolean west = orient == Target.WEST;
        final boolean east = orient == Target.EAST;

        if(orient == Target.NORTH || west) {
          if(lay.horiz == west) {
            lay.add(source, o);
          } else {
            final ViewAlignment l = new ViewAlignment(west);
            l.add(source);
            l.add(target);
            lay.comp[o] = l;
          }
        } else if(orient == Target.SOUTH || east) {
          if(lay.horiz == east) {
            lay.add(source, o + 1);
          } else {
            final ViewAlignment l = new ViewAlignment(east);
            l.add(target);
            l.add(source);
            lay.comp[o] = l;
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Adds the dragged view into the specified layout instance.
   * @param lay layout instance
   * @return resulting layout
   */
  private ViewAlignment addView(final ViewAlignment lay) {
    final boolean west = orient == Target.WEST;
    final boolean east = orient == Target.EAST;
    ViewAlignment l = lay;

    if(orient == Target.NORTH || west) {
      if(l.horiz == west) {
        l.add(source, 0);
      } else {
        final ViewAlignment ll = new ViewAlignment(west);
        ll.add(source);
        ll.add(l);
        l = ll;
      }
    } else if(orient == Target.SOUTH || east) {
      if(l.horiz == east) {
        l.add(source);
      } else {
        final ViewAlignment ll = new ViewAlignment(east);
        ll.add(l);
        ll.add(source);
        l = ll;
      }
    }
    return l;
  }

  /**
   * Finds the view under the mouse position and returns it as possible
   * target for dropping the dragged view.
   * @return view container
   */
  private ViewPanel getTarget() {
    for(final ViewPanel v : views) {
      if(v.isVisible() && new Rectangle(absLoc(v), v.getSize()).contains(sp))
        return v;
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
    if(orient != null) {
      g.setColor(color(16));
      g.drawRect(pos[0], pos[1], pos[2] - 1, pos[3] - 1);
      ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
      g.setColor(color(8));
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

    orient = null;
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
          orient = Target.NORTH;
        } else if(sp.y > tr.y + tr.height - miny && sp.y < tr.y + tr.height) {
          pos[0] = tr.x;
          pos[1] = tr.y + tr.height - miny;
          pos[2] = tr.width;
          pos[3] = miny;
          orient = Target.SOUTH;
        }
      } else if(Math.abs(tr.y + tr.height / 2 - sp.y) < tr.height / 3) {
        if(sp.x > tr.x && sp.x < tr.x + minx) {
          pos[0] = tr.x;
          pos[1] = tr.y;
          pos[2] = minx;
          pos[3] = tr.height;
          orient = Target.WEST;
        } else if(sp.x > tr.x + tr.width - minx && sp.x < tr.x + tr.width) {
          pos[0] = tr.x + tr.width - minx;
          pos[1] = tr.y;
          pos[2] = minx;
          pos[3] = tr.height;
          orient = Target.EAST;
        }
      }
    }

    if(orient == null) {
      final int minx = ww >> 2;
      final int miny = hh >> 2;
      target = null;
      if(sp.y < miny) {
        pos[3] = miny;
        orient = Target.NORTH;
      } else if(sp.y > hh - miny) {
        pos[3] = miny;
        pos[1] = hh - miny;
        orient = Target.SOUTH;
      } else if(sp.x < minx) {
        pos[2] = minx;
        orient = Target.WEST;
      } else if(sp.x > ww - minx) {
        pos[2] = minx;
        pos[0] = ww - minx;
        orient = Target.EAST;
      }
    }
  }

  /**
   * Builds the view layout by parsing the layout string.
   * @param cnstr layout string
   * @return true if everything went alright
   */
  private boolean buildLayout(final String cnstr) {
    try {
      layout = null;
      int lvl = -1;
      final ViewAlignment[] l = new ViewAlignment[16];
      final StringTokenizer st = new StringTokenizer(cnstr);
      int nv = 0;
      while(st.hasMoreTokens()) {
        final String t = st.nextToken();
        if(Token.eq(t, "H", "V")) {
          l[lvl + 1] = new ViewAlignment("H".equals(t));
          if(layout == null) {
            layout = l[0];
          } else {
            l[lvl].add(l[lvl + 1]);
          }
          ++lvl;
        } else if("-".equals(t)) {
          --lvl;
        } else {
          final ViewPanel view = getView(t);
          if(view == null) return false;
          l[lvl].add(view);
          ++nv;
        }
      }
      if(nv == views.length) return true;
      Util.errln(Util.className(this) + ": initializing views: " + cnstr);
    } catch(final Exception ex) {
      Util.debug(ex);
      Util.errln(Util.className(this) + ": could not build layout: " + cnstr);
    }
    return false;
  }

  /**
   * Returns the view specified by its internal name. The view names
   * are specified in the {@link GUIConstants} class.
   * @param name name of the view
   * @return found view container
   */
  private ViewPanel getView(final String name) {
    for(final ViewPanel view : views) {
      if(view.toString().equals(name)) return view;
    }
    Util.debug(Util.className(this) + ": Unknown view \"%\"", name);
    return null;
  }
}
