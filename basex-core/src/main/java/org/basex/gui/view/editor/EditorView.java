package org.basex.gui.view.editor;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.layout.BaseXLayout.DropHandler;
import org.basex.gui.text.*;
import org.basex.gui.text.TextPanel.Action;
import org.basex.gui.view.*;
import org.basex.gui.view.project.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.parse.json.*;
import org.basex.io.parse.xml.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;

/**
 * This view allows the input and evaluation of queries and documents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class EditorView extends View {
  /** Link pattern. */
  private static final Pattern LINK = Pattern.compile("(.*?), ([0-9]+)/([0-9]+)");
  /** Number of files in the history. */
  private static final int HISTORY = 18;
  /** Number of files in the compact history. */
  private static final int HISTCOMP = 7;

  /** History Button. */
  private final AbstractButton hist;
  /** Stop Button. */
  private final AbstractButton stop;
  /** Go button. */
  private final AbstractButton go;
  /** Search bar. */
  private final SearchBar search;
  /** Info label. */
  private final BaseXLabel info;
  /** Position label. */
  private final BaseXLabel pos;
  /** Splitter. */
  private final BaseXSplit split;
  /** Header string. */
  private final BaseXLabel label;
  /** Query area. */
  private final BaseXTabs tabs;
  /** Query file that has last been evaluated. */
  private IOFile execFile;
  /** Thread counter. */
  private int threadID;

  /** Project files. */
  final ProjectView project;
  /** Input info. */
  InputInfo errorInfo;

  /**
   * Default constructor.
   * @param man view manager
   */
  public EditorView(final ViewNotifier man) {
    super(EDITORVIEW, man);
    layout(new BorderLayout());

    label = new BaseXLabel(EDITOR, true, false);
    label.setForeground(GRAY);

    tabs = new BaseXTabs(gui);
    tabs.setFocusable(Prop.MAC);
    tabs.addDragDrop(false);
    addCreateTab();

    final SearchEditor center = new SearchEditor(gui, tabs, null);
    search = center.bar();

    final AbstractButton openB = BaseXButton.command(GUIMenuCmd.C_EDITOPEN, gui);
    final AbstractButton saveB = BaseXButton.get("c_save", SAVE, false, gui);
    final AbstractButton find = search.button(FIND_REPLACE);
    hist = BaseXButton.get("c_hist", RECENTLY_OPENED, false, gui);

    stop = BaseXButton.get("c_stop", STOP, false, gui);
    stop.addKeyListener(this);
    stop.setEnabled(false);

    go = BaseXButton.get("c_go", BaseXLayout.addShortcut(EXECUTE_QUERY, BaseXKeys.EXEC1.toString()),
        false, gui);
    go.addKeyListener(this);

    final BaseXBack buttons = new BaseXBack(Fill.NONE);
    buttons.layout(new TableLayout(1, 7, 1, 0)).border(0, 0, 8, 0);
    buttons.add(openB);
    buttons.add(saveB);
    buttons.add(hist);
    buttons.add(find);
    buttons.add(Box.createHorizontalStrut(6));
    buttons.add(stop);
    buttons.add(go);

    final BaseXBack north = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    north.add(buttons, BorderLayout.WEST);
    north.add(label, BorderLayout.EAST);

    // status and query pane
    search.editor(addTab(), false);

    info = new BaseXLabel().setText(OK, Msg.SUCCESS);
    pos = new BaseXLabel(" ");
    posCode.invokeLater();

    final BaseXBack south = new BaseXBack(Fill.NONE).border(10, 0, 2, 0);
    south.layout(new BorderLayout(4, 0));
    south.add(info, BorderLayout.CENTER);
    south.add(pos, BorderLayout.EAST);

    final BaseXBack main = new BaseXBack().border(5).mode(Fill.NONE);
    main.layout(new BorderLayout());
    main.add(north, BorderLayout.NORTH);
    main.add(center, BorderLayout.CENTER);
    main.add(south, BorderLayout.SOUTH);

    project = new ProjectView(this);
    split = new BaseXSplit(true);
    split.mode(Fill.NONE);
    split.add(project);
    split.add(main);
    split.init(new double[] { 0.3, 0.7 }, new double[] { 0, 1 });
    project();
    add(split, BorderLayout.CENTER);

    refreshLayout();

    // add listeners
    saveB.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu pop = new JPopupMenu();
        final StringBuilder mnem = new StringBuilder();
        final JMenuItem sa = GUIMenu.newItem(GUIMenuCmd.C_EDITSAVE, gui, mnem);
        final JMenuItem sas = GUIMenu.newItem(GUIMenuCmd.C_EDITSAVEAS, gui, mnem);
        sa.setEnabled(GUIMenuCmd.C_EDITSAVE.enabled(gui));
        sas.setEnabled(GUIMenuCmd.C_EDITSAVEAS.enabled(gui));
        pop.add(sa);
        pop.add(sas);
        pop.show(saveB, 0, saveB.getHeight());
      }
    });
    hist.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu pm = new JPopupMenu();
        ActionListener al = new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent ac) {
            // rewrite and open chosen file
            open(new IOFile(ac.getActionCommand().replaceAll("(.*) \\[(.*)\\]", "$2/$1")));
          }
        };

        // create popup menu with of recently opened files
        final StringList opened = new StringList();
        for(final EditorArea ea : editors()) opened.add(ea.file.path());

        final StringList hst = new StringList(HISTORY);
        final StringList all = new StringList(gui.gopts.get(GUIOptions.EDITOR));
        final int fl = Math.min(all.size(), e == null ? HISTORY : HISTCOMP);
        for(int f = 0; f < fl; f++) hst.add(all.get(f));

        Font f = null;
        for(final String en : hst.sort(Prop.CASE)) {
          // disable opened files
          final JMenuItem it = new JMenuItem(en.replaceAll("(.*)[/\\\\](.*)", "$2 [$1]"));
          if(opened.contains(en)) {
            if(f == null) f = it.getFont().deriveFont(Font.BOLD);
            it.setFont(f);
          }
          pm.add(it).addActionListener(al);
        }

        al = new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent ac) {
            hist.getActionListeners()[0].actionPerformed(null);
          }
        };
        if(e != null && pm.getComponentCount() == HISTCOMP) {
          pm.add(new JMenuItem("...")).addActionListener(al);
        }

        pm.show(hist, 0, hist.getHeight());
      }
    });
    refreshHistory(null);
    info.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        jumpToError();
      }
    });
    stop.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        stop.setEnabled(false);
        go.setEnabled(false);
        gui.stop();
      }
    });
    go.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        run(getEditor(), Action.EXECUTE);
      }
    });
    tabs.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        final EditorArea ea = getEditor();
        if(ea == null) return;
        search.editor(ea, true);
        gui.refreshControls();
        posCode.invokeLater();
        run(ea, Action.PARSE);
      }
    });

    BaseXLayout.addDrop(this, new DropHandler() {
      @Override
      public void drop(final Object file) {
        if(file instanceof File) open(new IOFile((File) file));
      }
    });

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // remember opened files; don't complain about missing files
        for(final String file : gui.gopts.get(GUIOptions.OPEN)) {
          open(new IOFile(file), false, false);
        }
      }
    });
  }

  @Override
  public void refreshInit() { }

  @Override
  public void refreshFocus() { }

  @Override
  public void refreshMark() {
    go.setEnabled(getEditor().script || !gui.gopts.get(GUIOptions.EXECRT));
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) { }

  @Override
  public void refreshLayout() {
    label.border(-6, 0, 0, 2).setFont(lfont);
    for(final EditorArea edit : editors()) edit.setFont(mfont);
    search.refreshLayout();
    final Font ef = GUIConstants.font.deriveFont(7f + (fontSize >> 1));
    info.setFont(ef);
    pos.setFont(ef);
  }

  @Override
  public void refreshUpdate() { }

  @Override
  public boolean visible() {
    return gui.gopts.get(GUIOptions.SHOWEDITOR);
  }

  @Override
  public void visible(final boolean v) {
    gui.gopts.set(GUIOptions.SHOWEDITOR, v);
  }

  @Override
  protected boolean db() {
    return false;
  }

  /**
   * Shows or hides the project view.
   */
  public void showProject() {
    if(!gui.gopts.get(GUIOptions.SHOWPROJECT)) {
      gui.gopts.invert(GUIOptions.SHOWPROJECT);
      split.visible(true);
    }
  }

  /**
   * Toggles the project view.
   */
  public void project() {
    final boolean show = gui.gopts.get(GUIOptions.SHOWPROJECT);
    split.visible(show);
    if(show) {
      project.focus();
    } else {
      getEditor().requestFocusInWindow();
    }
  }

  /**
   * Focuses the project view.
   */
  public void focusFilter() {
    project.focusFilter(getEditor().searchString());
  }

  /**
   * Focuses the currently edited file in the project view.
   */
  public void jumpToFile() {
    final EditorArea editor = getEditor();
    if(editor.opened()) project.jump(editor.file);
  }

  /**
   * Switches the current editor tab.
   * @param next next next/previous tag
   */
  public void tab(final boolean next) {
    final int s = tabs.getTabCount() - 1;
    final int i = (s + tabs.getSelectedIndex() + (next ? 1 : -1)) % s;
    tabs.setSelectedIndex(i);
  }

  /**
   * Opens a new file.
   */
  public void open() {
    // open file chooser for XML creation
    final BaseXFileChooser fc = new BaseXFileChooser(OPEN, gui.gopts.get(GUIOptions.WORKPATH), gui);
    fc.filter(XQUERY_FILES, IO.XQSUFFIXES);
    fc.filter(BXS_FILES, IO.BXSSUFFIX);
    fc.textFilters();

    final IOFile[] open = fc.multi().selectAll(Mode.FOPEN);
    for(final IOFile f : open) open(f);
  }

  /**
   * Reverts the contents of the currently opened editor.
   */
  public void reopen() {
    getEditor().reopen(true);
  }

  /**
   * Saves the contents of the currently opened editor.
   * @return {@code false} if operation was canceled
   */
  public boolean save() {
    final EditorArea edit = getEditor();
    return edit.opened() ? edit.save() : saveAs();
  }

  /**
   * Saves the contents of the currently opened editor under a new name.
   * @return {@code false} if operation was canceled
   */
  public boolean saveAs() {
    // open file chooser for XML creation
    final EditorArea edit = getEditor();
    final String path = edit.opened() ? edit.file.path() : gui.gopts.get(GUIOptions.WORKPATH);
    final BaseXFileChooser fc = new BaseXFileChooser(SAVE_AS, path, gui);
    fc.filter(XQUERY_FILES, IO.XQSUFFIXES);
    fc.filter(BXS_FILES, IO.BXSSUFFIX);
    fc.textFilters();
    fc.suffix(IO.XQSUFFIX);

    // save new file
    final IOFile file = fc.select(Mode.FSAVE);
    if(file == null) return false;

    // success: display new file in project view
    edit.save(file);
    return true;
  }

  /**
   * Creates a new file.
   */
  public void newFile() {
    if(!visible()) GUIMenuCmd.C_SHOWEDITOR.execute(gui);
    refreshControls(addTab(), true);
  }

  /**
   * Deletes a file.
   * @param file file to be deleted
   * @return success flag
   */
  public boolean delete(final IOFile file) {
    final EditorArea edit = find(file, true);
    if(edit != null) close(edit);
    return file.delete();
  }

  /**
   * Opens and parses the specified query file.
   * @param file query file
   * @return opened editor, or {@code null} if file could not be opened
   */
  public EditorArea open(final IOFile file) {
    return open(file, true, true);
  }

  /**
   * Opens the specified query file.
   * @param file query file
   * @param parse parse contents
   * @param error display error if file does not exist
   * @return opened editor, or {@code null} if file could not be opened
   */
  private EditorArea open(final IOFile file, final boolean parse, final boolean error) {
    if(!visible()) GUIMenuCmd.C_SHOWEDITOR.execute(gui);

    EditorArea edit = find(file, true);
    if(edit != null) {
      // display open file
      tabs.setSelectedComponent(edit);
      edit.reopen(true);
    } else {
      try {
        // check and retrieve content
        final byte[] text = read(file);
        if(text == null) return null;

        // get current editor
        edit = getEditor();
        // create new tab if current text is stored on disk or has been modified
        if(edit.opened() || edit.modified) edit = addTab();
        edit.initText(text);
        edit.file(file);
        if(parse) run(edit, Action.PARSE);
      } catch(final IOException ex) {
        refreshHistory(null);
        Util.debug(ex);
        if(error) BaseXDialog.error(gui, Util.info(FILE_NOT_OPENED_X, file));
        return null;
      }
    }
    return edit;
  }

  /**
   * Parses or evaluates the current file.
   * @param action action
   * @param editor current editor
   */
  void run(final EditorArea editor, final Action action) {
    refreshControls(editor, false);

    final byte[] in = editor.getText();
    final boolean eq = eq(in, editor.last);
    if(eq && action == Action.CHECK) return;

    if(action == Action.EXECUTE && gui.gopts.get(GUIOptions.SAVERUN)) {
      for(final EditorArea edit : editors()) {
        if(edit.opened()) edit.save();
      }
    }

    IOFile file = editor.file;
    editor.last = in;

    final boolean xquery = file.hasSuffix(IO.XQSUFFIXES) || !file.path().contains(".");
    editor.script = !xquery && file.hasSuffix(IO.BXSSUFFIX);
    if(action == Action.EXECUTE && editor.script) {
      // execute query if forced, or if realtime execution is activated
      gui.execute(true, new Execute(string(in)));
    } else if(xquery || action == Action.EXECUTE) {
      // check if input is/might be an xquery main module
      String input = in.length == 0 ? "()" : string(in);
      boolean lib = QueryProcessor.isLibrary(input);
      final boolean exec = action == Action.EXECUTE || gui.gopts.get(GUIOptions.EXECRT);
      if(exec && lib) {
        final EditorArea ea = execEditor();
        if(ea != null) {
          file = ea.file;
          input = string(ea.getText());
          lib = false;
        }
      }

      gui.context.options.set(MainOptions.QUERYPATH, file.path());
      if(!lib && exec) {
        // execute query if forced, or if realtime execution is activated
        gui.execute(true, new XQuery(input));
        execFile = file;
      } else {
        // parse query
        final QueryContext qc = new QueryContext(gui.context);
        try {
          qc.parse(input, lib, null, null);
          info(null);
        } catch(final QueryException ex) {
          info(ex);
        } finally {
          qc.close();
        }
      }
    } else if(file.hasSuffix(IO.JSONSUFFIX)) {
      try {
        final IOContent io = new IOContent(in);
        io.name(file.path());
        JsonConverter.get(new JsonParserOptions()).convert(io);
        info(null);
      } catch(final IOException ex) {
        info(ex);
      }
    } else if(editor.script || file.hasSuffix(IO.XMLSUFFIXES) || file.hasSuffix(IO.XSLSUFFIXES)) {
      final ArrayInput ai = new ArrayInput(in);
      try {
        // check XML syntax
        if(startsWith(in, '<') || !editor.script) new XmlParser().parse(ai);
        // check command script
        if(editor.script) new CommandParser(string(in), gui.context).parse();
        info(null);
      } catch(final Exception ex) {
        info(ex);
      }
    } else if(action != Action.CHECK) {
      info(null);
    }
  }

  /**
   * Evaluates the info message resulting from command or query parsing.
   * @param ex exception, or {@code null}
   */
  private void info(final Exception ex) {
    info(ex, false, false);
  }

  /**
   * Returns the editor that has been executed last.
   * @return editor
   */
  private EditorArea execEditor() {
    if(execFile != null) {
      for(final EditorArea edit : editors()) {
        if(edit.file.path().equals(execFile.path())) return edit;
      }
    }
    execFile = null;
    return null;
  }

  /**
   * Retrieves the contents of the specified file, or opens it externally.
   * @param file query file
   * @return contents, or {@code null} reference
   * @throws IOException I/O exception
   */
  private byte[] read(final IOFile file) throws IOException {
    // check content
    final TokenBuilder text = new TokenBuilder((int) Math.min(Integer.MAX_VALUE, file.length()));
    final TextInput ti = new NewlineInput(file).validate(true);
    boolean valid = true;
    try {
      while(true) {
        try {
          final int cp = ti.read();
          if(cp == -1) return text.array();
          text.add(cp);
        } catch(final InputException ex) {
          if(valid) {
            valid = false;
            final Boolean binary = BaseXDialog.yesNoCancel(gui, H_FILE_BINARY);
            if(binary == null) return null;
            if(binary) {
              try {
                file.open();
              } catch(final IOException ioex) {
                Desktop.getDesktop().open(file.file());
              }
              return null;
            }
          }
        }
      }
    } finally {
      ti.close();
    }
  }

  /**
   * Refreshes the list of recent query files and updates the query path.
   * @param file new file
   */
  void refreshHistory(final IO file) {
    final StringList paths = new StringList();
    if(file != null) {
      final String path = file.path();
      gui.gopts.set(GUIOptions.WORKPATH, file.dirPath());
      paths.add(path);
      tabs.setToolTipTextAt(tabs.getSelectedIndex(), path);
    }
    final String[] old = gui.gopts.get(GUIOptions.EDITOR);
    for(int p = 0; paths.size() < HISTORY && p < old.length; p++) {
      final IO fl = IO.get(old[p]);
      if(fl.exists() && !fl.eq(file)) paths.add(fl.path());
    }
    // store sorted history
    gui.gopts.set(GUIOptions.EDITOR, paths.toArray());
    hist.setEnabled(!paths.isEmpty());
  }

  /**
   * Closes an editor.
   * @param edit editor to be closed. {@code null} closes the currently opened editor.
   * @return {@code true} if editor was closed
   */
  public boolean close(final EditorArea edit) {
    final EditorArea ea = edit != null ? edit : getEditor();
    if(!confirm(ea)) return false;

    // remove reference to last executed file
    if(execFile != null && ea.file.path().equals(execFile.path())) execFile = null;
    tabs.remove(ea);
    final int t = tabs.getTabCount();
    final int i = tabs.getSelectedIndex();
    if(t == 1) {
      // no panels left: close search bar
      search.deactivate(true);
      // reopen single tab and focus project listener
      addTab();
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() { project(); }
      });
    } else if(i + 1 == t) {
      // if necessary, activate last editor tab
      tabs.setSelectedIndex(i - 1);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() { getEditor().requestFocusInWindow(); }
      });
    }
    return true;
  }

  /**
   * Starts a thread, which shows a waiting info after a short timeout.
   */
  public void start() {
    final int thread = threadID;
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(200);
        if(thread == threadID) {
          info.setText(PLEASE_WAIT_D, Msg.SUCCESS).setToolTipText(null);
          stop.setEnabled(true);
        }
      }
    }.start();
  }

  /**
   * Processes the result from a command or query execution.
   * @param th exception, or {@code null}
   * @param stopped {@code true} if evaluation was interrupted
   * @param refresh refresh buttons
   */
  public void info(final Throwable th, final boolean stopped, final boolean refresh) {
    // do not refresh view when query is running
    if(!refresh && stop.isEnabled()) return;

    ++threadID;
    getEditor().resetError();

    if(refresh) {
      stop.setEnabled(false);
      refreshMark();
    }

    if(stopped || th == null) {
      info.setCursor(CURSORARROW);
      info.setText(stopped ? INTERRUPTED : OK, Msg.SUCCESS);
      info.setToolTipText(null);
      errorInfo = null;
    } else {
      info.setCursor(CURSORHAND);
      info.setText(th.getLocalizedMessage(), Msg.ERROR);
      final String tt = th.getMessage().replace("<", "&lt;").replace(">", "&gt;").replaceAll(
          "\r?\n", "<br/>").replaceAll("(<br/>.*?)<br/>.*", "$1");
      info.setToolTipText("<html>" + tt + "</html>");

      if(th instanceof QueryIOException) {
        errorInfo = ((QueryIOException) th).getCause().info();
      } else if(th instanceof QueryException) {
        errorInfo = ((QueryException) th).info();
      } else if(th instanceof SAXParseException) {
        final SAXParseException ex = (SAXParseException) th;
        final String path = getEditor().file.path();
        errorInfo = new InputInfo(path, ex.getLineNumber(), ex.getColumnNumber());
      } else {
        errorInfo = new InputInfo(getEditor().file.path(), 1, 1);
      }
      error(false);
    }
  }

  /**
   * Jumps to the current error.
   */
  public void jumpToError() {
    error(true);
  }

  /**
   * Jumps to the specified file and position.
   * @param link link
   */
  public void jump(final String link) {
    final Matcher m = LINK.matcher(link);
    if(m.matches()) {
      errorInfo = new InputInfo(m.group(1), toInt(m.group(2)), toInt(m.group(3)));
      error(true);
    } else {
      Util.stack("No match found: " + link);
    }
  }

  /**
   * Jumps to the current error.
   * @param jump jump to error position
   */
  private void error(final boolean jump) {
    final InputInfo ei = errorInfo;
    if(ei == null) return;

    final IOFile file = new IOFile(ei.path());
    EditorArea edit = find(file, false);
    if(jump) {
      if(edit == null) edit = open(file, false, true);
      if(edit != null) tabs.setSelectedComponent(edit);
    }
    if(edit == null) return;

    // mark and jump to error position
    final int ep = pos(edit.last, ei.line(), ei.column());
    edit.error(ep);
    if(jump) {
      edit.setCaret(ep);
      posCode.invokeLater();
    }
  }

  /**
   * Returns an editor offset for the specified line and column.
   * @param text text
   * @param line line
   * @param col column
   * @return position
   */
  private int pos(final byte[] text, final int line, final int col) {
    final int ll = text.length;
    int ep = ll;
    for(int p = 0, l = 1, c = 1; p < ll; ++c, p += cl(text, p)) {
      if(l > line || l == line && c == col) {
        ep = p;
        break;
      }
      if(text[p] == '\n') {
        ++l;
        c = 0;
      }
    }
    if(ep < ll && Character.isLetterOrDigit(cp(text, ep))) {
      while(ep > 0 && Character.isLetterOrDigit(cp(text, ep - 1))) ep--;
    }
    return ep;
  }

  /**
   * Shows a quit dialog for all modified query files.
   * @return result of check
   */
  public boolean confirm() {
    // save modified files
    for(final EditorArea edit : editors()) {
      tabs.setSelectedComponent(edit);
      if(!confirm(edit)) return false;
    }
    // remember opened files
    final StringList files = new StringList();
    for(final EditorArea edit : editors()) {
      if(edit.opened()) files.add(edit.file.path());
    }
    gui.gopts.set(GUIOptions.OPEN, files.toArray());
    return true;
  }

  /**
   * Checks if the current text can be saved or reverted.
   * @return result of check
   */
  public boolean modified() {
    final EditorArea edit = getEditor();
    return edit.modified || !edit.opened();
  }

  /**
   * Returns the current editor.
   * @return editor
   */
  public EditorArea getEditor() {
    final Component c = tabs.getSelectedComponent();
    return c instanceof EditorArea ? (EditorArea) c : null;
  }

  /**
   * Updates the references to renamed files.
   * @param old old file file reference
   * @param renamed updated file reference
   */
  public void rename(final IOFile old, final IOFile renamed) {
    try {
      // use canonical representation and add slash to names of directories
      final boolean dir = renamed.isDir();
      final String oldPath = old.file().getCanonicalPath() + (dir ? File.separator : "");
      // iterate through all tabs
      final int s = tabs.getTabCount() - 1;
      for(int i = 0; i < s; i++) {
        final Component c = tabs.getComponentAt(i);
        if(!(c instanceof EditorArea)) continue;

        final EditorArea ea = (EditorArea) c;
        final String editPath = ea.file.file().getCanonicalPath();
        if(dir) {
          // change path to files in a renamed directory
          if(editPath.startsWith(oldPath)) {
            ea.file = new IOFile(renamed + File.separator + editPath.substring(oldPath.length()));
          }
        } else if(oldPath.equals(editPath)) {
          // update file reference and label of editor tab
          ea.file = renamed;
          ea.label.setText(renamed.name());
          break;
        }
      }
    } catch(final IOException ex) {
      Util.errln(ex);
    }
  }

  /**
   * Refreshes the query modification flag.
   * @param edit editor
   * @param force action
   */
  void refreshControls(final EditorArea edit, final boolean force) {
    // update modification flag
    final boolean oe = edit.modified;
    edit.modified = edit.hist != null && edit.hist.modified();
    if(edit.modified == oe && !force) return;

    // update tab title
    String title = edit.file.name();
    if(edit.modified) title += '*';
    edit.label.setText(title);

    // update components
    gui.refreshControls();
    posCode.invokeLater();
  }

  /** Code for setting cursor position. */
  public final GUICode posCode = new GUICode() {
    @Override
    public void execute(final Object arg) {
      final int[] lc = getEditor().pos();
      pos.setText(lc[0] + " : " + lc[1]);
    }
  };

  /**
   * Finds the editor that contains the specified file.
   * @param file file to be found
   * @param opened considers only opened files
   * @return editor
   */
  EditorArea find(final IO file, final boolean opened) {
    for(final EditorArea edit : editors()) {
      if(edit.file.eq(file) && (!opened || edit.opened())) return edit;
    }
    return null;
  }

  /**
   * Choose a unique tab file.
   * @return io reference
   */
  private IOFile newTabFile() {
    // collect numbers of existing files
    final BoolList bl = new BoolList();
    for(final EditorArea edit : editors()) {
      if(edit.opened()) continue;
      final String n = edit.file.name().substring(FILE.length());
      bl.set(n.isEmpty() ? 1 : Integer.parseInt(n), true);
    }
    // find first free file number
    int c = 0;
    while(++c < bl.size() && bl.get(c));
    // create io reference
    return new IOFile(gui.gopts.get(GUIOptions.WORKPATH), FILE + (c == 1 ? "" : c));
  }

  /**
   * Adds a new editor tab.
   * @return editor reference
   */
  EditorArea addTab() {
    final EditorArea edit = new EditorArea(this, newTabFile());
    edit.setFont(mfont);

    final BaseXBack tab = new BaseXBack(new BorderLayout(10, 0)).mode(Fill.NONE);
    tab.add(edit.label, BorderLayout.CENTER);

    final AbstractButton close = tabButton("e_close", "e_close2");
    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        close(edit);
      }
    });
    tab.add(close, BorderLayout.EAST);

    tabs.add(edit, tab, tabs.getComponentCount() - 2);
    return edit;
  }

  /**
   * Adds a tab for creating new tabs.
   */
  private void addCreateTab() {
    final AbstractButton add = tabButton("e_new", "e_new2");
    add.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        refreshControls(addTab(), true);
      }
    });
    tabs.add(new BaseXBack(), add, 0);
    tabs.setEnabledAt(0, false);
  }

  /**
   * Adds a new tab button.
   * @param icon name of button icon
   * @param rollover rollover icon
   * @return button
   */
  private AbstractButton tabButton(final String icon, final String rollover) {
    final AbstractButton b = BaseXButton.get(icon, null, false, gui);
    b.setBorder(new EmptyBorder(2, 0, 2, 0));
    b.setContentAreaFilled(false);
    b.setFocusable(false);
    b.setRolloverIcon(BaseXImages.icon(rollover));
    return b;
  }

  /**
   * Shows a quit dialog for the specified editor.
   * @param edit editor to be saved
   * @return {@code false} if confirmation was canceled
   */
  private boolean confirm(final EditorArea edit) {
    if(edit.modified && (edit.opened() || edit.getText().length != 0)) {
      final Boolean ok = BaseXDialog.yesNoCancel(gui, Util.info(CLOSE_FILE_X, edit.file.name()));
      if(ok == null || ok && !save()) return false;
    }
    return true;
  }

  /**
   * Returns all editors.
   * @return editors
   */
  EditorArea[] editors() {
    final ArrayList<EditorArea> edits = new ArrayList<EditorArea>();
    for(final Component c : tabs.getComponents()) {
      if(c instanceof EditorArea) edits.add((EditorArea) c);
    }
    return edits.toArray(new EditorArea[edits.size()]);
  }
}
