package org.basex.gui;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.util.concurrent.atomic.*;
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
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class is the main window of the GUI. It is the central instance for user interactions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class GUI extends JFrame implements BaseXWindow {
  /** Database Context. */
  public final Context context;
  /** GUI options. */
  public final GUIOptions gopts;

  /** view notifier. */
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
  /** Indicates if a running command or operation is updating. */
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
  private final BaseXLabel results;
  /** Buttons. */
  private final GUIToolBar toolbar;

  /** Currently executed command ({@code null} otherwise). */
  private volatile Command command;
  /** ID of currently executed command. */
  private final AtomicInteger commandID = new AtomicInteger(0);
  /** Indicates if a command is running. */
  private boolean running;

  /** Menu panel height. */
  private int menuHeight;
  /** Fullscreen Window. */
  private JFrame fullscr;

  /** Password reader. */
  private static volatile PasswordReader pwReader;

  /**
   * Default constructor.
   * @param context database context
   * @param gopts gui options
   */
  public GUI(final Context context, final GUIOptions gopts) {
    this.context = context;
    this.gopts = gopts;

    if(Prop.MAC) GUIMacOS.init(this);
    setIconImage(BaseXImages.get("logo_64"));
    setTitle();

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

    results = new BaseXLabel(" ").border(0, 0, 0, 4).resize(1.7f);
    results.setHorizontalAlignment(SwingConstants.RIGHT);

    BaseXBack b = new BaseXBack();
    b.add(results);

    buttons.add(b, BorderLayout.EAST);
    if(this.gopts.get(GUIOptions.SHOWBUTTONS)) control.add(buttons, BorderLayout.CENTER);

    mode = new BaseXCombo(this, FIND, XQUERY, COMMAND);
    mode.setSelectedIndex(2);

    input = new GUIInput(this);
    input.mode(mode.getSelectedItem());

    mode.addActionListener(e -> {
      final int s = mode.getSelectedIndex();
      if(s == gopts.get(GUIOptions.SEARCHMODE) || !mode.isEnabled()) return;

      gopts.set(GUIOptions.SEARCHMODE, s);
      input.mode(mode.getSelectedItem());
    });

    b = new BaseXBack(new BorderLayout(4, 0));
    b.add(input, BorderLayout.CENTER);

    nav = new BaseXBack(new BorderLayout(5, 0)).border(2, 2, 0, 2);
    nav.add(mode, BorderLayout.WEST);
    nav.add(b, BorderLayout.CENTER);

    stop = BaseXButton.get("c_stop", STOP, false, this);
    stop.setEnabled(false);
    stop.addActionListener(e -> {
      if(command != null) {
        command.stop();
        stop.setEnabled(false);
      }
    });

    final AbstractButton go = BaseXButton.get("c_go", RUN_QUERY, false, this);
    go.addActionListener(e -> execute());

    filter = BaseXButton.command(GUIMenuCmd.C_FILTER, this);

    b = new BaseXBack(new ColumnLayout(1));
    b.add(stop);
    b.add(go);
    b.add(filter);
    nav.add(b, BorderLayout.EAST);

    if(this.gopts.get(GUIOptions.SHOWINPUT)) control.add(nav, BorderLayout.SOUTH);
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
    if(this.gopts.get(GUIOptions.SHOWSTATUS)) top.add(status, BorderLayout.SOUTH);

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    add(top);

    setVisible(true);
    views.updateViews();
    refreshControls(true);

    // check version
    checkVersion();
  }

  @Override
  public void dispose() {
    saveOptions();
    // check if all modified texts are saved or closed
    if(editor.confirm(null)) {
      context.close();
      super.dispose();
    }
  }

  /**
   * Saves the current configuration.
   */
  public void saveOptions() {
    gopts.setFiles(GUIOptions.OPEN, editor.openFiles());
    final boolean max = getExtendedState() == MAXIMIZED_BOTH;
    gopts.set(GUIOptions.MAXSTATE, max);
    if(!max) {
      gopts.set(GUIOptions.GUILOC, new int[] { getX(), getY()});
      gopts.set(GUIOptions.GUISIZE, new int[] { getWidth(), getHeight()});
    }
    gopts.write();
    context.soptions.write();
  }

  /**
   * Sets the window title.
   */
  public void setTitle() {
    final TokenBuilder tb = new TokenBuilder();
    final EditorArea ea = editor == null ? null : editor.getEditor();
    if(ea != null) {
      tb.add(ea.opened() ? ea.file().path() : ea.file().name());
      if(ea.modified()) tb.add('*');
    }
    final Data data = context.data();
    if(data != null) {
      if(!tb.isEmpty()) tb.add(' ');
      tb.add("[").add(data.meta.name).add("]");
    }
    if(!tb.isEmpty()) tb.add(" - ");
    tb.add(TITLE);
    setTitle(tb.toString());
  }

  /**
   * Sets a cursor.
   * @param cursor cursor to be set
   */
  public void cursor(final Cursor cursor) {
    cursor(cursor, false);
  }

  /**
   * Sets a cursor, enforcing a new look if necessary.
   * @param cursor cursor to be set
   * @param enforce enforce new cursor
   */
  public void cursor(final Cursor cursor, final boolean enforce) {
    final Cursor cc = getCursor();
    if(cc != cursor && (cc != CURSORWAIT || enforce)) setCursor(cursor);
  }

  /**
   * Executes the input of the {@link GUIInput} bar.
   */
  void execute() {
    final String in = input.getText().trim();
    final boolean cmd = mode.getSelectedIndex() == 2;
    // run as command: command mode or exclamation mark as first character
    final boolean exc = Strings.startsWith(in, '!');
    if(cmd || exc) {
      try {
        // parse and execute all commands
        final CommandParser cp = CommandParser.get(in.substring(exc ? 1 : 0), context);
        if(pwReader == null) pwReader = () -> {
          final DialogPass dp = new DialogPass(this);
          return dp.ok() ? dp.password() : "";
        };
        cp.pwReader(pwReader);
        execute(cp.parse());
      } catch(final QueryException ex) {
        if(!info.visible()) GUIMenuCmd.C_SHOWINFO.execute(this);
        info.setInfo(Util.message(ex), null, false, true);
      }
    } else if(gopts.get(GUIOptions.SEARCHMODE) == 1 || Strings.startsWith(in, '/')) {
      simpleQuery(in);
    } else {
      execute(new Find(in, gopts.get(GUIOptions.FILTERRT)));
    }
  }

  /**
   * Launches a simple single-line query. Adds the default namespace if available.
   * @param query expression to be run
   */
  public void simpleQuery(final String query) {
    // check and add default namespace
    String q = query.trim().isEmpty() ? "()" : query;
    final Data data = context.data();
    final Namespaces ns = data.nspaces;
    final int uriId = ns.uriIdForPrefix(Token.EMPTY, 0, data);
    if(uriId != 0) q = Util.info("declare default element namespace \"%\"; %", ns.uri(uriId), q);
    execute(new XQuery(q));
  }

  /**
   * Launches the specified commands in a separate thread.
   * Commands are ignored if an update operation takes place.
   * @param cmd commands to be executed
   */
  public void execute(final Command... cmd) {
    execute(false, cmd);
  }

  /**
   * Launches the specified commands in a separate thread.
   * Commands are ignored if an update operation takes place.
   * @param edit call from editor view
   * @param cmds commands to be executed
   */
  public void execute(final boolean edit, final Command... cmds) {
    // ignore command if updates take place
    if(updating) return;

    new Thread(() -> {
      if(cmds.length == 0) info.setInfo("", null, true, true);
      for(final Command cmd : cmds) {
        if(!exec(cmd, edit)) break;
      }
    }).start();
  }

  /**
   * Executes the specified command.
   * @param cmd command to be executed
   * @param edit called from editor view
   * @return success flag
   */
  private boolean exec(final Command cmd, final boolean edit) {
    // wait when command is still running
    final int id = commandID.incrementAndGet();
    while(true) {
      final Command c = command;
      if(c == null) break;
      c.stop();
      Performance.sleep(1);
      if(commandID.get() != id) return true;
    }

    // indicate to the user that the command will be executed
    cursor(CURSORWAIT);
    input.setCursor(CURSORWAIT);
    stop.setEnabled(true);
    if(edit) editor.pleaseWait(id);

    final Data data = context.data();
    // reset current context if realtime filter is activated
    if(gopts.get(GUIOptions.FILTERRT) && data != null && !context.root()) context.invalidate();

    // remember current command and context nodes
    final DBNodes current = context.current();
    command = cmd;

    // execute command and cache result
    final ArrayOutput output = new ArrayOutput();
    output.setLimit(gopts.get(GUIOptions.MAXTEXT));
    // sets the maximum number of hits
    cmd.maxResults(gopts.get(GUIOptions.MAXRESULTS));
    // attaches the info listener to the command
    cmd.jc().tracer = info;

    final Performance perf = new Performance();
    boolean ok = true;
    try {
      running = true;
      updating = cmd.updating(context);

      // reset visualizations if data reference may be changed by command
      if(cmd.newData(context)) notify.init();

      // evaluate command
      String inf;
      Throwable cause = null;
      try {
        cmd.execute(context, output);
        inf = cmd.info();
      } catch(final BaseXException ex) {
        cause = ex.getCause();
        if(cause == null) cause = ex;
        ok = false;
        inf = Util.message(ex);
      } finally {
        updating = false;
        running = false;
      }

      // show query info, send feedback to query editor
      final String time = info.setInfo(inf, cmd, perf.getTime(), ok, true);
      final boolean stopped = inf.substring(inf.lastIndexOf('\n') + 1).equals(INTERRUPTED);
      if(edit) editor.info(cause, stopped, true);

      // get query result and node references to currently opened database
      final Value result = cmd.result();
      DBNodes nodes = result instanceof DBNodes ? (DBNodes) result : null;

      // show text view if a non-empty result does not reference the currently opened database
      if(!text.visible() && output.size() != 0 && nodes == null) {
        GUIMenuCmd.C_SHOWRESULT.execute(this);
      }

      // check if query feedback was evaluated in the query view
      if(!ok && !stopped) {
        // display error in info view
        text.setText(output, 0);
        if(!info.visible() && (!edit || inf.startsWith(S_BUGINFO))) {
          GUIMenuCmd.C_SHOWINFO.execute(this);
        }
      } else {
        final boolean updated = cmd.updated(context);
        if(context.data() != data) {
          // database reference has changed - notify views
          notify.init();
        } else if(updated) {
          // update visualizations
          notify.update();
          // adopt updated nodes as result set
          if(nodes == null && result == Empty.VALUE) nodes = context.current();
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
            } else if(!m.isEmpty()) {
              // remove old highlighting
              m = new DBNodes(data);
            }
            // refresh views
            if(context.marked != m) notify.mark(m, null);
          }
        }

        if(id == commandID.get() && !stopped) {
          // refresh editor info
          editor.refreshContextLabel();
          // show status info
          status.setText(TIME_REQUIRED + COLS + time);
          // show number of hits
          if(result != null) results.setText(gopts.results(result.size(), 0));
          // assign textual output if no node result was created
          if(nodes == null) text.setText(output, result != null ? result.size() : 0);
          // only cache output if data has not been updated (in which case notifyUpdate was called)
          if(!updated) text.cache(output, cmd, result);
        }
      }
    } catch(final Exception ex) {
      // unexpected error
      BaseXDialog.error(this, Util.info(EXEC_ERROR_X_X, cmd, Util.bug(ex)));
      updating = false;
    }
    stop();
    return ok;
  }

  /**
   * Stops the current command.
   */
  public void stop() {
    if(command != null) command.stop();
    cursor(CURSORARROW, true);
    input.setCursor(CURSORTEXT);
    stop.setEnabled(false);
    command = null;
  }

  /**
   * Checks if a command with the specified id is still running.
   * @param id command id
   * @return result of check
   */
  public boolean running(final int id) {
    return id == commandID.get() && running;
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
    final Component frame = fullscr == null ? getRootPane() : fullscr;
    frame.validate();
    refreshControls(false);
  }

  /**
   * Updates the view layout.
   */
  public void layoutViews() {
    views.updateViews();
    refreshControls(true);
  }

  /**
   * Refreshes the menu and the buttons.
   * @param result update number of results
   */
  public void refreshControls(final boolean result) {
    final DBNodes marked = context.marked;
    if(result && marked != null) {
      results.setText(gopts.results((marked.isEmpty() ? context.current() : marked).size(), 0));
    }

    filter.setEnabled(marked != null && !marked.isEmpty());

    final boolean inf = gopts.get(GUIOptions.SHOWINFO);
    context.options.set(MainOptions.QUERYINFO, inf);
    context.options.set(MainOptions.XMLPLAN, inf);

    final Data data = context.data();
    mode.setEnabled(data != null);
    final int m = data == null ? 2 : gopts.get(GUIOptions.SEARCHMODE);
    if(mode.getSelectedIndex() != m) {
      mode.setSelectedIndex(m);
      input.mode(mode.getSelectedItem());
    }

    toolbar.refresh();
    menu.refresh();
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
    updateControl(menu, !full, BorderLayout.NORTH);
    setVisible(!full);
  }

  /**
   * Starts a new thread that checks for new versions.
   */
  private void checkVersion() {
    // ignore snapshots and beta versions
    if(Strings.contains(Prop.VERSION, ' ')) return;

    new GUIWorker<Version>() {
      @Override
      protected Version doInBackground() throws Exception {
        final Version disk = new Version(gopts.get(GUIOptions.UPDATEVERSION));
        final Version used = new Version(Prop.VERSION);

        if(disk.compareTo(used) < 0) {
          // update version option to latest used version
          writeVersion(used);
        } else {
          final String page = Token.string(new IOUrl(VERSION_URL).read());
          final Matcher m = Pattern.compile("^(Version )?([\\w\\d.]*?)( .*|$)",
              Pattern.DOTALL).matcher(page);
          if(m.matches()) {
            final Version latest = new Version(m.group(2));
            if(disk.compareTo(latest) < 0) return latest;
          }
        }
        return null;
      }

      @Override
      protected void done(final Version latest) {
        if(BaseXDialog.confirm(GUI.this, Util.info(H_NEW_VERSION, Prop.NAME, latest))) {
          // jump to browser
          BaseXDialog.browse(GUI.this, UPDATE_URL);
        } else {
          // don't show update dialog anymore if it has been rejected once
          writeVersion(latest);
        }
      }

      private void writeVersion(final Version version) {
        gopts.set(GUIOptions.UPDATEVERSION, version.toString());
        saveOptions();
      }
    }.execute();
  }

  @Override
  public GUI gui() {
    return this;
  }

  @Override
  public BaseXDialog dialog() {
    return null;
  }

  @Override
  public GUI component() {
    return this;
  }
}
