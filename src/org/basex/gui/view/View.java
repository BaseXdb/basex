package org.basex.gui.view;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.basex.Text;
import org.basex.core.Context;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPanel;
import org.basex.gui.layout.BaseXPopup;
import org.basex.util.Array;

/**
 * View observer pattern. All inheriting classes are attached to the
 * views array
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class View extends BaseXPanel {
  /** Maximum history size. */
  public static final int MAXHIST = 20;
  /** Zoomed rectangle history. */
  public static final Nodes[] MARKHIST = new Nodes[MAXHIST];
  /** Zoomed rectangle history. */
  public static final Nodes[] NODEHIST = new Nodes[MAXHIST];
  /** Command history. */
  public static final String[] QUERYHIST = new String[MAXHIST];
  /** Attached views. */
  private static View[] view = new View[0];
  /** History pointer. */
  public static int hist;
  /** Maximum history value. */
  public static int maxhist;
  /** Painting flag. */
  public static boolean painting;
  /** Working flag. */
  public static boolean working;
  
  /** Currently focused node (pre value). */
  public static int focused = -1;
  /** Current FTPrePos values. */
  public static int[][] ftPos;
  /** Current FTPointer on FTPrePos values. */
  public static int[] ftPoi; 
  
  /** Popup reference. */
  public BaseXPopup popup;


  /**
   * Registers the specified view.
   * @param hlp help text
   */
  protected View(final byte[] hlp) {
    super(hlp);
    
    if(GUIConstants.font == null) notifyLayout();
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addKeyListener(this);
    addComponentListener(this);
    setMode(GUIConstants.FILL.DOWN);
    setFocusable(true);
    view = Array.add(view, this);
  }

  /**
   * Notifies all views of a data reference change.
   */
  public static void notifyInit() {
    final Context ctx = GUI.context;
    final boolean db = ctx.db();
    if(db) {
      NODEHIST[0] = ctx.current();
      MARKHIST[0] = new Nodes(ctx.data());
    }
    
    focused = -1;
    ftPos = null;
    ftPoi = null;
    hist = 0;
    maxhist = 0;
    for(final View v : view) v.refreshInit();

    final GUI gui = GUI.get();
    gui.views.setViews(db);
    gui.layoutViews();

    // set new windows title
    gui.status.setPerformance("");
    gui.setTitle(Text.TITLE + (db ? " - " + ctx.data().meta.dbname : ""));
  }

  /**
   * Notifies all views of a focus change.
   * @param pre focused pre value
   * @param vw the calling view
   */
  public static void notifyFocus(final int pre, final View vw) {
    if(focused == pre) return;
    focused = pre;
    for(final View v : view) if(v != vw && v.isValid()) v.refreshFocus();
    if(pre != -1) GUI.get().status.setPath(
        ViewData.path(GUI.context.data(), pre));
  }

  /**
   * Notifies all views of a selection change.
   * @param mark marked nodes
   */
  public static void notifyMark(final Nodes mark) {
    final Context context = GUI.context;
    context.marked(mark);
    for(final View v : view) if(v.isValid()) v.refreshMark();
    final GUI gui = GUI.get();
    BaseXLayout.enable(gui.filter, context.marked().size != 0);
    gui.refreshControls();
  }

  /**
   * Notifies all views of a selection change. The mode flag
   * determines what happens:
   * <li>0: set currently focused node as marked node</li>
   * <li>1: add currently focused node</li>
   * <li>2: toggle currently focused node</li>
   * @param mode mark mode
   */
  public static void notifyMark(final int mode) {
    if(focused == -1) return;

    final Context context = GUI.context;
    Nodes nodes = context.marked();
    if(mode == 0) {
      nodes = new Nodes(focused, context.data());
    } else if(mode == 1) {
      nodes.add(focused);
    } else {
      nodes.toggle(focused);
    }
    notifyMark(nodes);
  }

  /**
   * Moves around in the internal history and notifies all views of
   * a context change.
   * @param forward move forward or backward
   */
  public static void notifyHist(final boolean forward) {
    // browse back/forward
    String query = "";
    if(forward) {
      if(hist == maxhist) return;
      query = QUERYHIST[++hist];
    } else {
      if(hist == 0) return;
      MARKHIST[hist] = GUI.context.marked();
      query = QUERYHIST[--hist];
    }

    init(GUI.context, NODEHIST[hist], MARKHIST[hist]);

    final GUI gui = GUI.get();
    gui.input.setText(query);
    for(final View v : view) v.refreshContext(forward, false);
    gui.refreshControls();
  }

  /**
   * Notifies all views of a context change.
   * @param nodes new context set
   * @param quick quick switch
   */
  public static void notifyContext(final Nodes nodes, final boolean quick) {
    if(nodes.size == 0) return;

    final GUI gui = GUI.get();
    final Context context = GUI.context;
    final Nodes n = new Nodes(context.data());

    if(!quick) {
      final String input = gui.input.getText();

      // add new entry
      checkHist();
      QUERYHIST[hist] = input;
      MARKHIST[hist] = context.marked();
      NODEHIST[++hist] = nodes;
      QUERYHIST[hist] = input;
      MARKHIST[hist] = n;
      maxhist = hist;
    } else {
      // check if current node set has already been cached
      if(!NODEHIST[hist].sameAs(context.current())) {
        checkHist();
        // add new entry
        QUERYHIST[hist] = "";
        MARKHIST[hist] = new Nodes(context.data());
        NODEHIST[++hist] = context.current();
        maxhist = hist;
      }
    }
    init(context, nodes, n);
    
    for(final View v : view) v.refreshContext(true, quick);
    gui.refreshControls();
  }
  
  /**
   * Checks the history data arrays.
   */
  private static void checkHist() {
    final int hl = QUERYHIST.length;
    if(hist + 1 == hl) {
      Array.move(QUERYHIST, 1, 0, hl - 1);
      Array.move(NODEHIST, 1, 0, hl - 1);
      Array.move(MARKHIST, 1, 0, hl - 1);
      hist--;
    }
  }

  /**
   * Notifies all views of a context switch without storing
   * nodes in the history.
   * @param nodes new context nodes
   */
  public static void notifySwitch(final Nodes nodes) {
    if(nodes.size == 0) return;
    final Context context = GUI.context;
    init(context, nodes, new Nodes(context.data()));
    for(final View v : view) v.refreshContext(true, true);
    GUI.get().refreshControls();
  }

  /**
   * Notifies all views of updates in the data structure.
   */
  public static void notifyUpdate() {
    hist = 0;
    maxhist = 0;
    for(final View v : view) if(v.isValid()) v.refreshUpdate();
    GUI.get().refreshControls();
  }

  /**
   * Notifies all views of layout changes.
   */
  public static void notifyLayout() {
    GUIConstants.initFonts();
    if(GUI.context.db()) for(final View v : view) v.refreshLayout();
  }

  /**
   * Initializes the current context and marked node set.
   * @param ctx context reference
   * @param curr context set
   * @param mark marked nodes
   */
  private static void init(final Context ctx, final Nodes curr,
      final Nodes mark) {
    ctx.current(curr);
    ctx.marked(mark);
    focused = -1;
  }


  /**
   * Called when the data reference has changed.
   */
  protected abstract void refreshInit();

  /**
   * Called when a new focus has been chosen.
   */
  protected abstract void refreshFocus();

  /**
   * Called when a context set has been
   * marked.
   */
  protected abstract void refreshMark();

  /**
   * Called when a new context set has been chosen.
   * @param more show more details
   * @param quick perform a quick context switch
   */
  protected abstract void refreshContext(boolean more, boolean quick);

  /**
   * Called when GUI design has changed.
   */
  protected abstract void refreshLayout();

  /**
   * Called when updates have been done in the data structure.
   */
  protected abstract void refreshUpdate();

  @Override
  public void mouseEntered(final MouseEvent e) {
    if(working) return;
    GUI.get().checkFocus(this);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    if(working) return;
    GUI.get().cursor(GUIConstants.CURSORARROW);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(working) return;
    requestFocusInWindow();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if(working) return;
    final int key = e.getKeyCode();
    final boolean alt = e.isAltDown();
    final boolean ctrl = e.isControlDown();
    final boolean shift = e.isShiftDown();

    if(key == KeyEvent.VK_ESCAPE) {
      GUI.get().fullscreen(false);
    } else if(key == KeyEvent.VK_ENTER) {
      GUICommands.FILTER.execute();
    } else if(key == KeyEvent.VK_SPACE) {
      notifyMark(ctrl ? 2 : shift ? 1 : 0);
    } else if(key == KeyEvent.VK_BACK_SPACE || alt && key == KeyEvent.VK_LEFT) {
      GUICommands.GOBACK.execute();
    } else if(alt && key == KeyEvent.VK_RIGHT) {
      GUICommands.GOFORWARD.execute();
    } else if(alt && key == KeyEvent.VK_UP) {
      GUICommands.GOUP.execute();
    } else if(alt && key == KeyEvent.VK_HOME) {
      GUICommands.ROOT.execute();
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if(working) return;

    final char key = e.getKeyChar();
    if(key == '+' || key == '-') {
      GUIProp.fontsize = Math.max(1, GUIProp.fontsize + (key == '+' ? 1 : -1));
      notifyLayout();
    }
  }

  @Override
  public final String toString() {
    return getName();
  }
}
