package org.basex.gui;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.basex.core.AProp;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Find;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
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
import org.basex.gui.view.editor.EditorView;
import org.basex.gui.view.explore.ExploreView;
import org.basex.gui.view.folder.FolderView;
import org.basex.gui.view.info.InfoView;
import org.basex.gui.view.map.MapView;
import org.basex.gui.view.plot.PlotView;
import org.basex.gui.view.table.TableView;
import org.basex.gui.view.text.TextView;
import org.basex.gui.view.tree.TreeView;
import org.basex.io.IOUrl;
import org.basex.io.out.ArrayOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.Version;

/**
 * This class is the main window of the GUI. It is the central instance
 * for user interactions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class GUI extends AGUI {
  /** View Manager. */
  public final ViewNotifier notify;

  /** Status line. */
  public final GUIStatus status;
  /** Input field. */
  public final GUIInput input;
  /** Filter button. */
  public final BaseXButton filter;
  /** Search view. */
  public final EditorView editor;
  /** Info view. */
  public final InfoView info;

  /** Painting flag; if activated, interactive operations are skipped. */
  public boolean painting;
  /** Updating flag; if activated, operations accessing the data are skipped. */
  public boolean updating;
  /** Fullscreen flag. */
  public boolean fullscreen;
  /** Help dialog. */
  public DialogHelp help;
  /** Result panel. */
  public final GUIMenu menu;
  /** Button panel. */
  public final BaseXBack buttons;
  /** Navigation/input panel. */
  public final BaseXBack nav;

  /** Content panel, containing all views. */
  final ViewContainer views;
  /** History button. */
  final BaseXButton hist;
  /** Current input Mode. */
  final BaseXCombo mode;

  /** Text view. */
  private final TextView text;
  /** Top panel. */
  private final BaseXBack top;
  /** Execution Button. */
  private final BaseXButton go;
  /** Control panel. */
  private final BaseXBack control;
  /** Results label. */
  private final BaseXLabel hits;
  /** Buttons. */
  private final GUIToolBar toolbar;

  /** Current command. */
  private Command command;
  /** Menu panel height. */
  private int menuHeight;
  /** Fullscreen Window. */
  private JFrame fullscr;
  /** Thread counter. */
  private int threadID;

  /**
   * Default constructor.
   * @param ctx database context
   * @param gprops gui properties
   */
  public GUI(final Context ctx, final GUIProp gprops) {
    super(ctx, gprops);

    // set window size
    final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    final int[] ps = gprop.nums(GUIProp.GUILOC);
    final int[] sz = gprop.nums(GUIProp.GUISIZE);
    final int x = Math.max(0, Math.min(scr.width - sz[0], ps[0]));
    final int y = Math.max(0, Math.min(scr.height - sz[1], ps[1]));
    setBounds(x, y, sz[0], sz[1]);
    if(gprop.is(GUIProp.MAXSTATE)) {
      setExtendedState(MAXIMIZED_HORIZ);
      setExtendedState(MAXIMIZED_VERT);
      setExtendedState(MAXIMIZED_BOTH);
    }

    top = new BaseXBack(new BorderLayout());

    // add header
    control = new BaseXBack(new BorderLayout());

    // add menu bar
    menu = new GUIMenu(this);
    if(gprop.is(GUIProp.SHOWMENU)) setJMenuBar(menu);

    buttons = new BaseXBack(new BorderLayout());
    toolbar = new GUIToolBar(TOOLBAR, this);
    buttons.add(toolbar, BorderLayout.WEST);

    hits = new BaseXLabel(" ");
    hits.setFont(hits.getFont().deriveFont(18f));
    BaseXLayout.setWidth(hits, 150);
    hits.setHorizontalAlignment(SwingConstants.RIGHT);

    BaseXBack b = new BaseXBack();
    b.add(hits);

    buttons.add(b, BorderLayout.EAST);
    if(gprop.is(GUIProp.SHOWBUTTONS)) control.add(buttons, BorderLayout.CENTER);

    nav = new BaseXBack(new BorderLayout(5, 0)).border(2, 2, 0, 2);

    mode = new BaseXCombo(this, BUTTONSEARCH, BUTTONXQUERY, BUTTONCMD);
    mode.setSelectedIndex(2);

    mode.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final int s = mode.getSelectedIndex();
        if(s == gprop.num(GUIProp.SEARCHMODE) || !mode.isEnabled()) return;

        gprop.set(GUIProp.SEARCHMODE, s);
        input.setText("");
        refreshControls();
      }
    });
    nav.add(mode, BorderLayout.WEST);

    input = new GUIInput(this);

    hist = new BaseXButton(this, "hist", HELPHIST);
    hist.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu pop = new JPopupMenu();
        final ActionListener al = new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent ac) {
            input.setText(ac.getActionCommand());
            input.requestFocusInWindow();
            pop.setVisible(false);
          }
        };
        final int i = context.data() == null ? 2 :
          gprop.num(GUIProp.SEARCHMODE);
        final String[] hs = gprop.strings(i == 0 ? GUIProp.SEARCH : i == 1 ?
            GUIProp.XQUERY : GUIProp.COMMANDS);
        for(final String en : hs) {
          final JMenuItem jmi = new JMenuItem(en);
          jmi.addActionListener(al);
          pop.add(jmi);
        }
        pop.show(hist, 0, hist.getHeight());
      }
    });

    b = new BaseXBack(new BorderLayout(5, 0));
    b.add(hist, BorderLayout.WEST);
    b.add(input, BorderLayout.CENTER);
    nav.add(b, BorderLayout.CENTER);

    go = new BaseXButton(this, "go", HELPGO);
    go.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        execute();
      }
    });

    filter = BaseXButton.command(GUICommands.FILTER, this);

    b = new BaseXBack(new TableLayout(1, 3));
    b.add(go);
    b.add(Box.createHorizontalStrut(1));
    b.add(filter);
    nav.add(b, BorderLayout.EAST);

    if(gprop.is(GUIProp.SHOWINPUT)) control.add(nav, BorderLayout.SOUTH);
    top.add(control, BorderLayout.NORTH);

    // create views
    notify = new ViewNotifier(this);
    text = new TextView(notify);
    editor = new EditorView(notify);
    info = new InfoView(notify);

    // create panels for closed and opened database mode
    views = new ViewContainer(this, text, editor, info,
        new FolderView(notify), new PlotView(notify), new TableView(notify),
        new MapView(notify), new TreeView(notify), new ExploreView(notify)
    );

    top.add(views, BorderLayout.CENTER);
    setContentBorder();

    // add status bar
    status = new GUIStatus(this);
    if(gprop.is(GUIProp.SHOWSTATUS)) top.add(status, BorderLayout.SOUTH);

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    add(top);

    setVisible(true);
    views.updateViews();
    refreshControls();

    // start logo animation as thread
    new Thread() {
      @Override
      public void run() {
        checkVersion();
        views.run();
      }
    }.start();

    input.requestFocusInWindow();
  }

  @Override
  public void dispose() {
    // close opened queries
    if(!editor.confirm()) return;

    final boolean max = getExtendedState() == MAXIMIZED_BOTH;
    gprop.set(GUIProp.MAXSTATE, max);
    if(!max) {
      gprop.set(GUIProp.GUILOC, new int[] { getX(), getY() });
      gprop.set(GUIProp.GUISIZE, new int[] { getWidth(), getHeight() });
    }
    super.dispose();
    gprop.write();
    context.close();
  }

  /**
   * Executes the input of the {@link GUIInput} bar.
   */
  void execute() {
    final String in = input.getText().trim();
    final boolean cmd = mode.getSelectedIndex() == 2;
    if(cmd || in.startsWith("!")) {
      // run as command: command mode or exclamation mark as first character
      final int i = cmd ? 0 : 1;
      if(i == in.length()) return;

      try {
        // parse and run all commands
        execute(true, new CommandParser(in.substring(i), context).parse());
      } catch(final QueryException ex) {
        if(!info.visible()) GUICommands.SHOWINFO.execute(this);
        info.setInfo(ex.getMessage(), null, null, false);
        info.reset();
      }
    } else if(gprop.num(GUIProp.SEARCHMODE) == 1 || in.startsWith("/")) {
      xquery(in, true);
    } else {
      final String qu = Find.find(in, context, gprop.is(GUIProp.FILTERRT));
      execute(true, new XQuery(qu));
    }
  }

  /**
   * Launches a query. Adds the default namespace, if available.
   * The command is ignored if an update operation takes place.
   * @param qu query to be run
   * @param main main window
   */
  public void xquery(final String qu, final boolean main) {
    // check and add default namespace
    final Namespaces ns = context.data().ns;
    String in = qu.trim().isEmpty() ? "()" : qu;
    final int u = ns.uri(Token.EMPTY, 0);
    if(u != 0) in = Util.info("declare default element namespace \"%\"; %",
        ns.uri(u), in);
    execute(main, new XQuery(in));
  }

  /**
   * Launches the specified command in a separate thread.
   * The command is ignored if an update operation takes place.
   * @param cmd command to be launched
   */
  public void execute(final Command cmd) {
    execute(true, cmd);
  }

  /**
   * Launches the specified commands in a separate thread.
   * The command is ignored if an update operation takes place.
   * @param main call from main window
   * @param cmd command to be launched
   */
  public void execute(final boolean main, final Command... cmd) {
    // ignore command if updates take place
    if(updating) return;

    new Thread() {
      @Override
      public void run() {
        for(final Command c : cmd) if(!exec(c, main)) break;
      }
    }.start();
  }

  /**
   * Executes the specified command.
   * @param c command to be executed
   * @param main call from the main input field
   * @return success flag
   */
  boolean exec(final Command c, final boolean main) {
    final int thread = ++threadID;

    // wait when command is still running
    while(command != null) {
      command.stop();
      Performance.sleep(50);
      if(threadID != thread) return true;
    }

    cursor(CURSORWAIT);

    boolean ok = true;
    try {
      final Performance perf = new Performance();

      // reset current context if realtime filter is activated
      final Data data = context.data();
      if(gprop.is(GUIProp.FILTERRT) && context.current() != null &&
          !context.root()) context.update();

      // cache some variables before executing the command
      final Nodes current = context.current();
      command = c;

      // execute command and cache result
      final ArrayOutput ao = new ArrayOutput().max(gprop.num(GUIProp.MAXTEXT));
      final boolean up = c.updating(context);
      updating = up;

      // resets the query editor
      if(editor.visible()) {
        if(main) editor.reset();
        else if(c instanceof XQuery) editor.start();
      }

      // evaluate command
      String inf = null;
      try {
        c.execute(context, ao);
        inf = c.info();
      } catch(final BaseXException ex) {
        ok = false;
        inf = ex.getMessage();
      } finally {
        updating = false;
      }
      final String time = perf.getTimer();

      // show query info
      info.setInfo(inf, c, time, ok);
      info.reset();

      // show feedback in query editor
      boolean feedback = main;
      if(!main && editor.visible() && c instanceof XQuery) {
        editor.info(inf.startsWith(PROGERR) ? PROGERR : inf, ok);
        feedback = true;
      }

      // check if query feedback was evaluated in the query view
      if(!ok) {
        // display error in info view
        if((!feedback || inf.startsWith(BUGINFO)) && !info.visible()) {
          GUICommands.SHOWINFO.execute(this);
        }
      } else {
        // get query result
        final Result result = c.result();
        final Nodes nodes = result instanceof Nodes &&
          ((Nodes) result).size() != 0 ? (Nodes) result : null;

        // treat text view different to other views
        if(nodes == null) {
          // display text view
          if(!text.visible()) GUICommands.SHOWTEXT.execute(this);
          text.setText(ao, c);
        }

        final Data ndata = context.data();
        Nodes marked = context.marked;

        if(ndata != data) {
          // database reference has changed - notify views
          notify.init();
        } else if(up) {
          // update command
          notify.update();
        } else if(result != null) {
          final Nodes nd = context.current();
          final boolean flt = gprop.is(GUIProp.FILTERRT);
          if(flt || nd != null && !nd.sameAs(current)) {
            // refresh context if at least one node was found
            if(nodes != null) notify.context((Nodes) result, flt, null);
          } else if(marked != null) {
            // refresh highlight
            if(nodes != null) {
              // use query result
              marked = nodes;
            } else if(marked.size() != 0) {
              // remove old highlight
              marked = new Nodes(data);
            }
            // refresh views
            notify.mark(marked, null);
            if(thread != threadID) {
              command = null;
              return true;
            }
          }
        }
        // show number of hits
        setHits(result == null ? 0 : result.size());

        // show status info
        status.setText(Util.info(PROCTIME, time));
      }
    } catch(final Exception ex) {
      // unexpected error
      Util.stack(ex);
      Dialog.error(this, Util.info(PROCERR, c,
          !ex.toString().isEmpty() ? ex.toString() : ex.getMessage()));
      updating = false;
    }

    cursor(CURSORARROW, true);
    command = null;
    return ok;
  }

 /**
  * Stops the current process.
  */
 public void stop() {
   if(command != null) command.stop();
   cursor(CURSORARROW, true);
   command = null;
 }

 /**
  * Sets a property and displays the command in the info view.
  * @param pr property to be set
  * @param val value
  */
 public void set(final Object[] pr, final Object val) {
   set(context.prop, pr, val);
 }

 /**
  * Sets a main property and displays the command in the info view.
  * @param pr property to be set
  * @param val value
  */
 public void setMain(final Object[] pr, final Object val) {
   set(context.mprop, pr, val);
 }

 /**
  * Sets a property and displays the command in the info view.
  * @param prop property instance
  * @param pr property to be set
  * @param val value
  */
 private void set(final AProp prop, final Object[] pr, final Object val) {
   if(!prop.sameAs(pr, val)) {
     final Set cmd = new Set(pr, val);
     cmd.run(context);
     info.setInfo(cmd.info(), cmd, null, true);
   }
 }

  /**
   * Sets the border of the content area.
   */
  private void setContentBorder() {
    final int n = control.getComponentCount();
    final int n2 = top.getComponentCount();

    if(n == 0 && n2 == 2) {
      views.border(0, 0, 0, 0);
    } else {
      views.setBorder(new CompoundBorder(new EmptyBorder(3, 1, 3, 1),
          new EtchedBorder()));
    }
  }

  /**
   * Refreshes the layout.
   */
  public void updateLayout() {
    init(gprop);
    notify.layout();
    views.repaint();
  }

  /**
   * Updates the control panel.
   * @param comp component to be updated
   * @param show true if component is visible
   * @param layout component layout
   */
  public void updateControl(final JComponent comp, final boolean show,
      final String layout) {

    if(comp == status) {
      if(!show) top.remove(comp);
      else top.add(comp, layout);
    } else if(comp == menu) {
      if(!show) menuHeight = menu.getHeight();
      final int s = show ? menuHeight : 0;
      BaseXLayout.setHeight(menu, s);
      menu.setSize(menu.getWidth(), s);
    } else { // buttons, input
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
    final Nodes marked = context.marked;
    if(marked != null) setHits(marked.size());

    filter.setEnabled(marked != null && marked.size() != 0);

    final boolean inf = gprop.is(GUIProp.SHOWINFO);
    context.prop.set(Prop.QUERYINFO, inf);
    context.prop.set(Prop.XMLPLAN, inf);

    final Data data = context.data();
    final int t = mode.getSelectedIndex();
    final int s = data == null ? 2 : gprop.num(GUIProp.SEARCHMODE);

    input.help(s == 0 ? HELPSEARCHXML : s == 1 ? HELPXPATH : HELPCMD);
    mode.setEnabled(data != null);
    go.setEnabled(s == 2 || !gprop.is(GUIProp.EXECRT));

    if(s != t) {
      mode.setSelectedIndex(s);
      input.setText("");
      input.requestFocusInWindow();
    }

    toolbar.refresh();
    menu.refresh();

    final int i = context.data() == null ? 2 : gprop.num(GUIProp.SEARCHMODE);
    final String[] hs = i == 0 ? gprop.strings(GUIProp.SEARCH) : i == 1 ?
        gprop.strings(GUIProp.XQUERY) : gprop.strings(GUIProp.COMMANDS);
    hist.setEnabled(hs.length != 0);
  }

  /**
   * Sets hits information.
   * @param n number of hits
   */
  private void setHits(final long n) {
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
      if(!gprop.is(GUIProp.SHOWMENU)) GUICommands.SHOWMENU.execute(this);
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
      if(!gprop.is(GUIProp.SHOWBUTTONS))
        control.add(buttons, BorderLayout.CENTER);
      if(!gprop.is(GUIProp.SHOWINPUT)) control.add(nav, BorderLayout.SOUTH);
      if(!gprop.is(GUIProp.SHOWSTATUS)) top.add(status, BorderLayout.SOUTH);
      setJMenuBar(menu);
      add(top);
    }

    gprop.set(GUIProp.SHOWMENU, !full);
    gprop.set(GUIProp.SHOWBUTTONS, !full);
    gprop.set(GUIProp.SHOWINPUT, !full);
    gprop.set(GUIProp.SHOWSTATUS, !full);
    fullscreen = full;

    GraphicsEnvironment.getLocalGraphicsEnvironment().
      getDefaultScreenDevice().setFullScreenWindow(fullscr);
    setContentBorder();
    refreshControls();
    updateControl(menu, !full, BorderLayout.NORTH);
    setVisible(!full);
  }

  /**
   * Checks for a new version and shows a confirmation dialog.
   */
  void checkVersion() {
    final Version disk = new Version(gprop.get(GUIProp.UPDATEVERSION));
    final Version used = new Version(VERSION.replaceAll(" .*", ""));
    if(disk.compareTo(used) < 0) {
      // update version property to latest used version
      gprop.set(GUIProp.UPDATEVERSION, used.toString());
    } else {
      try {
        final String page = Token.string(new IOUrl(VERSION_URL).read());
        final Matcher m = Pattern.compile("^(Version )?([\\w\\d.]*?)( .*|$)",
            Pattern.DOTALL).matcher(page);
        if(m.matches()) {
          final Version latest = new Version(m.group(2));
          if(disk.compareTo(latest) < 0 &&
              Dialog.confirm(this, Util.info(GUICHECKVER, NAME, latest))) {
            Dialog.browse(this, UPDATE_URL);
          } else {
            // don't show update dialog anymore if it has been rejected once
            gprop.set(GUIProp.UPDATEVERSION, latest.toString());
          }
        }
      } catch(final Exception ex) {
        // ignore connection failure
      }
    }
  }
}
