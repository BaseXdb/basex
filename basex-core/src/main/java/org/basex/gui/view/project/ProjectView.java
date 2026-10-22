package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.gui.listener.*;
import org.basex.gui.text.*;
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Project file tree.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProjectView extends BaseXPanel {
  /** Cached file paths. */
  final ProjectFiles files;
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
  /** Indicates if the root path combo box is being reset. */
  private boolean resetting;

  /** Remembers the last focused component. */
  final FocusGainedListener lastfocus = e -> last = e.getComponent();

  /**
   * Constructor.
   * @param view editor view
   */
  public ProjectView(final EditorView view) {
    super(view.gui);
    setLayout(new BorderLayout());

    files = new ProjectFiles(this);

    final String project = gui.gopts.get(GUIOptions.PROJECTPATH);
    final IOFile dir = new IOFile(project.isEmpty() ? Prop.HOMEDIR : project).normalize();
    root = new ProjectDir(dir, this);

    tree = new ProjectTree(this);
    tree.init(root);

    filter = new ProjectFilter(this);
    list = new ProjectList(this);
    BaseXLayout.addInteraction(list, gui);

    final BaseXBack back = new BaseXBack(new BorderLayout(2, 4));
    back.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GUIConstants.gray),
        BaseXLayout.border(5, 3, 5, 4)));

    rootPath = new BaseXCombo(gui, false).history(GUIOptions.PROJECTS, gui.gopts);
    rootPath.addActionListener(e -> changeRoot());
    rootPath.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(final PopupMenuEvent e) { }
      @Override public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
        rootPath.updateHistory();
      }
      @Override public void popupMenuCanceled(final PopupMenuEvent e) { }
    });

    final BaseXToolBar buttons = new BaseXToolBar();

    final AbstractButton browse = BaseXButton.get("c_edit_open", OPEN, false, gui);
    browse.setToolTipText(CHOOSE_DIR + ELLIPSIS);
    browse.addActionListener(e -> chooseRoot());
    browse.setFocusable(true);
    buttons.add(browse);

    back.add(rootPath, BorderLayout.CENTER);
    back.add(buttons, BorderLayout.EAST);
    back.add(filter, BorderLayout.SOUTH);

    // add scroll bars
    final JScrollPane lscroll = new JScrollPane(list);
    lscroll.setBorder(BaseXLayout.border(0, 0, 0, 0));
    final JScrollPane tscroll = new JScrollPane(tree);
    tscroll.setBorder(BaseXLayout.border(0, 0, 0, 0));

    final BaseXBack listPanel = new BaseXBack(false).layout(new BorderLayout(0, 2));
    listPanel.add(lscroll, BorderLayout.CENTER);
    listPanel.add(filter.replaceRow(), BorderLayout.SOUTH);

    split = new BaseXSplit(false);
    split.setOpaque(false);
    split.add(listPanel);
    split.add(tscroll);
    final double lh = gui.gopts.get(GUIOptions.PROJECTLIST) / 100d;
    split.init(new double[] { lh, 1 - lh }, new double[] { 0, 1});
    split.resized(sizes -> gui.gopts.set(GUIOptions.PROJECTLIST, (int) Math.round(sizes[0] * 100)));
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
        // trigger parsing of project files; the filter must not be refreshed
        parse(false, false);
      }
    });

    rootPath.select(dir.path());
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
   * @param xquery file is XQuery module
   * @param enforce enforce parsing of XQuery files
   */
  public void save(final IOFile file, final boolean rename, final boolean xquery,
      final boolean enforce) {

    SwingUtilities.invokeLater(() -> {
      final IOFile io = file.normalize();
      if(root.file.contains(io)) {
        if(xquery) ProjectFiles.parse(file.path(), gui.context, files.errors());
        refreshTree(io);
      }
      refresh(rename, enforce, false);
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
    for(final TreeNode tn : Collections.list(node.depthFirstEnumeration())) {
      final ProjectNode child = (ProjectNode) tn;
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
  private static boolean find(final Set<String> errPaths, final IOFile file) {
    for(final String errPath : errPaths) {
      if(file.contains(new IOFile(errPath))) return true;
    }
    return false;
  }

  /**
   * Refreshes the project view.
   */
  void refresh() {
    refresh(true, true, true);
  }

  /**
   * Refreshes the project view.
   * @param reset   invalidate the file cache
   * @param enforce enforce parsing of XQuery files
   * @param report  report the parsing time in the status bar
   */
  private void refresh(final boolean reset, final boolean enforce, final boolean report) {
    if(reset) files.reset();
    filter.refresh(true);
    parse(enforce, report);
  }

  /**
   * Parses the project files in the background.
   * @param enforce enforce parsing of XQuery files
   * @param report  report the parsing time in the status bar
   */
  private void parse(final boolean enforce, final boolean report) {
    // do not parse if project view is not visible, or if it has already been parsed
    if(!gui.gopts.get(GUIOptions.PARSEPROJ) || getWidth() == 0 || parsed && !enforce) return;

    parsed = false;
    new GUIWorker<Boolean>() {
      @Override
      protected Boolean doInBackground() throws InterruptedException {
        final Performance perf = new Performance();
        files.parse(root.file, gui.context);
        parsed = true;
        // background parsing must not replace the status of the last query
        if(report) gui.status.setText(PARSING_CC + perf.formatRuntime(), true);
        return true;
      }

      @Override
      protected void done(final Boolean refresh) {
        if(refresh) refreshHighlight(root);
      }
    }.execute();
  }

  /**
   * Jumps to the specified file.
   * @param file file to be focused (can be {@code null})
   * @param focus focus tree
   */
  public void jumpTo(final IOFile file, final boolean focus) {
    // ignore call if project view is not visible
    if(getWidth() == 0) return;

    if(file != null) {
      // keep paths below links to the root unresolved
      final IOFile fl = root.file.contains(file) ? file : file.normalize();
      if(root.file.contains(fl)) tree.expand(root, fl.path());
    }
    if(focus) tree.requestFocusInWindow();
  }

  /**
   * Refreshes the rendering of the specified file, and its parents, in the tree.
   * It may, possibly, be hidden in the current tree.
   * @param file file to be refreshed
   */
  private void refreshTree(final IOFile file) {
    final ProjectNode node = find(file.path());
    if(node != null) node.refresh();

    final IOFile parent = file.parent();
    if(parent != null) refreshTree(parent);
  }

  /**
   * Returns the node for the specified file.
   * @param path path of file to be found
   * @return node or {@code null}
   */
  private ProjectNode find(final String path) {
    for(final TreeNode tn : Collections.list(root.depthFirstEnumeration())) {
      final ProjectNode node = (ProjectNode) tn;
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
    final IOFile old = node.file;
    if(name.equals(old.name())) return null;
    // check if chosen file name is valid
    if(!IOFile.isValidName(name)) {
      BaseXDialog.error(gui, Util.info(NAME_INVALID_X, name));
      return null;
    }
    // directory paths end with a slash
    final IOFile updated = new IOFile(old.file().getParent(),
        node instanceof ProjectDir ? name + '/' : name);
    // rename file or show error dialog
    try {
      old.moveTo(updated, false);
      // update tab references if file or directory could be renamed
      gui.editor.rename(old, updated);
      // invalidate file cache, filter list and errors
      refresh();
      return updated;
    } catch(final IOException ex) {
      Util.debug(ex);
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
    if(ea != null && !search.isEmpty()) {
      final SearchFlags flags = filter.listFlags();
      SwingUtilities.invokeLater(() -> ea.jump(search, flags));
    }
  }

  /**
   * Shows a dialog for changing the root directory.
   */
  private void chooseRoot() {
    final BaseXFileChooser fc = new BaseXFileChooser(gui, CHOOSE_DIR, root.file.path());
    final IOFile io = fc.select(Mode.DOPEN);
    if(io != null) {
      rootPath.select(io.normalize().path());
      changeRoot();
    }
  }

  /**
   * Changes the root directory. Called when another directory is chosen in the root path combo box.
   */
  private void changeRoot() {
    final String text = rootPath.getText();
    final IOFile path = new IOFile(text);
    if(resetting || root.file.eq(path)) return;

    if(!path.isDir()) {
      // directory does not exist anymore: report it, drop it from the history, restore the root
      BaseXDialog.error(gui, Util.info(FILE_NOT_OPENED_X, path));
      SwingUtilities.invokeLater(() -> {
        resetting = true;
        rootPath.removeHistory(text);
        rootPath.select(root.file.path());
        resetting = false;
      });
      return;
    }

    // the pending replacement backup mirrors the old root; invalidate it
    filter.discardBackups();

    gui.gopts.setFile(GUIOptions.PROJECTPATH, path);
    gui.saveOptions();

    root.file = path;
    root.expand();
    tree.expandPath(root.path());
    refresh();
  }
}
