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
import javax.swing.event.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.editor.Editor.Action;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.layout.BaseXLayout.DropHandler;
import org.basex.gui.view.*;
import org.basex.gui.view.project.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This view allows the input and evaluation of queries and documents.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class EditorView extends View {
  /** Number of files in the history. */
  private static final int HISTORY = 18;
  /** Number of files in the compact history. */
  private static final int HISTCOMP = 7;
  /** XQuery error pattern. */
  private static final Pattern XQERROR = Pattern.compile(
      "(.*?), ([0-9]+)/([0-9]+)" + COL);
  /** XML error pattern. */
  private static final Pattern XMLERROR = Pattern.compile(
      LINE_X.replaceAll("%", "(.*?)") + COL + ".*");
  /** Error information pattern. */
  private static final Pattern ERRORINFO = Pattern.compile(
      "^.*\r?\n\\[.*?\\] |" + LINE_X.replaceAll("%", "\\d+") + COLS + "|\r?\n.*",
      Pattern.DOTALL);
  /** Error tooltip pattern. */
  private static final Pattern ERRORTT = Pattern.compile(
      "^.*\r?\n" + STOPPED_AT + "|\r?\n" + STACK_TRACE + COL + ".*", Pattern.DOTALL);

  /** Search bar. */
  private final SearchBar search;
  /** History Button. */
  private final BaseXButton hist;
  /** Execute Button. */
  private final BaseXButton stop;
  /** Info label. */
  private final BaseXLabel info;
  /** Position label. */
  private final BaseXLabel pos;
  /** Execute button. */
  private final BaseXButton go;
  /** Splitter. */
  private final BaseXSplit split;
  /** Project files. */
  private final ProjectView project;
  /** Header string. */
  private final BaseXLabel label;
  /** Filter button. */
  private final BaseXButton filter;
  /** Query area. */
  private final BaseXTabs tabs;

  /** Sizes. */
  private double[] sizes = new double[] { 0.3, 0.7 };
  /** Thread counter. */
  private int threadID;

  /** Most recent error position; used for clicking on error message. */
  private int errPos;
  /** File in which the most recent error occurred. */
  public IOFile errFile;
  /** Last error message. */
  public String errMsg;

  /**
   * Default constructor.
   * @param man view manager
   */
  public EditorView(final ViewNotifier man) {
    super(EDITORVIEW, man);
    layout(new BorderLayout());

    label = new BaseXLabel(EDITOR, true, false);
    label.setForeground(GRAY);

    final BaseXButton openB = BaseXButton.command(GUICommands.C_EDITOPEN, gui);
    final BaseXButton saveB = new BaseXButton(gui, "save", H_SAVE);
    hist = new BaseXButton(gui, "hist", H_RECENTLY_OPEN);
    final BaseXButton find = new BaseXButton(gui, "find",
        BaseXLayout.addShortcut(H_REPLACE, BaseXKeys.FIND.toString()));

    stop = new BaseXButton(gui, "stop", H_STOP_PROCESS);
    stop.addKeyListener(this);
    stop.setEnabled(false);

    go = new BaseXButton(gui, "go",
        BaseXLayout.addShortcut(H_EXECUTE_QUERY, BaseXKeys.EXEC1.toString()));
    go.addKeyListener(this);

    filter = BaseXButton.command(GUICommands.C_FILTER, gui);
    filter.addKeyListener(this);
    filter.setEnabled(false);

    final BaseXBack buttons = new BaseXBack(Fill.NONE);
    buttons.layout(new TableLayout(1, 8, 1, 0)).border(0, 0, 8, 0);
    buttons.add(openB);
    buttons.add(saveB);
    buttons.add(hist);
    buttons.add(find);
    buttons.add(Box.createHorizontalStrut(6));
    buttons.add(stop);
    buttons.add(go);
    buttons.add(filter);

    final BaseXBack north = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    north.add(buttons, BorderLayout.WEST);
    north.add(label, BorderLayout.EAST);

    tabs = new BaseXTabs(gui);
    tabs.setFocusable(Prop.MAC);
    final SearchEditor center = new SearchEditor(gui, tabs, null).button(find);
    search = center.bar();
    addCreateTab();

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
    split.sizes(sizes);
    project();
    add(split, BorderLayout.CENTER);

    refreshLayout();

    // add listeners
    saveB.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu pop = new JPopupMenu();
        final StringBuilder mnem = new StringBuilder();
        final JMenuItem sa = GUIMenu.newItem(GUICommands.C_EDITSAVE, gui, mnem);
        final JMenuItem sas = GUIMenu.newItem(GUICommands.C_EDITSAVEAS, gui, mnem);
        GUICommands.C_EDITSAVE.refresh(gui, sa);
        GUICommands.C_EDITSAVEAS.refresh(gui, sas);
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
        getEditor().release(Action.EXECUTE);
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
        // remember opened files
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
    final EditorArea edit = getEditor();
    go.setEnabled(edit.script || !gui.gopts.get(GUIOptions.EXECRT));
    final Nodes m = gui.context.marked;
    filter.setEnabled(!gui.gopts.get(GUIOptions.FILTERRT) && m != null && m.size() != 0);
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
   * Toggles the project view.
   */
  public void project() {
    if(gui.gopts.get(GUIOptions.SHOWPROJECT)) {
      split.sizes(sizes);
      if(gui.editor != null) gui.editor.focus(false);
    } else {
      sizes = split.sizes(new double[] { 0, 1 });
      if(gui.editor != null) gui.editor.getEditor().requestFocusInWindow();
    }
  }

  /**
   * Focuses the project view.
   * @param filt focus filter or content
   */
  public void focus(final boolean filt) {
    project.focus(filt);
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
    return edit.opened() ? save(edit.file) : saveAs();
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

    final IOFile file = fc.select(Mode.FSAVE);
    if(file == null || !save(file)) return false;

    project.refresh(file.dir());
    return true;
  }

  /**
   * Creates a new file.
   */
  public void newFile() {
    if(!visible()) GUICommands.C_SHOWEDITOR.execute(gui);
    addTab();
    refreshControls(true);
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
    if(!visible()) GUICommands.C_SHOWEDITOR.execute(gui);

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
        if(parse) edit.release(Action.PARSE);
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
   * Retrieves the contents of the specified file.
   * @param file query file
   * @return contents, or {@code null} reference
   * @throws IOException I/O exception
   */
  private byte[] read(final IOFile file) throws IOException {
    // check content
    final BufferInput bi = new BufferInput(file);
    final byte[] buffer = new byte[IO.BLOCKSIZE];
    try {
      final int size = Math.max(0, bi.read(buffer) - 4);
      for(int c = 0; c < size; c += cl(buffer, c)) {
        if(!XMLToken.valid(cp(buffer, c))) {
          if(!BaseXDialog.confirm(gui, H_FILE_BINARY)) break;
          try {
            file.open();
          } catch(final IOException ex) {
            Desktop.getDesktop().open(file.file());
          }
          return null;
        }
      }
    } finally {
      bi.close();
    }
    return file.read();
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
   * @param edit editor to be closed. {@code null} closes the currently
   * opened editor.
   * @return {@code true} if editor was closed
   */
  public boolean close(final EditorArea edit) {
    final EditorArea ea = edit != null ? edit : getEditor();
    if(!confirm(ea)) return false;

    tabs.remove(ea);
    final int t = tabs.getTabCount();
    final int i = tabs.getSelectedIndex();
    if(t == 1) {
      // reopen single tab and focus project listener
      addTab();
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() { focus(false); }
      });
    } else if(i + 1 == t) {
      // if necessary, activate last editor tab
      tabs.setSelectedIndex(i - 1);
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
   * Evaluates the info message resulting from a parsed or executed query.
   * @param msg info message
   * @param ok {@code true} if evaluation was successful
   * @param refresh refresh buttons
   */
  public void info(final String msg, final boolean ok, final boolean refresh) {
    // do not refresh view when query is running
    if(!refresh && stop.isEnabled()) return;

    ++threadID;
    errPos = -1;
    errFile = null;
    errMsg = null;
    getEditor().resetError();

    if(refresh) {
      stop.setEnabled(false);
      refreshMark();
    }

    if(ok) {
      info.setCursor(CURSORARROW);
      info.setText(msg, Msg.SUCCESS).setToolTipText(null);
    } else {
      error(msg, false);
      info.setCursor(CURSORHAND);
      info.setText(ERRORINFO.matcher(msg).replaceAll(""), Msg.ERROR);
      final String tt = ERRORTT.matcher(msg).replaceAll("").
          replace("<", "&lt;").replace(">", "&gt;").
          replaceAll("\r?\n", "<br/>").replaceAll("(<br/>.*?)<br/>.*", "$1");
      info.setToolTipText("<html>" + tt + "</html>");
    }
  }

  /**
   * Jumps to the current error.
   */
  public void jumpToError() {
    if(errMsg != null) error(true);
  }

  /**
   * Handles info messages resulting from a query execution.
   * @param jump jump to error position
   * @param msg info message
   */
  public void error(final String msg, final boolean jump) {
    errMsg = msg;
    for(final String s : msg.split("\r?\n")) {
      if(XQERROR.matcher(s).matches()) {
        errMsg = s.replace(STOPPED_AT, "");
        break;
      }
    }
    error(jump);
  }

  /**
   * Handles info messages resulting from a query execution.
   * @param jump jump to error position
   */
  private void error(final boolean jump) {
    Matcher m = XQERROR.matcher(errMsg);
    final int el;
    int ec = 2;
    if(m.matches()) {
      errFile = new IOFile(m.group(1));
      el = toInt(m.group(2));
      ec = toInt(m.group(3));
    } else {
      m = XMLERROR.matcher(errMsg);
      if(!m.matches()) return;
      el = toInt(m.group(1));
      errFile = getEditor().file;
    }

    EditorArea edit = find(errFile, false);
    if(jump) {
      if(edit == null) edit = open(errFile, false, true);
      if(edit != null) tabs.setSelectedComponent(edit);
    }
    if(edit == null) return;

    // find approximate error position
    final int ll = edit.last.length;
    int ep = ll;
    for(int p = 0, l = 1, c = 1; p < ll; ++c, p += cl(edit.last, p)) {
      if(l > el || l == el && c == ec) {
        ep = p;
        break;
      }
      if(edit.last[p] == '\n') {
        ++l;
        c = 0;
      }
    }
    if(ep < ll && Character.isLetterOrDigit(cp(edit.last, ep))) {
      while(ep > 0 && Character.isLetterOrDigit(cp(edit.last, ep - 1))) ep--;
    }
    edit.error(ep);
    errPos = ep;

    if(jump) {
      edit.jumpError(errPos);
      posCode.invokeLater();
    }
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
   * @param force action
   */
  void refreshControls(final boolean force) {
    // update modification flag
    final EditorArea edit = getEditor();
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
    public void eval(final Object arg) {
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
   * Saves the specified editor contents.
   * @param file file to write
   * @return success flag
   */
  private boolean save(final IOFile file) {
    try {
      final EditorArea edit = getEditor();
      file.write(edit.getText());
      edit.file(file);
      project.repaint();
      return true;
    } catch(final Exception ex) {
      BaseXDialog.error(gui, Util.info(FILE_NOT_SAVED_X, file));
      return false;
    }
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

    final BaseXButton close = tabButton("e_close");
    close.setRolloverIcon(BaseXLayout.icon("e_close2"));
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
    final BaseXButton add = tabButton("e_new");
    add.setRolloverIcon(BaseXLayout.icon("e_new2"));
    add.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        addTab();
        refreshControls(true);
      }
    });
    tabs.add(new BaseXBack(), add, 0);
    tabs.setEnabledAt(0, false);
  }

  /**
   * Adds a new tab button.
   * @param icon button icon
   * @return button
   */
  private BaseXButton tabButton(final String icon) {
    final BaseXButton b = new BaseXButton(gui, icon, null);
    b.setMargin(new Insets(0, 0, 0, 0));
    b.setContentAreaFilled(false);
    b.setFocusable(false);
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
