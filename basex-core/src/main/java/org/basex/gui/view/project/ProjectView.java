package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.gui.listener.*;
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Project file tree.
 *
 * @author BaseX Team 2005-18, BSD License
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
  private final BaseXCombo rootPath;
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
    if(dir == null) dir = new IOFile(Prop.HOMEDIR).normalize();
    root = new ProjectDir(dir, this);
    tree.init(root);

    filter = new ProjectFilter(this);
    list = new ProjectList(this);
    BaseXLayout.addInteraction(list, gui);

    final BaseXBack back = new BaseXBack(new BorderLayout(2, 4));
    back.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GUIConstants.gray),
        BaseXLayout.border(5, 3, 5, 4)));

    rootPath = new BaseXCombo(gui, true).history(GUIOptions.PROJECTS, gui.gopts);
    rootPath.setFocusable(false);
    SwingUtilities.invokeLater(() -> rootPath.textField().getDocument().addDocumentListener(
      new DocumentListener() {
        @Override public void insertUpdate(final DocumentEvent e) { changeRoot(true); }
        @Override public void removeUpdate(final DocumentEvent e) { changeRoot(true); }
        @Override public void changedUpdate(final DocumentEvent e) { }
      }
    ));

    final AbstractButton browse = BaseXButton.get("c_editopen", OPEN, false, gui);
    browse.setToolTipText(CHOOSE_DIR + DOTS);
    browse.addActionListener(e -> chooseRoot());

    back.add(rootPath, BorderLayout.CENTER);
    back.add(browse, BorderLayout.EAST);
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

    rootPath(dir, false);
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
      if(file != null) child.error = find(errPaths, file);
    }
    repaint();
  }

  /**
   * Checks if the specified file is an error path.
   * @param errPaths error paths
   * @param file file to be found
   * @return result of check
   */
  private boolean find(final Set<String> errPaths, final IOFile file) {
    final String path = file.path();
    for(final String errPath : errPaths) {
      if(errPath.startsWith(path)) return true;
    }
    return false;
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
   * @param enforce enforce parsing of XQuery files
   */
  private void refresh(final boolean reset, final boolean enforce) {
    if(reset) files.reset();
    filter.refresh(true);

    if(gui.gopts.get(GUIOptions.PARSEPROJ) && (!parsed || enforce)) {
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
   * @param file file to be focused (can be {@code null})
   * @param focus focus tree
   */
  public void jumpTo(final IOFile file, final boolean focus) {
    if(file != null) {
      final IOFile fl = file.normalize();
      if(fl.path().startsWith(root.file.path())) tree.expand(root, fl.path());
    }
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
    if(ea != null && !search.isEmpty()) SwingUtilities.invokeLater(() -> ea.jump(search));
  }

  /**
   * Shows a dialog for changing the root directory.
   */
  private void chooseRoot() {
    final ProjectNode child = tree.selectedNode();
    final IOFile file = (child != null ? child : root).file;
    final BaseXFileChooser fc = new BaseXFileChooser(gui, CHOOSE_DIR, file.path());
    final IOFile io = fc.select(Mode.DOPEN);
    if(io != null) rootPath(io.normalize(), true);
  }

  /**
   * Changes the root directory.
   * @param save save project path
   */
  public void changeRoot(final boolean save) {
    final IOFile path = new IOFile(rootPath.getText());
    if(root.file.eq(path)) return;

    if(save) {
      gui.gopts.set(GUIOptions.PROJECTPATH, path.path());
      gui.saveOptions();
    }
    root.file = path;
    root.refresh();
    refresh();
  }

  /**
   * Refreshes the root path.
   * @param path root path
   * @param history update history
   */
  public void rootPath(final IOFile path, final boolean history) {
    rootPath.setText(path.normalize().path());
    changeRoot(history);
    if(history) rootPath.updateHistory();
  }

  /**
   * Returns the project directory.
   * @return project directory, or {@code null} if no path has been assigned so far
   */
  public IOFile dir() {
    final String project = gui.gopts.get(GUIOptions.PROJECTPATH);
    return project.isEmpty() ? null : new IOFile(project).normalize();
  }
}
