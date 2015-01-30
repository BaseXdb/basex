package org.basex.gui;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.data.*;
import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.gui.view.editor.*;
import org.basex.gui.view.explore.*;
import org.basex.gui.view.folder.*;
import org.basex.gui.view.info.*;
import org.basex.gui.view.map.*;
import org.basex.gui.view.plot.*;
import org.basex.gui.view.table.*;
import org.basex.gui.view.text.*;
import org.basex.gui.view.tree.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class is the main window of the GUI. It is the central instance for user interactions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class GUI extends JFrame {
  /** Database Context. */
  public final Context context;
  /** GUI options. */
  public final GUIOptions gopts;

  /** View Manager. */
  public final ViewNotifier notify;

  /** Status line. */
  public final GUIStatus status;
  /** Input field. */
  public final GUIInput input;
  /** Filter button. */
  public final AbstractButton filter;
  /** Search view. */
  public final EditorView editor;
  /** Info view. */
  public final InfoView info;

  /** Painting flag; if activated, interactive operations are skipped. */
  public boolean painting;
  /** Updating flag; if activated, operations accessing the data are skipped. */
  public boolean updating;

  /** Fullscreen flag. */
  boolean fullscreen;
  /** Button panel. */
  final BaseXBack buttons;
  /** Navigation/input panel. */
  final BaseXBack nav;

  /** Result panel. */
  private final GUIMenu menu;
  /** Content panel, containing all views. */
  private final ViewContainer views;
  /** History button. */
  private final AbstractButton hist;
  /** Execution Button. */
  private final AbstractButton go;
  /** Execution Button. */
  private final AbstractButton stop;
  /** Current input Mode. */
  private final BaseXCombo mode;

  /** Text view. */
  private final TextView text;
  /** Top panel. */
  private final BaseXBack top;
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

  /** Password reader. */
  private static PasswordReader pwReader;

  /** Info listener. */
  private final InfoListener infoListener = new InfoListener() {
    @Override
    public void info(final String inf) {
      info.setInfo(inf, null, true, false);
    }
  };

  /**
   * Default constructor.
   * @param ctx database context
   * @param opts gui options
   */
  public GUI(final Context ctx, final GUIOptions opts) {
    this.context = ctx;
    this.gopts = opts;

    setIconImage(BaseXImages.get("logo_64"));
    setTitle();

    GUIMacOSX.enableOSXFullscreen(this);

    // set window size
    final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    final int[] loc = gopts.get(GUIOptions.GUILOC);
    final int[] size = gopts.get(GUIOptions.GUISIZE);
    final int x = Math.max(0, Math.min(scr.width - size[0], loc[0]));
    final int y = Math.max(0, Math.min(scr.height - size[1], loc[1]));
    setBounds(x, y, size[0], size[1]);
    if(gopts.get(GUIOptions.MAXSTATE)) {
      setExtendedState(MAXIMIZED_HORIZ);
      setExtendedState(MAXIMIZED_VERT);
      setExtendedState(MAXIMIZED_BOTH);
    }

    top = new BaseXBack(new BorderLayout());

    // add header
    control = new BaseXBack(new BorderLayout());

    // add menu bar
    menu = new GUIMenu(this);
    setJMenuBar(menu);

    buttons = new BaseXBack(new BorderLayout());
    toolbar = new GUIToolBar(TOOLBAR, this);
    buttons.add(toolbar, BorderLayout.WEST);

    hits = new BaseXLabel(" ");
    hits.setFont(hits.getFont().deriveFont(18f));
    hits.setHorizontalAlignment(SwingConstants.RIGHT);

    BaseXBack b = new BaseXBack();
    b.add(hits);

    buttons.add(b, BorderLayout.EAST);
    if(gopts.get(GUIOptions.SHOWBUTTONS)) control.add(buttons, BorderLayout.CENTER);

    nav = new BaseXBack(new BorderLayout(5, 0)).border(2, 2, 0, 2);

    mode = new BaseXCombo(this, FIND, XQUERY, COMMAND);
    mode.setSelectedIndex(2);

    mode.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final int s = mode.getSelectedIndex();
        if(s == gopts.get(GUIOptions.SEARCHMODE) || !mode.isEnabled()) return;

        gopts.set(GUIOptions.SEARCHMODE, s);
        input.mode(mode.getSelectedItem());
        refreshControls();
      }
    });
    nav.add(mode, BorderLayout.WEST);

    input = new GUIInput(this);
    input.mode(mode.getSelectedItem());

    hist = BaseXButton.get("c_hist", INPUT_HISTORY, false, this);
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
        final int i = context.data() == null ? 2 : gopts.get(GUIOptions.SEARCHMODE);
        final String[] hs = gopts.get(
            i == 0 ? GUIOptions.SEARCH : i == 1 ? GUIOptions.XQUERY : GUIOptions.COMMANDS);
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

    stop = BaseXButton.get("c_stop", STOP, false, this);
    stop.setEnabled(false);
    stop.addActionListener(new ActionListener() {
      @Override
     public void actionPerformed(final ActionEvent e) {
        if(command != null) {
          command.stop();
          stop.setEnabled(false);
        }
      }
    });

    go = BaseXButton.get("c_go", RUN_QUERY, false, this);
    go.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        input.store();
        execute();
      }
    });

    filter = BaseXButton.command(GUIMenuCmd.C_FILTER, this);

    b = new BaseXBack(new TableLayout(1, 3, 1, 0));
    b.add(stop);
    b.add(go);
    b.add(filter);
    nav.add(b, BorderLayout.EAST);

    if(gopts.get(GUIOptions.SHOWINPUT)) control.add(nav, BorderLayout.SOUTH);
    top.add(control, BorderLayout.NORTH);

    // create views
    notify = new ViewNotifier(this);
    text = new TextView(notify);
    editor = new EditorView(notify);
    info = new InfoView(notify);

    // create panels for closed and opened database mode
    views = new ViewContainer(this, text, editor, info, new FolderView(notify),
        new PlotView(notify), new TableView(notify), new MapView(notify), new TreeView(notify),
        new ExploreView(notify));

    top.add(views, BorderLayout.CENTER);
    setContentBorder();

    // add status bar
    status = new GUIStatus(this);
    if(gopts.get(GUIOptions.SHOWSTATUS)) top.add(status, BorderLayout.SOUTH);

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    add(top);

    setVisible(true);
    views.updateViews();
    refreshControls();

    // check version
    checkVersion();
    input.requestFocusInWindow();
  }

  @Override
  public void dispose() {
    // close opened queries
    if(!editor.confirm()) return;

    final boolean max = getExtendedState() == MAXIMIZED_BOTH;
    gopts.set(GUIOptions.MAXSTATE, max);
    if(!max) {
      gopts.set(GUIOptions.GUILOC, new int[] { getX(), getY()});
      gopts.set(GUIOptions.GUISIZE, new int[] { getWidth(), getHeight()});
    }
    super.dispose();
    gopts.write();
    context.close();
  }

  /**
   * Sets the window title.
   */
  public void setTitle() {
    final TokenBuilder tb = new TokenBuilder();
    final EditorArea ea = editor == null ? null : editor.getEditor();
    if(ea != null) {
      if(ea.opened()) {
        tb.add(ea.file().path());
      } else {
        tb.add(ea.file().name());
      }
      if(ea.modified()) tb.add('*');
    }
    final Data data = context.data();
    if(data != null) {
      if(!tb.isEmpty()) tb.add(' ');
      tb.add("[").add(data.meta.name).add("]");
    }
    if(!tb.isEmpty()) tb.add(" - ");
    tb.add(Prop.TITLE);
    setTitle(tb.toString());
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
   * Executes the input of the {@link GUIInput} bar.
   */
  void execute() {
    final String in = input.getText().trim();
    final boolean cmd = mode.getSelectedIndex() == 2;
    // run as command: command mode or exclamation mark as first character
    final boolean exc = in.startsWith("!");
    if(cmd || exc) {
      try {
        // parse and execute all commands
        final CommandParser cp = new CommandParser(in.substring(exc ? 1 : 0), context);
        if(pwReader == null) pwReader = new PasswordReader() {
          @Override
          public String password() {
            final DialogPass dp = new DialogPass(GUI.this);
            return dp.ok() ? dp.password() : "";
          }
        };
        cp.pwReader(pwReader);
        execute(false, cp.parse());
      } catch(final QueryException ex) {
        if(!info.visible()) GUIMenuCmd.C_SHOWINFO.execute(this);
        info.setInfo(Util.message(ex), null, false, true);
      }
    } else if(gopts.get(GUIOptions.SEARCHMODE) == 1 || in.startsWith("/")) {
      xquery(in, false);
    } else {
      execute(false, new Find(in, gopts.get(GUIOptions.FILTERRT)));
    }
  }

  /**
   * Launches a query. Adds the default namespace if available. The command is ignored if an update
   * operation takes place.
   * @param qu query to be run
   * @param edit editor panel
   */
  public void xquery(final String qu, final boolean edit) {
    // check and add default namespace
    final Data data = context.data();
    final Namespaces ns = data.nspaces;
    String in = qu.trim().isEmpty() ? "()" : qu;
    final int u = ns.uri(Token.EMPTY, 0, data);
    if(u != 0) in = Util.info("declare default element namespace \"%\"; %", ns.uri(u), in);
    execute(edit, new XQuery(in));
  }

  /**
   * Launches the specified command in a separate thread. The command is ignored if an update
   * operation takes place.
   * @param cmd command to be launched
   */
  public void execute(final Command cmd) {
    execute(false, cmd);
  }

  /**
   * Launches the specified commands in a separate thread. The command is ignored if an update
   * operation takes place.
   * @param edit call from editor view
   * @param cmd command to be launched
   */
  public void execute(final boolean edit, final Command... cmd) {
    // ignore command if updates take place
    if(updating) return;

    final Thread t = new Thread() {
      @Override
      public void run() {
        if(cmd.length == 0) info.setInfo("", null, true, true);
        for(final Command c : cmd)
          if(!exec(c, edit)) break;
      }
    };
    t.setDaemon(true);
    t.start();
  }

  /**
   * Executes the specified command.
   * @param cmd command to be executed
   * @param edit call from editor panel
   * @return success flag
   */
  private boolean exec(final Command cmd, final boolean edit) {
    // wait when command is still running
    final int thread = ++threadID;
    while(true) {
      final Command c = command;
      if(c == null) break;
      c.stop();
      Thread.yield();
      if(threadID != thread) return true;
    }
    cursor(CURSORWAIT);
    input.setCursor(CURSORWAIT);
    stop.setEnabled(true);

    boolean ok = true;
    try {
      final Performance perf = new Performance();

      final Data data = context.data();
      // reset current context if realtime filter is activated
      if(gopts.get(GUIOptions.FILTERRT) && data != null && !context.root()) context.invalidate();

      // remember current command and context nodes
      final DBNodes current = context.current();
      command = cmd;

      // execute command and cache result
      final ArrayOutput ao = new ArrayOutput();
      ao.setLimit(gopts.get(GUIOptions.MAXTEXT));
      updating = cmd.updating(context);

      // updates the query editor
      if(edit) editor.start();

      // reset visualizations if data reference will be changed
      if(cmd.newData(context)) notify.init();
      // attaches the info listener to the command
      cmd.listen(infoListener);

      // evaluate command
      String inf = null;
      Throwable cause = null;
      try {
        cmd.execute(context, ao);
        inf = cmd.info();
      } catch(final BaseXException ex) {
        cause = ex.getCause();
        if(cause == null) cause = ex;
        ok = false;
        inf = Util.message(ex);
      } finally {
        updating = false;
      }
      final String time = perf.getTime();

      // show query info
      info.setInfo(inf, cmd, time, ok, true);

      // sends feedback to the query editor
      final boolean stopped = inf.endsWith(INTERRUPTED);
      if(edit) editor.info(cause, stopped, true);

      // check if query feedback was evaluated in the query view
      if(!ok && !stopped) {
        // display error in info view
        text.setText(ao);
        if((!edit || inf.startsWith(S_BUGINFO)) && !info.visible()) {
          GUIMenuCmd.C_SHOWINFO.execute(this);
        }
      } else {
        // get query result
        final Result result = cmd.finish();
        DBNodes nodes = result instanceof DBNodes && result.size() != 0 ? (DBNodes) result : null;

        if(context.data() != data) {
          // database reference has changed - notify views
          notify.init();
        } else if(cmd.updated(context)) {
          // update visualizations
          notify.update();
          // adopt updated nodes as result set
          if(nodes == null) nodes = context.current();
        } else if(result != null) {
          // check if result has changed
          final boolean flt = gopts.get(GUIOptions.FILTERRT);
          final DBNodes curr = context.current();
          if(flt || curr != null && !curr.equals(current)) {
            // refresh context if at least one node was found
            if(nodes != null) notify.context(nodes, flt, null);
          } else if(context.marked != null) {
            // refresh highlight
            DBNodes m = context.marked;
            if(nodes != null) {
              // use query result
              m = nodes;
            } else if(m.size() != 0) {
              // remove old highlight
              m = new DBNodes(data);
            }
            // refresh views
            if(context.marked != m) notify.mark(m, null);
          }
        }

        if(thread == threadID && !stopped) {
          // show status info
          status.setText(Util.info(TIME_NEEDED_X, time));
          // show number of hits
          if(result != null) setResults(result.size());

          if(nodes == null) {
            // make text view visible
            if(!text.visible() && ao.size() != 0) GUIMenuCmd.C_SHOWRESULT.execute(this);
            // assign textual output if no node result was created
            text.setText(ao);
          }
          text.cacheText(ao, cmd, result);
        }
      }
    } catch(final Exception ex) {
      // unexpected error
      BaseXDialog.error(this, Util.info(EXEC_ERROR, cmd, Util.bug(ex)));
      updating = false;
    }
    stop();
    return ok;
  }

  /**
   * Stops the current process.
   */
  public void stop() {
    if(command != null) command.stop();
    cursor(CURSORARROW, true);
    input.setCursor(CURSORTEXT);
    stop.setEnabled(false);
    command = null;
  }

  /**
   * Sets an option if its value differs from current value and displays the command in the info
   * view.
   * @param <T> option type
   * @param <V> value type
   * @param opt option to be set
   * @param val value
   */
  public <T extends Option<V>, V> void set(final T opt, final V val) {
    if(!context.options.get(opt).toString().equals(val.toString())) {
      final Set cmd = new Set(opt, val);
      cmd.run(context);
      info.setInfo(cmd.info(), cmd, true, false);
    }
  }

  /**
   * Sets the border of the content area.
   */
  private void setContentBorder() {
    final int n = control.getComponentCount();
    final int n2 = top.getComponentCount();

    if(n == 0 && n2 == 2) {
      views.border(0);
    } else {
      views.setBorder(new CompoundBorder(BaseXLayout.border(3, 1, 3, 1),
          BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
    }
  }

  /**
   * Refreshes the layout.
   */
  public void updateLayout() {
    init(gopts);
    notify.layout();
    views.repaint();
  }

  /**
   * Updates the control panel.
   * @param comp component to be updated
   * @param show true if component is visible
   * @param layout component layout
   */
  void updateControl(final JComponent comp, final boolean show, final String layout) {
    if(comp == status) {
      if(show) top.add(comp, layout);
      else top.remove(comp);
    } else if(comp == menu) {
      if(!show) menuHeight = menu.getHeight();
      final int s = show ? menuHeight : 0;
      comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, s));
      menu.setSize(menu.getWidth(), s);
    } else { // buttons, input
      if(show) control.add(comp, layout);
      else control.remove(comp);
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
    final DBNodes marked = context.marked;
    if(marked != null) setResults(marked.size());

    filter.setEnabled(marked != null && marked.size() != 0);

    final boolean inf = gopts.get(GUIOptions.SHOWINFO);
    context.options.set(MainOptions.QUERYINFO, inf);
    context.options.set(MainOptions.XMLPLAN, inf);

    final Data data = context.data();
    final int t = mode.getSelectedIndex();
    final int s = data == null ? 2 : gopts.get(GUIOptions.SEARCHMODE);

    mode.setEnabled(data != null);
    go.setEnabled(s == 2 || !gopts.get(GUIOptions.EXECRT));

    if(s != t) {
      mode.setSelectedIndex(s);
      input.mode(mode.getSelectedItem());
      input.requestFocusInWindow();
    }

    toolbar.refresh();
    menu.refresh();

    final int i = context.data() == null ? 2 : gopts.get(GUIOptions.SEARCHMODE);
    final StringsOption options =
        i == 0 ? GUIOptions.SEARCH : i == 1 ? GUIOptions.XQUERY : GUIOptions.COMMANDS;
    hist.setEnabled(gopts.get(options).length != 0);
  }

  /**
   * Sets results information.
   * @param n number of results
   */
  private void setResults(final long n) {
    int mh = context.options.get(MainOptions.MAXHITS);
    if(mh < 0) mh = Integer.MAX_VALUE;
    hits.setText(Util.info(RESULTS_X, (n >= mh ? "\u2265" : "") + n));
  }

  /**
   * Toggles fullscreen mode.
   */
  void fullscreen() {
    fullscreen ^= true;
    fullscreen(fullscreen);
  }

  /**
   * Turns fullscreen mode on/off.
   * @param full fullscreen flag
   */
  public void fullscreen(final boolean full) {
    if(full ^ fullscr == null) return;

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
      if(!gopts.get(GUIOptions.SHOWBUTTONS)) control.add(buttons, BorderLayout.CENTER);
      if(!gopts.get(GUIOptions.SHOWINPUT)) control.add(nav, BorderLayout.SOUTH);
      if(!gopts.get(GUIOptions.SHOWSTATUS)) top.add(status, BorderLayout.SOUTH);
      setJMenuBar(menu);
      add(top);
    }

    gopts.set(GUIOptions.SHOWBUTTONS, !full);
    gopts.set(GUIOptions.SHOWINPUT, !full);
    gopts.set(GUIOptions.SHOWSTATUS, !full);
    fullscreen = full;

    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(
        fullscr);
    setContentBorder();
    refreshControls();
    updateControl(menu, !full, BorderLayout.NORTH);
    setVisible(!full);
  }

  /**
   * Starts a new thread that checks for new versions.
   */
  private void checkVersion() {
    // ignore snapshots and beta versions
    if(Strings.contains(Prop.VERSION, ' ')) return;

    final Thread t = new Thread() {
      @Override
      public void run() {
        final Version disk = new Version(gopts.get(GUIOptions.UPDATEVERSION));
        final Version used = new Version(Prop.VERSION);

        if(disk.compareTo(used) < 0) {
          // update version option to latest used version
          writeVersion(used);
        } else {
          try {
            final String page = Token.string(new IOUrl(Prop.VERSION_URL).read());
            final Matcher m = Pattern.compile("^(Version )?([\\w\\d.]*?)( .*|$)",
                Pattern.DOTALL).matcher(page);
            if(m.matches()) {
              final Version latest = new Version(m.group(2));
              if(disk.compareTo(latest) < 0) {
                if(BaseXDialog.confirm(GUI.this, Util.info(H_NEW_VERSION, Prop.NAME, latest))) {
                  // jump to browser
                  BaseXDialog.browse(GUI.this, Prop.UPDATE_URL);
                } else {
                  // don't show update dialog anymore if it has been rejected once
                  writeVersion(latest);
                }
              }
            }
          } catch(final Exception ex) {
            // ignore connection failure
          }
        }
      }
    };
    t.setDaemon(true);
    SwingUtilities.invokeLater(t);
  }

  /**
   * Writes a version to the options.
   * @param version version
   */
  private void writeVersion(final Version version) {
    gopts.set(GUIOptions.UPDATEVERSION, version.toString());
    gopts.write();
  }
}
