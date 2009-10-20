package org.basex.gui;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.core.proc.Find;
import org.basex.core.proc.XQuery;
import org.basex.data.Data;
import org.basex.data.Namespaces;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.dialog.DialogHelp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.ViewContainer;
import org.basex.gui.view.ViewNotifier;
import org.basex.gui.view.ViewPanel;
import org.basex.gui.view.explore.ExploreView;
import org.basex.gui.view.folder.FolderView;
import org.basex.gui.view.info.InfoView;
import org.basex.gui.view.map.MapView;
import org.basex.gui.view.plot.PlotView;
import org.basex.gui.view.table.TableView;
import org.basex.gui.view.text.TextView;
import org.basex.gui.view.tree.TreeView;
import org.basex.gui.view.xquery.XQueryView;
import org.basex.io.CachedOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class is the main window of the GUI. It is the central instance
 * for user interactions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class GUI extends JFrame {
  /** Database Context. */
  public final Context context;
  /** GUI properties. */
  public final GUIProp prop;
  /** View Manager. */
  public final ViewNotifier notify;

  /** Status line. */
  public final GUIStatus status;
  /** Content panel, containing all views. */
  public final ViewContainer views;
  /** Input field. */
  public final GUIInput input;
  /** Filter button. */
  public final BaseXButton filter;
  /** Search view. */
  public final XQueryView query;

  /** Fullscreen flag. */
  public boolean fullscreen;
  /** Help dialog. */
  public DialogHelp help;
  /** Text view. */
  final TextView text;
  /** Info view. */
  final InfoView info;

  /** History button. */
  final BaseXButton hist;
  /** Current input Mode. */
  final BaseXCombo mode;
  /** Top panel. */
  final BaseXBack top;
  /** Result panel. */
  final GUIMenu menu;
  /** Button panel. */
  final BaseXBack buttons;
  /** Query panel. */
  final BaseXBack nav;

  /** Execution Button. */
  private final BaseXButton go;
  /** Control panel. */
  private final BaseXBack control;
  /** Results label. */
  private final BaseXLabel hits;
  /** Buttons. */
  private final GUIToolBar toolbar;
  /** Menu panel height. */
  private int menuHeight;
  /** Fullscreen Window. */
  private JFrame fullscr;

  /** Painting flag. */
  public boolean painting;
  /** Working flag. */
  public boolean updating;
  /** Currently focused node (pre value). */
  public int focused = -1;

  /**
   * Default constructor.
   * @param ctx context reference
   * @param gprops gui properties
   */
  public GUI(final Context ctx, final GUIProp gprops) {
    context = ctx;
    prop = gprops;
    setTitle(Text.TITLE);
    setIconImage(BaseXLayout.image("icon"));

    // set window size
    final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    final int[] ps = prop.nums(GUIProp.GUILOC);
    final int[] sz = prop.nums(GUIProp.GUISIZE);
    final int x = Math.max(0, Math.min(scr.width - sz[0], ps[0]));
    final int y = Math.max(0, Math.min(scr.height - sz[1], ps[1]));
    setBounds(x, y, sz[0], sz[1]);
    if(prop.is(GUIProp.MAXSTATE)) {
      setExtendedState(MAXIMIZED_HORIZ);
      setExtendedState(MAXIMIZED_VERT);
      setExtendedState(MAXIMIZED_BOTH);
    }

    top = new BaseXBack();
    top.setLayout(new BorderLayout());

    // add header
    control = new BaseXBack();
    control.setLayout(new BorderLayout());
    control.setBorder(0, 0, 0, 1);

    // add menu bar
    menu = new GUIMenu(this);
    if(prop.is(GUIProp.SHOWMENU)) setJMenuBar(menu);

    buttons = new BaseXBack();
    buttons.setLayout(new BorderLayout());
    toolbar = new GUIToolBar(TOOLBAR, this);
    buttons.add(toolbar, BorderLayout.WEST);

    hits = new BaseXLabel(" ");
    hits.setFont(hits.getFont().deriveFont(18f));
    BaseXLayout.setWidth(hits, 150);
    hits.setHorizontalAlignment(SwingConstants.RIGHT);

    BaseXBack b = new BaseXBack();
    b.add(hits);

    buttons.add(b, BorderLayout.EAST);
    if(prop.is(GUIProp.SHOWBUTTONS)) control.add(buttons, BorderLayout.CENTER);

    nav = new BaseXBack();
    nav.setLayout(new BorderLayout(5, 0));
    nav.setBorder(2, 2, 0, 2);

    mode = new BaseXCombo(new String[] {
        BUTTONSEARCH, BUTTONXQUERY, BUTTONCMD }, HELPMODE, this);
    mode.setSelectedIndex(2);

    mode.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final int s = mode.getSelectedIndex();
        if(s == prop.num(GUIProp.SEARCHMODE) || !mode.isEnabled()) return;

        prop.set(GUIProp.SEARCHMODE, s);
        input.setText("");
        refreshControls();
      }
    });
    nav.add(mode, BorderLayout.WEST);

    input = new GUIInput(this);

    hist = new BaseXButton(this, "hist", HELPHIST);
    hist.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu popup = new JPopupMenu("History");
        final ActionListener al = new ActionListener() {
          public void actionPerformed(final ActionEvent ac) {
            input.setText(ac.getActionCommand());
            input.requestFocusInWindow();
            popup.setVisible(false);
          }
        };
        final int i = context.data() == null ? 2 :
          prop.num(GUIProp.SEARCHMODE);
        final String[] hs = i == 0 ? prop.strings(GUIProp.SEARCH) : i == 1 ?
            prop.strings(GUIProp.XQUERY) : prop.strings(GUIProp.COMMANDS);
        for(final String en : hs) {
          final JMenuItem jmi = new JMenuItem(en);
          jmi.addActionListener(al);
          popup.add(jmi);
        }
        popup.show(hist, 0, hist.getHeight());
      }
    });

    b = new BaseXBack();
    b.setLayout(new BorderLayout(5, 0));
    b.add(hist, BorderLayout.WEST);
    b.add(input, BorderLayout.CENTER);
    nav.add(b, BorderLayout.CENTER);

    go = new BaseXButton(this, "go", HELPGO);
    go.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        execute();
      }
    });

    filter = BaseXButton.command(GUICommands.FILTER, this);

    b = new BaseXBack();
    b.setLayout(new TableLayout(1, 3));
    b.add(go);
    b.add(Box.createHorizontalStrut(1));
    b.add(filter);
    nav.add(b, BorderLayout.EAST);

    if(prop.is(GUIProp.SHOWINPUT)) control.add(nav, BorderLayout.SOUTH);
    top.add(control, BorderLayout.NORTH);

    // create views
    notify = new ViewNotifier(this);
    query = new XQueryView(notify);
    text = new TextView(notify);
    info = new InfoView(notify, HELPINFO);
    final ViewPanel textpanel = new ViewPanel(text, TEXTVIEW);
    final ViewPanel querypanel = new ViewPanel(query, XQUERYVIEW);
    final ViewPanel infopanel = new ViewPanel(info, INFOVIEW);

    // create panels for closed and opened database mode
    final ViewPanel[][] panels = {
      { querypanel, infopanel, textpanel },
      { new ViewPanel(new FolderView(notify), FOLDERVIEW),
        new ViewPanel(new PlotView(notify), PLOTVIEW),
        new ViewPanel(new TableView(notify), TABLEVIEW),
        new ViewPanel(new MapView(notify), MAPVIEW),
        new ViewPanel(new TreeView(notify), TREEVIEW),
        new ViewPanel(new ExploreView(notify), EXPLOREVIEW),
        querypanel, infopanel, textpanel
      }
    };
    views = new ViewContainer(this, panels);
    views.setViews(false);

    top.add(views, BorderLayout.CENTER);
    setContentBorder();

    // add status bar
    status = new GUIStatus(this);
    if(prop.is(GUIProp.SHOWSTATUS)) top.add(status, BorderLayout.SOUTH);

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    add(top);

    setVisible(true);
    views.updateViews();
    refreshControls();

    // start logo animation as thread
    new Thread() {
      @Override
      public void run() {
        views.run();
      }
    }.start();

    input.requestFocusInWindow();
  }

  @Override
  public void dispose() {
    query.confirm();
    final boolean max = getExtendedState() == MAXIMIZED_BOTH;
    prop.set(GUIProp.MAXSTATE, max);
    if(!max) {
      prop.set(GUIProp.GUILOC, new int[] { getX(), getY() });
      prop.set(GUIProp.GUISIZE, new int[] { getWidth(), getHeight() });
    }
    super.dispose();
    prop.write();
    context.close();
  }

  /**
   * Sets a cursor.
   * @param c cursor to be set
   */
  public void cursor(final Cursor c) {
    cursor(c, false);
  }

  /**
   * Sets a cursor, forcing a new look if necessary.
   * @param c cursor to be set
   * @param force new cursor
   */
  public void cursor(final Cursor c, final boolean force) {
    final Cursor cc = getCursor();
    if(cc != c && (cc != CURSORWAIT || force)) setCursor(c);
  }

  /**
   * Queries the current input, depending on the current input mode.
   */
  protected void execute() {
    String in = input.getText();
    final boolean db = context.data() != null;
    final boolean cmd = prop.num(GUIProp.SEARCHMODE) == 2 || !db;

    if(cmd || in.startsWith("!")) {
      // run as command: command mode or exclamation mark as first character
      final int i = cmd ? 0 : 1;
      if(in.length() > i) {
        try {
          for(final Process p : new CommandParser(in.substring(i),
              context).parse()) {
            if(!exec(p, p instanceof XQuery)) break;
          }
        } catch(final QueryException ex) {
          if(!text.visible()) GUICommands.SHOWTEXT.execute(this);
          text.setText(new TokenBuilder(ex.getMessage()).finish());
        }
      }
    } else {
      if(prop.num(GUIProp.SEARCHMODE) == 1 || in.startsWith("/")) {
        xquery(in, true);
      } else {
        in = Find.find(in, context, prop.is(GUIProp.FILTERRT));
        execute(new XQuery(in), true);
      }
    }
  }

  /**
   * Launches an XQuery. Adds the default namespace, if available.
   * @param qu query to be run
   * @param main main window
   */
  public void xquery(final String qu, final boolean main) {
    // check and add default namespace
    final Namespaces ns = context.data().ns;
    final int def = ns.get(Token.EMPTY, 0);
    String in = qu;
    if(in.trim().length() == 0) in = ".";
    if(def != 0) in = "declare default element namespace \"" +
      Token.string(ns.key(def)) + "\"; " + in;
    execute(new XQuery(in), main);
  }

  /**
   * Launches the specified process in a thread. The process is ignored
   * if an update operation takes place.
   * @param pr process to be launched
   */
  public void execute(final Process pr) {
    execute(pr, false);
  }

  /**
   * Launches the specified process in a thread. The process is ignored
   * if an update operation takes place.
   * @param pr process to be launched
   * @param main call from main window
   */
  void execute(final Process pr, final boolean main) {
    if(updating) return;
    new Thread() {
      @Override
      public void run() { exec(pr, main); }
    }.start();
  }

  /** Thread counter. */
  private int threadID;
  /** Current process. */
  private Process proc;

  /**
   * Stops the current process.
   */
  public void stop() {
    if(proc != null) proc.stop();
    cursor(CURSORARROW, true);
    proc = null;
  }

  /**
   * Launches the specified process.
   * @param pr process to be launched
   * @param main call from the main window
   * @return success flag
   */
  boolean exec(final Process pr, final boolean main) {
    final int thread = ++threadID;

    // wait when command is still running
    while(proc != null) {
      proc.stop();
      Performance.sleep(50);
      if(threadID != thread) return true;
    }

    cursor(CURSORWAIT);
    try {
      // cache some variables before executing the command
      final Performance perf = new Performance();
      final Nodes current = context.current();
      final Data data = context.data();
      proc = pr;

      // execute command
      pr.context = context;
      final boolean up = pr.write();
      updating = up;
      // retrieve text result
      final CachedOutput out = new CachedOutput(context.prop.num(Prop.MAXTEXT));
      final boolean ok = pr.execute(context, out);
      updating = false;

      // command info
      final String inf = pr.info();
      // skip interrupted command
      if(!ok && inf.equals(PROGERR)) {
        proc = null;
        return false;
      }

      // show query editor feedback
      boolean feedback = main;
      if(!main && query.visible() && pr instanceof XQuery) {
        feedback = true;
        query.info(inf, ok);
      }

      // show query info
      if(prop.is(GUIProp.SHOWINFO) && (ok || main)) {
        info.setInfo(Token.token(inf), ok);
      }

      // check if query feedback was processed in the query view
      if(!ok) {
        // show error info
        if(!feedback) {
          // display text view if text output is not shown
          if(!text.visible()) GUICommands.SHOWTEXT.execute(this);
          text.setText(Token.token(inf));
        }
      } else {
        // try to convert xquery result to nodeset
        final Result result = pr.result();
        final Nodes nodes = result instanceof Nodes ? (Nodes) result : null;
  
        // treat text view different to other views
        if(ok && out.size() != 0 && nodes == null) {
          // display text view
          if(!text.visible()) GUICommands.SHOWTEXT.execute(this);
          text.setText(out);
        }
  
        final Data ndata = context.data();
        final String time = perf.getTimer();
        Nodes marked = context.marked();
        if(ndata != data) {
          // database reference has changed - notify views
          notify.init();
        } else if(up) {
          // update command
          notify.update();
        } else if(result != null) {
          if(context.current() != current || prop.is(GUIProp.FILTERRT)) {
            // refresh context
            if(nodes != null) {
              notify.context((Nodes) result, prop.is(GUIProp.FILTERRT), null);
            }
          } else if(marked != null) {
            // refresh highlight
            if(nodes != null) {
              // use query result
              marked = nodes;
            } else if(marked.size() != 0) {
              // remove old highlight
              marked = new Nodes(data);
            }
            // highlights have changed.. refresh views
            if(!marked.same(context.marked())) {
              notify.mark(marked, null);
            }
            if(thread != threadID) {
              proc = null;
              return true;
            }
          }
        }
  
        // show number of hits
        setHits(result == null ? 0 : result.size());
  
        // show status info
        status.setText(Main.info(PROCTIME, time));
      }
    } catch(final Exception ex) {
      // unexpected error
      ex.printStackTrace();
      Dialog.error(this, Main.info(PROCERR, pr,
          ex.toString().length() != 0 ? ex.toString() : ex.getMessage()));
      updating = false;
    }

    cursor(CURSORARROW, true);
    proc = null;
    return true;
  }

  /**
   * Sets the border of the content area.
   */
  void setContentBorder() {
    final int n = control.getComponentCount();
    final int n2 = top.getComponentCount();

    if(n == 0 && n2 == 2) {
      views.setBorder(0, 0, 0, 0);
    } else {
      views.setBorder(new CompoundBorder(new EmptyBorder(3, 1, 3, 1),
          new EtchedBorder()));
    }
  }

  /**
   * Refreshes the layout.
   */
  public void updateLayout() {
    init(prop);
    repaint();
    notify.layout();
  }

  /**
   * Updates the control panel.
   * @param comp component to be updated
   * @param show true if component is visible
   * @param layout component layout.
   */
  void updateControl(final JComponent comp, final boolean show,
      final String layout) {

    if(comp == status) {
      if(!show) top.remove(comp);
      else top.add(comp, layout);
    } else if(comp == menu) {
      if(!show) menuHeight = menu.getHeight();
      final int s = show ? menuHeight : 0;
      BaseXLayout.setHeight(menu, s);
      menu.setSize(menu.getWidth(), s);
    } else {
      if(!show) control.remove(comp);
      else control.add(comp, layout);
    }
    setContentBorder();
    (fullscr == null ? getRootPane() : fullscr).validate();
    refreshControls();
  }

  /**
   * Updates the view layout.
   */
  public void layoutViews() {
    views.updateViews();
    refreshControls();
    repaint();
  }

  /**
   * Refreshes the menu and the buttons.
   */
  public void refreshControls() {
    final Nodes marked = context.marked();
    if(marked != null) setHits(marked.size());

    BaseXLayout.enable(filter, marked != null && marked.size() != 0);
    refreshMode();
    toolbar.refresh();
    menu.refresh();

    final int i = context.data() == null ? 2 : prop.num(GUIProp.SEARCHMODE);
    final String[] hs = i == 0 ? prop.strings(GUIProp.SEARCH) : i == 1 ?
        prop.strings(GUIProp.XQUERY) : prop.strings(GUIProp.COMMANDS);
    hist.setEnabled(hs.length != 0);
  }

  /**
   * Refreshes the input mode.
   */
  protected void refreshMode() {
    final Data data = context.data();
    final boolean db = data != null;
    final int t = mode.getSelectedIndex();
    final int s = !db ? 2 : prop.num(GUIProp.SEARCHMODE);

    final boolean inf = prop.is(GUIProp.SHOWINFO);
    context.prop.set(Prop.ALLINFO, inf);
    context.prop.set(Prop.XMLPLAN, inf);
    context.prop.set(Prop.INFO, inf);

    input.help(s == 0 ? data.fs != null ? HELPSEARCHFS : HELPSEARCHXML :
      s == 1 ? HELPXPATH : HELPCMD);
    mode.setEnabled(db);
    go.setEnabled(s == 2 || !prop.is(GUIProp.EXECRT));

    if(s != t) {
      mode.setSelectedIndex(s);
      input.setText("");
      input.requestFocusInWindow();
    }
  }

  /**
   * Sets hits information.
   * @param n number of hits
   */
  public void setHits(final long n) {
    hits.setText(n + " " + HITS);
  }

  /**
   * Turns fullscreen mode on/off.
   */
  public void fullscreen() {
    fullscreen ^= true;
    fullscreen(fullscreen);
  }

  /**
   * Turns fullscreen mode on/off.
   * @param full fullscreen mode
   */
  public void fullscreen(final boolean full) {
    if(full ^ fullscr == null) {
      if(!prop.is(GUIProp.SHOWMENU)) GUICommands.SHOWMENU.execute(this);
      return;
    }

    if(full) {
      control.remove(buttons);
      control.remove(nav);
      getRootPane().remove(menu);
      top.remove(status);
      remove(top);
      fullscr = new JFrame();
      fullscr.setIconImage(getIconImage());
      fullscr.setTitle(getTitle());
      fullscr.setUndecorated(true);
      fullscr.setJMenuBar(menu);
      fullscr.add(top);
      fullscr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    } else {
      fullscr.removeAll();
      fullscr.dispose();
      fullscr = null;
      if(!prop.is(GUIProp.SHOWBUTTONS))
        control.add(buttons, BorderLayout.CENTER);
      if(!prop.is(GUIProp.SHOWINPUT)) control.add(nav, BorderLayout.SOUTH);
      if(!prop.is(GUIProp.SHOWSTATUS)) top.add(status, BorderLayout.SOUTH);
      setJMenuBar(menu);
      add(top);
    }

    prop.set(GUIProp.SHOWMENU, !full);
    prop.set(GUIProp.SHOWBUTTONS, !full);
    prop.set(GUIProp.SHOWINPUT, !full);
    prop.set(GUIProp.SHOWSTATUS, !full);
    fullscreen = full;

    GraphicsEnvironment.getLocalGraphicsEnvironment().
      getDefaultScreenDevice().setFullScreenWindow(fullscr);
    setContentBorder();
    refreshControls();
    updateControl(menu, !full, BorderLayout.NORTH);
    setVisible(!full);
  }
}
