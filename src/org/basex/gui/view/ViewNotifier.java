package org.basex.gui.view;

import java.awt.Window;

import org.basex.BaseX;
import org.basex.Text;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.dialog.DialogHelp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.util.Array;

/**
 * This class stores all views in a window and notifies views of global
 * changes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ViewNotifier {
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
  /** Maximum history value. */
  private int maxhist;

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
  public void add(final View v) {
    view = Array.add(view, v);
  }

  /**
   * Notifies all views of a data reference change.
   */
  public void init() {
    final Context ctx = gui.context;
    final boolean db = ctx.db();
    if(db) {
      cont[0] = ctx.current();
      marked[0] = new Nodes(ctx.data());
      if(Prop.fuse) {
        BaseX.debug("[ViewNotifier] Setting gui reference in DeepFS.");
        Data data = ctx.data();
        data.fs.gui = gui;
      }
    } else {
      // close all dialogs (except help) together with database
      for(final Window w : gui.getOwnedWindows()) {
        if(w.isVisible() && w instanceof Dialog && !(w instanceof DialogHelp))
          ((Dialog) w).cancel();
      }
    }

    gui.focused = -1;
    hist = 0;
    maxhist = 0;
    for(final View v : view) v.refreshInit();

    gui.views.setViews(db);
    gui.layoutViews();
    gui.setTitle(Text.TITLE + (db ? " - " + ctx.data().meta.dbname : ""));
  }

  /**
   * Notifies all views of a focus change.
   * @param pre focused pre value
   * @param vw the calling view
   */
  public void focus(final int pre, final View vw) {
    if(gui.focused == pre) return;
    gui.focused = pre;
    for(final View v : view) 
      if(v != vw && v.isValid()) v.refreshFocus();
    if(pre != -1) gui.status.setPath(ViewData.path(gui.context.data(), pre));
  }

  /**
   * Notifies all views of a selection change.
   * @param mark marked nodes
   * @param vw the calling view
   */
  public void mark(final Nodes mark, final View vw) {
    final Context context = gui.context;
    context.marked(mark);
    for(final View v : view) if(v != vw && v.isValid()) v.refreshMark();
    BaseXLayout.enable(gui.filter, context.marked().size() != 0);
    gui.refreshControls();
  }

  /**
   * Notifies all views of a selection change. The mode flag
   * determines what happens:
   * <li>0: set currently focused node as marked node</li>
   * <li>1: add currently focused node</li>
   * <li>2: toggle currently focused node</li>
   * @param mode mark mode
   * @param vw the calling view
   */
  public void mark(final int mode, final View vw) {
    if(gui.focused == -1) return;

    final Context context = gui.context;
    Nodes nodes = context.marked();
    if(mode == 0) {
      nodes = new Nodes(gui.focused, context.data());
    } else if(mode == 1) {
      nodes.union(new int[] { gui.focused });
    } else {
      nodes.toggle(gui.focused);
    }
    mark(nodes, vw);
  }

  /**
   * Moves around in the internal history and notifies all views of
   * a context change.
   * @param forward move forward or backward
   */
  public void hist(final boolean forward) {
    // browse back/forward
    String query = "";
    if(forward) {
      if(hist == maxhist) return;
      query = queries[++hist];
    } else {
      if(hist == 0) return;
      marked[hist] = gui.context.marked();
      query = queries[--hist];
    }

    init(gui.context, cont[hist], marked[hist]);

    gui.input.setText(query);
    for(final View v : view) v.refreshContext(forward, false);
    gui.refreshControls();
  }

  /**
   * Notifies all views of a context change.
   * @param nodes new context set
   * @param quick quick switch
   * @param vw the calling view
   */
  public void context(final Nodes nodes, final boolean quick, final View vw) {
    if(nodes.size() == 0) return;

    final Context context = gui.context;
    final Nodes n = new Nodes(context.data(), context.marked().ftpos);

    if(!quick) {
      final String input = gui.input.getText();

      // add new entry
      checkHist();
      queries[hist] = input;
      marked[hist] = context.marked();
      cont[++hist] = nodes;
      queries[hist] = input;
      marked[hist] = n;
      maxhist = hist;
    } else {
      // check if current node set has already been cached
      if(!cont[hist].same(context.current())) {
        checkHist();
        // add new entry
        queries[hist] = "";
        marked[hist] = new Nodes(context.data());
        cont[++hist] = context.current();
        maxhist = hist;
      }
    }
    init(context, nodes, n);
    
    for(final View v : view) if(v != vw) v.refreshContext(true, quick);
    gui.refreshControls();
  }

  /**
   * Notifies all views of a context switch without storing
   * nodes in the history.
   * @param nodes new context nodes
   */
  public void jump(final Nodes nodes) {
    if(nodes.size() == 0) return;
    final Context context = gui.context;
    init(context, nodes, new Nodes(context.data()));
    for(final View v : view) v.refreshContext(true, true);
    gui.refreshControls();
  }

  /**
   * Notifies all views of updates in the data structure.
   */
  public void update() {
    hist = 0;
    maxhist = 0;
    for(final View v : view) if(v.isValid()) v.refreshUpdate();
    gui.refreshControls();
  }

  /**
   * Notifies all views of layout changes.
   */
  public void layout() {
    if(gui.help != null) gui.help.refresh();
    if(gui.context.db()) for(final View v : view) v.refreshLayout();
  }

  /**
   * Returns the last or next query string.
   * @param back back/forward flag
   * @return query string
   */
  public String tooltip(final boolean back) {
    return back ? hist > 0 ? gui.notify.queries[hist - 1] : null :
      hist < gui.notify.maxhist ? gui.notify.queries[hist + 1] : null;
  }
  
  // Private Methods ==========================================================

  /**
   * Initializes the current context and marked node set.
   * @param ctx context reference
   * @param curr context set
   * @param mark marked nodes
   */
  private void init(final Context ctx, final Nodes curr, final Nodes mark) {
    ctx.current(curr);
    ctx.marked(mark);
    gui.focused = -1;
  }
  
  /**
   * Checks the history data arrays.
   */
  private void checkHist() {
    final int hl = queries.length;
    if(hist + 1 == hl) {
      Array.move(queries, 1, 0, hl - 1);
      Array.move(cont, 1, 0, hl - 1);
      Array.move(marked, 1, 0, hl - 1);
      hist--;
    }
  }
}
