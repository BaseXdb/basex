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

/**
 * This class is the main window of the GUI. It is the central instance
 * for user interactions.
 *
 * @author BaseX Team 2005-12, BSD License
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
  /** Result panel. */
  private final GUIMenu menu;
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

  /** Password reader. */
  public static final PasswordReader READER = new PasswordReader() {
    @Override
    public String password() {
      final DialogPass dp = new DialogPass();
      return dp.ok() ? Token.md5(dp.pass()) : "";
    }
  };

  /** Info listener. */
  public final InfoListener infoListener = new InfoListener() {
    @Override
    public void info(final String inf) {
      info.setInfo(inf, null, true, false);
    }
  };

  /**
   * Default constructor.
   * @param ctx database context
   * @param gprops gui properties
   */
  public GUI(final Context ctx, final GUIProp gprops) {
    super(ctx, gprops);
    GUIMacOSX.enableOSXFullscreen(this);

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
    if(gprop.is(GUIProp.SHOWBUTTONS)) control.add(buttons, BorderLayout.CENTER);

    nav = new BaseXBack(new BorderLayout(5, 0)).border(2, 2, 0, 2);

    mode = new BaseXCombo(this, SEARCH, XQUERY, COMMAND);
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

    hist = new BaseXButton(this, "hist", H_SHOW_HISTORY);
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
        final int i = context.data() == null ? 2 : gprop.num(GUIProp.SEARCHMODE);
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

    go = new BaseXButton(this, "go", H_EXECUTE_QUERY);
    go.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        execute();
      }
    });

    filter = BaseXButton.command(GUICommands.C_FILTER, this);

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
        views.run();
        checkVersion();
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
        final CommandParser cp = new CommandParser(in.substring(i), context);
        cp.pwReader(READER);

        // parse and execute all commands
        execute(false, cp.parse());
      } catch(final QueryException ex) {
        if(!info.visible()) GUICommands.C_SHOWINFO.execute(this);
        info.setInfo(Util.message(ex), null, false, true);
      }
    } else if(gprop.num(GUIProp.SEARCHMODE) == 1 || in.startsWith("/")) {
      xquery(in, false);
    } else {
      final String qu = Find.find(in, context, gprop.is(GUIProp.FILTERRT));
      execute(false, new XQuery(qu));
    }
  }

  /**
   * Launches a query. Adds the default namespace, if available.
   * The command is ignored if an update operation takes place.
   * @param qu query to be run
   * @param edit editor panel
   */
  public void xquery(final String qu, final boolean edit) {
    // check and add default namespace
    final Namespaces ns = context.data().nspaces;
    String in = qu.trim().isEmpty() ? "()" : qu;
    final int u = ns.uri(Token.EMPTY, 0);
    if(u != 0) in = Util.info("declare default element namespace \"%\"; %",
        ns.uri(u), in);
    execute(edit, new XQuery(in));
  }

  /**
   * Launches the specified command in a separate thread.
   * The command is ignored if an update operation takes place.
   * @param cmd command to be launched
   */
  public void execute(final Command cmd) {
    execute(false, cmd);
  }

  /**
   * Launches the specified commands in a separate thread.
   * The command is ignored if an update operation takes place.
   * @param edit call from editor view
   * @param cmd command to be launched
   */
  public void execute(final boolean edit, final Command... cmd) {
    // ignore command if updates take place
    if(updating) return;

    new Thread() {
      @Override
      public void run() {
        for(final Command c : cmd) if(!exec(c, edit)) break;
      }
    }.start();
  }

  /**
   * Executes the specified command.
   * @param cmd command to be executed
   * @param edit call from editor panel
   * @return success flag
   */
  boolean exec(final Command cmd, final boolean edit) {
    // wait when command is still running
    final int thread = ++threadID;
    while(command != null) {
      command.stop();
      Performance.sleep(50);
      if(threadID != thread) return true;
    }
    cursor(CURSORWAIT);

    boolean ok = true;
    try {
      final Performance perf = new Performance();

      final Data data = context.data();
      // reset current context if realtime filter is activated
      if(gprop.is(GUIProp.FILTERRT) && data != null && !context.root()) context.update();

      // remember current command and context nodes
      final Nodes current = context.current();
      command = cmd;

      // execute command and cache result
      final ArrayOutput ao = new ArrayOutput().max(gprop.num(GUIProp.MAXTEXT));
      updating = cmd.updating(context);

      // updates the query editor
      if(edit) editor.start();

      // reset visualizations if data reference will be changed
      if(cmd.newData(context)) notify.init();
      // attaches the info listener to the command
      cmd.listen(infoListener);

      // evaluate command
      String inf = null;
      try {
        cmd.execute(context, ao);
        inf = cmd.info();
      } catch(final BaseXException ex) {
        ok = false;
        inf = Util.message(ex);
      } finally {
        updating = false;
      }
      final String time = perf.getTime();

      // show query info

      if(info.visible()) info.setInfo(inf, cmd, time, ok, true);

      // sends feedback to the query editor
      final boolean interrupted = inf.endsWith(INTERRUPTED);
      if(edit) {
        editor.info(interrupted ? INTERRUPTED : ok ? OK : inf, ok || interrupted, true);
      }

      // check if query feedback was evaluated in the query view
      if(!ok && !interrupted) {
        // display error in info view
        if((!edit || inf.startsWith(BUGINFO)) && !info.visible()) {
          GUICommands.C_SHOWINFO.execute(this);
        }
      } else {
        // get query result
        final Result result = cmd.result();
        Nodes nodes = result instanceof Nodes && result.size() != 0 ?
          (Nodes) result : null;

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
          final boolean flt = gprop.is(GUIProp.FILTERRT);
          final Nodes nd = context.current();
          if(flt || nd != null && !nd.sameAs(current)) {
            // refresh context if at least one node was found
            if(nodes != null) notify.context((Nodes) result, flt, null);
          } else if(context.marked != null) {
            // refresh highlight
            Nodes m = context.marked;
            if(nodes != null) {
              // use query result
              m = nodes;
            } else if(m.size() != 0) {
              // remove old highlight
              m = new Nodes(data);
            }
            // refresh views
            if(context.marked != m) notify.mark(m, null);
          }
        }

        if(thread == threadID && !interrupted) {
          // show status info
          status.setText(Util.info(TIME_NEEDED_X, time));
          // show number of hits
          if(result != null) setResults(result.size());

          if(nodes == null) {
            // make text view visible
            if(!text.visible() && ao.size() != 0) GUICommands.C_SHOWTEXT.execute(this);
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
  * Sets a property and displays the command in the info view.
  * @param prop property instance
  * @param pr property to be set
  * @param val value
  */
 private void set(final AProp prop, final Object[] pr, final Object val) {
   if(!prop.sameAs(pr, val)) {
     final Set cmd = new Set(pr, val);
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
    if(marked != null) setResults(marked.size());

    filter.setEnabled(marked != null && marked.size() != 0);

    final boolean inf = gprop.is(GUIProp.SHOWINFO);
    context.prop.set(Prop.QUERYINFO, inf);
    context.prop.set(Prop.XMLPLAN, inf);

    final Data data = context.data();
    final int t = mode.getSelectedIndex();
    final int s = data == null ? 2 : gprop.num(GUIProp.SEARCHMODE);

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
    final Object[] options = i == 0 ? GUIProp.SEARCH : i == 1 ?
      GUIProp.XQUERY : GUIProp.COMMANDS;
    hist.setEnabled(gprop.strings(options).length != 0);
  }

  /**
   * Sets results information.
   * @param n number of results
   */
  private void setResults(final long n) {
    int mh = context.prop.num(Prop.MAXHITS);
    if(mh < 0) mh = Integer.MAX_VALUE;
    hits.setText(Util.info(RESULTS_X, (n >= mh ? "\u2265" : "") + n));
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
      if(!gprop.is(GUIProp.SHOWBUTTONS))
        control.add(buttons, BorderLayout.CENTER);
      if(!gprop.is(GUIProp.SHOWINPUT)) control.add(nav, BorderLayout.SOUTH);
      if(!gprop.is(GUIProp.SHOWSTATUS)) top.add(status, BorderLayout.SOUTH);
      setJMenuBar(menu);
      add(top);
    }

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
    final Version used = new Version(Prop.VERSION.replaceAll(" .*", ""));
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
          if(disk.compareTo(latest) < 0 && BaseXDialog.confirm(this,
              Util.info(H_NEW_VERSION, Prop.NAME, latest))) {
            BaseXDialog.browse(this, UPDATE_URL);
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
