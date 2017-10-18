package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.gui.listener.*;
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project file tree.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ProjectView extends BaseXPanel {
  /** Cached file paths. */
  final ProjectFiles files = new ProjectFiles();
  /** Root directory. */
  final ProjectDir root;
  /** Tree. */
  final ProjectTree tree;
  /** Filter list. */
  final ProjectList list;

  /** Filter field. */
  private final ProjectFilter filter;
  /** Root path. */
  private final BaseXTextField rootPath;
  /** History button. */
  private final AbstractButton history;
  /** Splitter. */
  private final BaseXSplit split;

  /** Last focused component. */
  private Component last;
  /** Indicates if the current project files have been parsed. */
  private boolean parsed;

  /** Remembers the last focused component. */
  final FocusGainedListener lastfocus = (FocusGainedListener) e -> last = e.getComponent();

  /**
   * Constructor.
   * @param ev editor view
   */
  public ProjectView(final EditorView ev) {
    super(ev.gui);
    setLayout(new BorderLayout());

    tree = new ProjectTree(this);
    IOFile dir = dir();
    if(dir == null) dir = new IOFile(Prop.HOME).normalize();
    root = new ProjectDir(dir, this);
    tree.init(root);

    filter = new ProjectFilter(this);
    list = new ProjectList(this);
    BaseXLayout.addInteraction(list, gui);

    final BaseXBack back = new BaseXBack(new BorderLayout(2, 4));
    back.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GUIConstants.gray),
        BaseXLayout.border(5, 3, 5, 4)));

    rootPath = new BaseXTextField(gui);
    rootPath.history(GUIOptions.PROJECTS);
    rootPath.setEnabled(false);

    final AbstractButton browse = BaseXButton.get("c_editopen", OPEN, false, gui);
    browse.setToolTipText(CHOOSE_DIR + DOTS);
    browse.addActionListener(e -> changeRoot());

    history = BaseXButton.get("c_hist", RECENTLY_OPENED, false, gui);
    history.setToolTipText(INPUT_HISTORY);
    history.addActionListener(e -> showHistory());

    final BaseXBack buttons = new BaseXBack(new TableLayout(1, 2, 1, 0));
    buttons.add(browse);
    buttons.add(history);

    back.add(rootPath, BorderLayout.CENTER);
    back.add(buttons, BorderLayout.EAST);
    back.add(filter, BorderLayout.SOUTH);

    // add scroll bars
    final JScrollPane lscroll = new JScrollPane(list);
    lscroll.setBorder(BaseXLayout.border(0, 0, 0, 0));
    final JScrollPane tscroll = new JScrollPane(tree);
    tscroll.setBorder(BaseXLayout.border(0, 0, 0, 0));

    split = new BaseXSplit(false);
    split.setOpaque(false);
    split.add(lscroll);
    split.add(tscroll);
    split.init(new double[] { 0.3, 0.7}, new double[] { 0, 1});
    split.visible(false);
    showList(false);

    add(back, BorderLayout.NORTH);
    add(split, BorderLayout.CENTER);

    last = tree;
    tree.addFocusListener(lastfocus);
    list.addFocusListener(lastfocus);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent e) {
        // trigger parsing of project files
        refresh(false, false);
      }
    });

    rootPath(dir.path());
  }

  /**
   * Makes the list (in)visible.
   * @param vis visibility flag
   */
  void showList(final boolean vis) {
    split.visible(vis);
  }

  /**
   * Refreshes the view after a file has been saved.
   * @param file file to be opened
   * @param rename file has been renamed
   */
  public void save(final IOFile file, final boolean rename) {
    SwingUtilities.invokeLater(() -> {
      final IOFile path = file.normalize();
      if(path.path().startsWith(root.file.path())) refreshTree(path);
      refresh(rename, file.hasSuffix(IO.XQSUFFIXES));
    });
  }

  /**
   * Returns the list of erroneous files.
   * @return files
   */
  public TreeMap<String, InputInfo> errors() {
    return files.errors();
  }

  /**
   * Finds nodes to be highlighted and refreshes the tree.
   * @param node node to start from
   */
  void refreshHighlight(final ProjectNode node) {
    // loop through all tree nodes
    final Set<String> errPaths = errors().keySet();
    final Enumeration<?> en = node.depthFirstEnumeration();
    while(en.hasMoreElements()) {
      final ProjectNode child = (ProjectNode) en.nextElement();
      final IOFile file = child.file;
      if(file == null) continue;

      final String path = file.path(), dirPath = path + '/';
      boolean found = false;

      // loop through all erroneous paths
      for(final String errPath : errPaths) {
        // check if error path equals node path, or if it is a descendant
        if(errPath.equals(path) || errPath.startsWith(dirPath)) {
          found = true;
          break;
        }
      }
      child.error = found;
    }
    repaint();
  }

  /**
   * Refreshes the project view.
   */
  void refresh() {
    refresh(true, true);
  }

  /**
   * Refreshes the project view.
   * @param reset invalidate the file cache
   * @param parse parse
   */
  private void refresh(final boolean reset, final boolean parse) {
    if(reset) files.reset();
    filter.refresh(true);

    final boolean parseproj = gui.gopts.get(GUIOptions.PARSEPROJ);
    if(parseproj && !root.file.eq(new IOFile(Prop.USERHOME)) && (!parsed || parse)) {
      parsed = false;
      // do not parse if project view is hidden
      if(getWidth() == 0) return;

      new GUIWorker<Boolean>() {
        @Override
        protected Boolean doInBackground() throws InterruptedException {
          final Performance perf = new Performance();
          files.parse(root.file, gui.context);
          parsed = true;
          gui.status.setText(PARSING_CC + perf.getTime());
          return true;
        }
        @Override
        protected void done(final Boolean refresh) {
          if(refresh) refreshHighlight(root);
        }
      }.execute();
    }
  }

  /**
   * Jumps to the specified file.
   * @param file file to be focused
   * @param focus focus tree
   */
  public void jumpTo(final IOFile file, final boolean focus) {
    final IOFile fl = file.normalize();
    if(fl.path().startsWith(root.file.path())) tree.expand(root, fl.path());
    if(focus) tree.requestFocusInWindow();
  }

  /**
   * Refreshes the rendering of the specified file, or its parent, in the tree.
   * It may possibly be hidden in the current tree.
   * @param file file to be refreshed
   */
  private void refreshTree(final IOFile file) {
    final ProjectNode node = find(file.path());
    if(node != null) {
      node.refresh();
    } else {
      final IOFile parent = file.parent();
      if(parent != null) refreshTree(parent);
    }
  }

  /**
   * Returns the node for the specified file.
   * @param path path of file to be found
   * @return node or {@code null}
   */
  private ProjectNode find(final String path) {
    final Enumeration<?> en = root.depthFirstEnumeration();
    while(en.hasMoreElements()) {
      final ProjectNode node = (ProjectNode) en.nextElement();
      if(node.file != null && node.file.path().equals(path)) return node;
    }
    return null;
  }

  /**
   * Called when GUI design has changed.
   */
  public void refreshLayout() {
    filter.refreshLayout();
  }

  /**
   * Finds files with the text selected in the specified editor area.
   * @param ea calling editor
   */
  public void findFiles(final EditorArea ea) {
    filter.find(ea);
  }

  /**
   * Focuses the project view.
   */
  public void focus() {
    last.requestFocusInWindow();
  }

  /**
   * Renames a file or directory in the tree.
   * @param node source node
   * @param name new name of file or directory
   * @return new file reference or {@code null} if operation failed
   */
  IOFile rename(final ProjectNode node, final String name) {
    // check if chosen file name is valid
    if(IOFile.isValidName(name)) {
      final IOFile old = node.file;
      final IOFile updated = new IOFile(old.file().getParent(), name);
      // rename file or show error dialog
      if(old.rename(updated)) {
        // update tab references if file or directory could be renamed
        gui.editor.rename(old, updated);
        return updated;
      }
      BaseXDialog.error(gui, Util.info(FILE_NOT_RENAMED_X, old));
    }
    return null;
  }

  /**
   * Opens the selected file.
   * @param file file to be opened
   * @param search search string
   */
  void open(final IOFile file, final String search) {
    final EditorArea ea = gui.editor.open(file);
    if(ea != null) {
      SwingUtilities.invokeLater(() -> ea.jump(search));
    }
  }

  /**
   * Shows a dialog for changing the root directory.
   */
  private void changeRoot() {
    final ProjectNode child = tree.selectedNode();
    final IOFile file = (child != null ? child : root).file;
    final BaseXFileChooser fc = new BaseXFileChooser(gui, CHOOSE_DIR, file.path());
    final IOFile io = fc.select(Mode.DOPEN);
    if(io != null) changeRoot(io, true);
  }

  /**
   * Shows the project history.
   */
  private void showHistory() {
    final JPopupMenu popup = new JPopupMenu();
    for(final String project : new StringList(gui.gopts.get(GUIOptions.PROJECTS))) {
      final JMenuItem mi = new JMenuItem(project);
      mi.addActionListener(ac -> {
        changeRoot(new IOFile(ac.getActionCommand()), true);
      });
      popup.add(mi);
    }
    popup.show(history, 0, history.getHeight());
  }

  /**
   * Changes the root directory.
   * @param io root directory
   * @param force enforce directory and setting change
   */
  public void changeRoot(final IOFile io, final boolean force) {
    final IOFile normIO = io.normalize();
    if(force || dir() == null) {
      root.file = normIO;
      root.refresh();
      refresh();
      if(force) {
        gui.gopts.set(GUIOptions.PROJECTPATH, normIO.path());
        gui.gopts.write();
      }
    }
    rootPath(normIO.path());
  }

  /**
   * Refreshes the root path.
   * @param path root path
   */
  public void rootPath(final String path) {
    rootPath.setText(path);
    rootPath.store();
    history.setEnabled(rootPath.history().values().length != 0);
  }

  /**
   * Returns the project directory.
   * @return project directory, or {@code null}
   */
  public IOFile dir() {
    final String project = gui.gopts.get(GUIOptions.PROJECTPATH);
    return project.isEmpty() ? null : new IOFile(project).normalize();
  }
}
