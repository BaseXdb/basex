package org.basex.gui.view;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * This class serves as a container for all existing views. The observer pattern
 * is used to inform all views on user interactions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ViewNotifier {
  /** Large database size (minimum database size to show warning). */
  private static final long LARGEDB = 200000000;
  /** Maximum history size. */
  public static final int MAXHIST = 20;
  /** History pointer. */
  public int hist;
  /** Reference to main window. */
  final GUI gui;

  /** Zoomed rectangle history. */
  private final Nodes[] marked = new Nodes[MAXHIST];
  /** Zoomed rectangle history. */
  private final Nodes[] cont = new Nodes[MAXHIST];
  /** Command history. */
  private final String[] queries = new String[MAXHIST];
  /** Attached views. */
  private View[] view = new View[0];
  /** Number of history entries. */
  private int histsize;

  /**
   * Constructor.
   * @param main reference to the main window
   */
  public ViewNotifier(final GUI main) {
    gui = main;
  }

  /**
   * Adds a new view.
   * @param v view to be added
   */
  void add(final View v) {
    view = Array.add(view, v);
  }

  /**
   * Notifies all views of a data reference change.
   */
  public void init() {
    final Data data = initHistory(gui.context);
    if(data != null) {
      // if a large database is opened, the user is asked if complex
      /// visualizations should be closed first
      final long size = data.meta.dbsize();
      boolean open = false;
      for(final View v : view) open |= v.visible() && v.db();
      if(open && size > LARGEDB && BaseXDialog.confirm(gui,
          Util.info(H_LARGE_DB, Performance.format(size)))) {
        for(final View v : view) if(v.visible() && v.db()) v.visible(false);
      }
    } else {
      // database closed: close open dialogs
      for(final Window w : gui.getOwnedWindows()) {
        if(w.isVisible() && w instanceof BaseXDialog) ((BaseXDialog) w).cancel();
      }
    }

    gui.context.focused = -1;
    for(final View v : view) v.refreshInit();
    gui.layoutViews();
    gui.setTitle(data != null ? data.meta.name : null);
  }

  /**
   * Notifies all views of a focus change.
   * @param pre focused pre value
   * @param vw the calling view
   */
  public void focus(final int pre, final View vw) {
    if(gui.context.focused == pre) return;
    gui.context.focused = pre;
    for(final View v : view) if(v != vw && v.visible()) v.refreshFocus();
    if(pre != -1) {
      gui.status.setText(Token.string(ViewData.path(gui.context.data(), pre)));
    }
  }

  /**
   * Notifies all views of a selection change.
   * @param mark marked nodes
   * @param vw the calling view
   */
  public void mark(final Nodes mark, final View vw) {
    final Context ctx = gui.context;
    ctx.marked = mark;
    for(final View v : view) if(v != vw && v.visible()) v.refreshMark();
    gui.filter.setEnabled(mark.size() != 0);
    gui.refreshControls();
  }

  /**
   * Notifies all views of a selection change.
   * The mode flag determines what happens:
   * <ul>
   * <li>0: set currently focused node as marked node</li>
   * <li>1: add currently focused node</li>
   * <li>2: toggle currently focused node</li>
   * </ul>
   * @param mode mark mode
   * @param vw the calling view
   */
  public void mark(final int mode, final View vw) {
    final int f = gui.context.focused;
    if(f == -1) return;

    final Context ctx = gui.context;
    Nodes nodes = ctx.marked;
    if(mode == 0) {
      nodes = new Nodes(f, ctx.data());
    } else if(mode == 1) {
      nodes.union(new int[] { f });
    } else {
      nodes.toggle(f);
    }
    mark(nodes, vw);
  }

  /**
   * Moves around in the internal history and notifies all views of a context change.
   * @param forward move forward or backward
   */
  public void hist(final boolean forward) {
    final Context ctx = gui.context;
    final String query;
    if(forward) {
      if(hist == histsize) return;
      query = queries[++hist];
    } else {
      if(hist == 0) return;
      marked[hist] = ctx.marked;
      query = queries[--hist];
    }
    ctx.set(cont[hist], marked[hist]);

    gui.input.setText(query);
    for(final View v : view) if(v.visible()) v.refreshContext(forward, false);
    gui.refreshControls();
  }

  /**
   * Notifies all views of a context change.
   * @param nodes new context set (may be {@code null} if root nodes are addressed)
   * @param quick quick switch
   * @param vw the calling view
   */
  public void context(final Nodes nodes, final boolean quick, final View vw) {
    final Context ctx = gui.context;

    // add new entry if current node set has not been cached yet
    final Nodes newn = nodes.checkRoot();
    final Nodes empty = new Nodes(new int[0], ctx.data(), ctx.marked.ftpos);
    final Nodes curr = quick ? ctx.current() : null;
    final Nodes cmp = quick ? curr : ctx.marked;
    if(cont[hist] == null ? cmp != null : cmp == null || !cont[hist].sameAs(cmp)) {
      checkHist();
      if(quick) {
        // store history entry
        queries[hist] = "";
        marked[hist] = new Nodes(ctx.data());
        // add current entry
        cont[++hist] = curr;
      } else {
        // store history entry
        final String in = gui.input.getText();
        queries[hist] = in;
        marked[hist] = ctx.marked;
        // add current entry
        cont[++hist] = newn;
        queries[hist] = in;
        marked[hist] = empty;
      }
      histsize = hist;
    }
    ctx.set(newn, empty);

    for(final View v : view) if(v != vw && v.visible()) v.refreshContext(true, quick);
    gui.refreshControls();
  }

  /**
   * Notifies all views of updates in the data structure.
   */
  public void update() {
    final Data data = initHistory(gui.context);
    if(data == null) return;
    gui.context.marked = new Nodes(data);
    for(final View v : view) if(v.visible()) v.refreshUpdate();
    gui.refreshControls();
  }

  /**
   * Notifies all views of layout changes.
   */
  public void layout() {
    for(final View v : view) {
      v.refreshLayout();
      final ViewPanel vp = (ViewPanel) v.getParent();
      final ViewMover vm = (ViewMover) vp.getComponent(0);
      vm.refreshLayout();
    }
  }

  /**
   * Returns the last or next query string.
   * @param back back/forward flag
   * @return query string
   */
  public String tooltip(final boolean back) {
    return back ? hist > 0 ? hist > 1 ? queries[hist - 2] : "" : null :
      hist < histsize ? queries[hist + 1] : null;
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Checks the history data arrays.
   */
  private void checkHist() {
    final int hl = queries.length;
    if(hist + 1 == hl) {
      Array.move(queries, 1, 0, hl - 1);
      Array.move(cont, 1, 0, hl - 1);
      Array.move(marked, 1, 0, hl - 1);
      --hist;
    }
  }

  /**
   * Removes existing history entries and sets an initial entry.
   * @param ctx database context
   * @return {@link Data} reference, or {@code null}
   */
  private Data initHistory(final Context ctx) {
    for(int h = 0; h < histsize; h++) {
      marked[h] = null;
      cont[h] = null;
      queries[h] = null;
    }
    hist = 0;
    histsize = 0;

    final Data data = ctx.data();
    if(data != null) {
      // new database opened
      marked[0] = new Nodes(data);
      queries[0] = "";
    }
    return data;
  }
}
