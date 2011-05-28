package org.basex.gui.view.editor;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.basex.data.Nodes;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIMenu;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.io.IO;
import org.basex.util.BoolList;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * This view allows the input and evaluation of queries and documents.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class EditorView extends View {
  /** Error string. */
  private static final String ERRSTRING = STOPPED + ' ' + (LINEINFO +
      ", " + COLINFO).replaceAll("%", "([0-9]+)");
  /** Error file pattern. */
  private static final Pattern FILEPATTERN =
    Pattern.compile(ERRSTRING + ' ' + FILEINFO.replaceAll("%", "(.*)") + COL);

  /** Execute Button. */
  final BaseXButton stop;
  /** Info label. */
  final BaseXLabel info;
  /** Position label. */
  final BaseXLabel pos;
  /** Query area. */
  final BaseXTabs tabs;
  /** Search field. */
  final BaseXTextField find;
  /** Thread counter. */
  int threadID;

  /** Current error file. */
  String errFile;
  /** Current error position. */
  int errPos;

  /** Header string. */
  private final BaseXLabel header;
  /** Scroll Pane. */
  private final BaseXBack south;
  /** Execute button. */
  private final BaseXButton go;
  /** Filter button. */
  private final BaseXButton filter;

  /**
   * Default constructor.
   * @param man view manager
   */
  public EditorView(final ViewNotifier man) {
    super(EDITORVIEW, HELPXQUERYY, man);

    border(6, 6, 6, 6).layout(new BorderLayout()).setFocusable(false);

    header = new BaseXLabel(EDITORTIT, true, false);

    final BaseXButton openB = BaseXButton.command(GUICommands.EDITOPEN, gui);
    final BaseXButton saveB = new BaseXButton(gui, "editsave",
        token(GUISAVETT));
    final BaseXButton hist = new BaseXButton(gui, "hist", HELPRECENT);

    find = new BaseXTextField(gui);
    find.setSearch(null);
    BaseXLayout.setHeight(find, (int) openB.getPreferredSize().getHeight());

    BaseXBack sp = new BaseXBack(Fill.NONE).layout(new TableLayout(1, 7));
    sp.add(find);
    sp.add(Box.createHorizontalStrut(5));
    sp.add(openB);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(saveB);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(hist);

    final BaseXBack b = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    b.add(header, BorderLayout.CENTER);
    b.add(sp, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    tabs = new BaseXTabs(gui);
    tabs.setFocusable(false);

    addCreateTab();
    addTab();
    add(tabs, BorderLayout.CENTER);

    south = new BaseXBack(Fill.NONE).layout(new BorderLayout(8, 0));
    info = new BaseXLabel(" ");
    info.setText(OK, Msg.SUCCESS);
    pos = new BaseXLabel(" ");

    sp = new BaseXBack(Fill.NONE).layout(new BorderLayout(8, 0));
    sp.add(info, BorderLayout.CENTER);
    sp.add(pos, BorderLayout.EAST);

    south.add(sp, BorderLayout.CENTER);

    stop = new BaseXButton(gui, "stop", HELPSTOP);
    stop.addKeyListener(this);
    stop.setEnabled(false);

    go = new BaseXButton(gui, "go", HELPGO);
    go.addKeyListener(this);

    filter = BaseXButton.command(GUICommands.FILTER, gui);
    filter.addKeyListener(this);

    sp = new BaseXBack(Fill.NONE).border(4, 0, 0, 0).layout(
        new TableLayout(1, 5));
    sp.add(stop);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(go);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(filter);
    south.add(sp, BorderLayout.EAST);
    add(south, BorderLayout.SOUTH);
    refreshLayout();

    // add listeners
    saveB.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu pop = new JPopupMenu();
        final StringBuilder mnem = new StringBuilder();
        final JMenuItem sa =
          GUIMenu.newItem(GUICommands.EDITSAVE, gui, mnem);
        final JMenuItem sas =
          GUIMenu.newItem(GUICommands.EDITSAVEAS, gui, mnem);
        GUICommands.EDITSAVE.refresh(gui, sa);
        GUICommands.EDITSAVEAS.refresh(gui, sas);
        pop.add(sa);
        pop.add(sas);
        pop.show(saveB, 0, saveB.getHeight());
      }
    });
    hist.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JPopupMenu popup = new JPopupMenu();
        final ActionListener al = new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent ac) {
            open(IO.get(ac.getActionCommand()));
          }
        };
        if(gui.gprop.strings(GUIProp.QUERIES).length == 0) {
          popup.add(new JMenuItem("- No recently opened files -"));
        }
        for(final String en : gui.gprop.strings(GUIProp.QUERIES)) {
          final JMenuItem jmi = new JMenuItem(en);
          jmi.addActionListener(al);
          popup.add(jmi);
        }
        popup.show(hist, 0, hist.getHeight());
      }
    });
    info.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        EditorArea edit = getEditor();
        if(errFile != null) {
          edit = find(IO.get(errFile), false);
          if(edit == null) edit = open(IO.get(errFile));
          tabs.setSelectedComponent(edit);
          edit.error = errPos;
        }
        if(edit.error == -1) return;
        edit.setCaret(edit.error);
        edit.requestFocusInWindow();
        edit.markError();
        pos.setText(edit.pos());
      }
    });
    stop.addActionListener(new ActionListener() {
      @Override
     public void actionPerformed(final ActionEvent e) {
        stop.setEnabled(false);
        info.setText(OK, Msg.SUCCESS);
        gui.stop();
      }
    });
    go.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        getEditor().query();
      }
    });
    tabs.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        final EditorArea edit = getEditor();
        if(edit == null) return;
        edit.setSearch(find);
        gui.refreshControls();
        refreshMark();
        if(gui.gprop.is(GUIProp.EXECRT)) edit.query();
      }
    });
  }

  @Override
  public void refreshInit() { }

  @Override
  public void refreshFocus() { }

  @Override
  public void refreshMark() {
    go.setEnabled(getEditor().exec && !gui.gprop.is(GUIProp.EXECRT));
    final Nodes marked = gui.context.marked;
    filter.setEnabled(!gui.gprop.is(GUIProp.FILTERRT) &&
        marked != null && marked.size() != 0);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) { }

  @Override
  public void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    for(final EditorArea edit : editors()) edit.setFont(GUIConstants.mfont);
    refreshMark();
  }

  @Override
  public void refreshUpdate() { }

  @Override
  public boolean visible() {
    return gui.gprop.is(GUIProp.SHOWXQUERY);
  }

  @Override
  public void visible(final boolean v) {
    gui.gprop.set(GUIProp.SHOWXQUERY, v);
  }

  @Override
  protected boolean db() {
    return false;
  }

  /**
   * Opens a new file.
   */
  public void open() {
    // open file chooser for XML creation
    final BaseXFileChooser fc = new BaseXFileChooser(GUIOPEN,
        gui.gprop.get(GUIProp.XQPATH), gui);
    fc.addFilter(CREATEXQEXDESC, IO.XQSUFFIXES);
    final IO file = fc.select(BaseXFileChooser.Mode.FOPEN);
    if(file != null) {
      open(file);
      getEditor().opened = true;
    }
  }

  /**
   * Saves the contents of the currently opened editor.
   * @return {@code false} if operation was canceled
   */
  public boolean save() {
    final EditorArea edit = getEditor();
    if(!edit.opened) return saveAs();
    save(edit);
    return true;
  }

  /**
   * Saves the contents of the currently opened editor under a new name.
   * @return {@code false} if operation was canceled
   */
  public boolean saveAs() {
    // open file chooser for XML creation
    final EditorArea edit = getEditor();
    final BaseXFileChooser fc =
      new BaseXFileChooser(GUISAVEAS, edit.file.path(), gui);
    fc.addFilter(CREATEXQEXDESC, IO.XQSUFFIXES);

    final IO file = fc.select(BaseXFileChooser.Mode.FSAVE);
    if(file == null) return false;
    edit.file = file;
    edit.setSyntax(file);
    save(edit);
    return true;
  }

  /**
   * Creates a new file.
   */
  public void newFile() {
    addTab();
    refresh(false, true);
  }

  /**
   * Opens the specified query file.
   * @param file query file
   * @return opened editor
   */
  public EditorArea open(final IO file) {
    if(!visible()) GUICommands.SHOWXQUERY.execute(gui);

    EditorArea edit = find(file, true);
    if(edit != null) {
      // switch to open file
      tabs.setSelectedComponent(edit);
      return edit;
    }
    // get current editor
    edit = getEditor();
    // create new tab if current text is stored on disk, or has been modified
    if(edit.opened || edit.mod) edit = addTab();

    try {
      edit.setText(file.content());
      edit.opened = true;
      edit.file = file;
      gui.gprop.recent(file);
      refresh(false, true);
      if(gui.gprop.is(GUIProp.EXECRT)) edit.query();
      return edit;
    } catch(final IOException ex) {
      Dialog.error(gui, NOTOPENED);
      return edit;
    }
  }

  /**
   * Closes an editor.
   * @param edit editor to be closed. {@code null} closes the currently
   * opened editor.
   * opened editor is to be closed
   */
  public void close(final EditorArea edit) {
    final EditorArea ea = edit != null ? edit : getEditor();
    if(!confirm(ea)) return;

    tabs.remove(ea);
    final int t = tabs.getTabCount();
    final int i = tabs.getSelectedIndex();
    if(t == 1) {
      // reopen single tab
      addTab();
    } else if(i + 1 == t) {
      // if necessary, activate last editor tab
      tabs.setSelectedIndex(i - 1);
    }
  }

  /**
   * Initializes the info message.
   */
  public void reset() {
    ++threadID;
    errFile = null;
    info.setToolTipText(null);
    info.setText(OK, Msg.SUCCESS);
    stop.setEnabled(false);
  }

  /**
   * Starts a waiting thread, which shows a waiting info after a short timeout.
   */
  void startWait() {
    final int thread = ++threadID;
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(200);
        if(thread == threadID) info.setText(INFOWAIT, Msg.SUCCESS);
      }
    }.start();
  }

  /**
   * Evaluates the info message resulting from a query execution.
   * @param inf info message
   * @param ok true if query was successful
   */
  public void info(final String inf, final boolean ok) {
    ++threadID;
    info.setCursor(error(inf, ok) ?
        GUIConstants.CURSORHAND : GUIConstants.CURSORARROW);
    info.setText(ok ? OK : inf.replaceAll(STOPPED + ".*\\r?\\n\\[.*?\\] ", ""),
        ok ? Msg.SUCCESS : Msg.ERROR);
    info.setToolTipText(ok ? null : inf);
    stop.setEnabled(false);
  }

  /**
   * Shows a quit dialog for all modified query files.
   * @return {@code false} if confirmation was canceled
   */
  public boolean confirm() {
    for(final EditorArea edit : editors()) {
      if(!confirm(edit)) return false;
    }
    return true;
  }

  /**
   * Checks if the current text can be saved. The check returns {@code true}
   * if the text has not been opened from disk, or if it has been modified.
   * @return result of check
   */
  public boolean saveable() {
    final EditorArea area = getEditor();
    return !area.opened || area.mod;
  }

  /**
   * Returns the current editor.
   * @return editor
   */
  EditorArea getEditor() {
    final Component c = tabs.getSelectedComponent();
    return c instanceof EditorArea ? (EditorArea) c : null;
  }

  /**
   * Refreshes the query modification flag.
   * @param mod modification flag
   * @param force action
   */
  void refresh(final boolean mod, final boolean force) {
    final EditorArea edit = getEditor();
    refreshMark();
    if(edit.mod == mod && !force) return;

    String title = edit.file.name();
    if(mod) title += "*";
    edit.label.setText(title);
    edit.mod = mod;
    gui.refreshControls();
  }

  /**
   * Finds the editor that contains the specified file.
   * @param file file to be found
   * @param opened considers only opened files
   * @return editor
   */
  EditorArea find(final IO file, final boolean opened) {
    for(final EditorArea edit : editors()) {
      if(edit.file.eq(file) && (!opened || edit.opened)) return edit;
    }
    return null;
  }

  /**
   * Saves the specified editor contents.
   * @param edit editor area
   */
  private void save(final EditorArea edit) {
    try {
      edit.file.write(getEditor().getText());
      edit.opened = true;
      gui.gprop.recent(edit.file);
      refresh(false, true);
    } catch(final IOException ex) {
      Dialog.error(gui, NOTSAVED);
    }
  }

  /**
   * Choose a unique tab file.
   * @return io reference
   */
  private IO newTabFile() {
    // collect numbers of existing files
    final BoolList bl = new BoolList();
    for(final EditorArea edit : editors()) {
      if(edit.opened) continue;
      final String n = edit.file.name().substring(EDITORFILE.length());
      bl.set(true, n.isEmpty() ? 1 : Integer.parseInt(n));
    }
    // find first free file number
    int c = 0;
    while(++c < bl.size() && bl.get(c));
    // create io reference
    final String dir = gui.gprop.get(GUIProp.XQPATH);
    return IO.get(dir + EDITORFILE + (c == 1 ? "" : c));
  }

  /**
   * Adds a new editor tab.
   * @return editor reference
   */
  EditorArea addTab() {
    final EditorArea edit = new EditorArea(this, newTabFile());
    edit.setFont(GUIConstants.mfont);

    final BaseXBack tab = new BaseXBack(
        new BorderLayout(10, 0)).mode(Fill.NONE);
    tab.add(edit.label, BorderLayout.CENTER);

    final BaseXButton close = tabButton("editclose");
    close.setRolloverIcon(BaseXLayout.icon("cmd-editclose2"));
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
    final BaseXButton add = tabButton("editnew");
    add.setRolloverIcon(BaseXLayout.icon("cmd-editnew2"));
    add.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        addTab();
        refresh(false, true);
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
    b.border(2, 2, 2, 2).setContentAreaFilled(false);
    b.setFocusable(false);
    return b;
  }

  /**
   * Shows a quit dialog the specified editor.
   * @param edit editor to be saved
   * @return {@code false} if confirmation was canceled
   */
  private boolean confirm(final EditorArea edit) {
    if(edit.mod) {
      final Boolean ok = Dialog.yesNoCancel(gui,
          Util.info(XQUERYCONF, edit.file.name()));
      if(ok == null || ok && !save()) return false;
    }
    return true;
  }

  /**
   * Returns all editors.
   * @return editors
   */
  public EditorArea[] editors() {
    final ArrayList<EditorArea> edits = new ArrayList<EditorArea>();
    for(final Component c : tabs.getComponents()) {
      if(c instanceof EditorArea) edits.add((EditorArea) c);
    }
    return edits.toArray(new EditorArea[edits.size()]);
  }

  /**
   * Handles info messages resulting from a query execution.
   * @param message info message
   * @param ok true if query was successful
   * @return true if error was found
   */
  boolean error(final String message, final boolean ok) {
    errPos = -1;
    EditorArea edit = getEditor();
    final String msg = message.replaceAll("[\\r\\n].*", "");
    if(!ok) {
      final Matcher m = FILEPATTERN.matcher(msg);
      if(!m.matches()) return true;

      errFile = m.group(3);
      edit = find(IO.get(errFile), false);
      if(edit == null) return true;

      final int el = Integer.parseInt(m.group(1));
      final int ec = Integer.parseInt(m.group(2));
      errPos = edit.last.length;
      // find approximate error position
      final int ll = errPos;
      for(int e = 0, l = 1, c = 1; e < ll; ++c, e += cl(edit.last, e)) {
        if(l > el || l == el && c == ec) {
          errPos = e;
          break;
        }
        if(edit.last[e] == '\n') {
          ++l;
          c = 0;
        }
      }
    }
    edit.error = errPos;
    edit.markError();
    return errPos != -1;
  }
}
