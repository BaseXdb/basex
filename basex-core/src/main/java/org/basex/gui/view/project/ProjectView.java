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
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Project file tree.
 *
 * @author BaseX Team 2005-16, BSD License
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
  /** Splitter. */
  private final BaseXSplit split;

  /** Last focused component. */
  private Component last;
  /** Indicates if the current project files have been parsed. */
  private boolean parsed;

  /** Remembers the last focused component. */
  final FocusAdapter lastfocus = new FocusAdapter() {
    @Override
    public void focusGained(final FocusEvent ev) {
      last = ev.getComponent();
    }
  };

  /**
   * Constructor.
   * @param ev editor view
   */
  public ProjectView(final EditorView ev) {
    super(ev.gui);
    setLayout(new BorderLayout());

    tree = new ProjectTree(this);
    final String proj = gui.gopts.get(GUIOptions.PROJECTPATH);
    root = new ProjectDir(new IOFile(proj.isEmpty() ? Prop.HOME : proj), this);
    tree.init(root);

    filter = new ProjectFilter(this);
    list = new ProjectList(this);
    BaseXLayout.addInteraction(list, gui);

    final BaseXBack back = new BaseXBack().layout(new BorderLayout(2, 2));
    back.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GUIConstants.gray),
        BaseXLayout.border(3, 1, 3, 2)));

    rootPath = new BaseXTextField(gui);
    rootPath.setText(root.file.path());
    rootPath.setEnabled(false);

    final BaseXButton browse = new BaseXButton(DOTS, gui);
    browse.setMargin(new Insets(0, 2, 0, 2));
    browse.setToolTipText(CHOOSE_DIR + DOTS);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        changeRoot();
      }
    });

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
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final IOFile path = file.normalize();
        if(path.path().startsWith(root.file.path())) refreshTree(path);
        refresh(rename, file.hasSuffix(IO.XQSUFFIXES));
      }
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
   * Finds nodes to be highlighted as erroneous and refreshes the tree.
   */
  void refreshTree() {
    // loop through all tree nodes
    final Set<String> errPaths = files.errors().keySet();
    final Enumeration<?> en = root.depthFirstEnumeration();
    while(en.hasMoreElements()) {
      final ProjectNode node = (ProjectNode) en.nextElement();
      final IOFile file = node.file;
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
      node.error = found;
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
  void refresh(final boolean reset, final boolean parse) {
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
          if(refresh) refreshTree();
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
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          ea.jump(search);
        }
      });
    }
  }

  /**
   * Shows a dialog for changing the root directory.
   */
  private void changeRoot() {
    final ProjectNode child = tree.selectedNode();
    final IOFile file = (child != null ? child : root).file;
    final BaseXFileChooser fc = new BaseXFileChooser(CHOOSE_DIR, file.path(), gui);
    final IOFile io = fc.select(Mode.DOPEN);
    if(io != null) changeRoot(io, true);
  }

  /**
   * Changes the root directory.
   * @param io root directory
   * @param force enforce directory and setting change
   */
  public void changeRoot(final IOFile io, final boolean force) {
    final String project = gui.gopts.get(GUIOptions.PROJECTPATH);
    if(!force && !project.isEmpty()) return;
    root.file = io;
    root.refresh();
    refresh();
    rootPath.setText(io.path());
    if(force) {
      gui.gopts.set(GUIOptions.PROJECTPATH, io.path());
      gui.gopts.write();
    }
  }
}
