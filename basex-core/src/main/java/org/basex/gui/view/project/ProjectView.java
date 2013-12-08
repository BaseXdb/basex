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

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project file tree.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class ProjectView extends BaseXPanel implements TreeWillExpandListener {
  /** Root directory. */
  final ProjectDir root;
  /** Root node. */
  final ProjectCellRenderer renderer;
  /** Tree. */
  final BaseXTree tree;
  /** Editor view. */
  final EditorView view;
  /** Filter field. */
  final ProjectFilter filter;
  /** Filter list. */
  final ProjectList list;
  /** Scroll pane. */
  final JScrollPane scroll;
  /** Root path. */
  final BaseXTextField path;

  /**
   * Constructor.
   * @param ev editor view
   */
  public ProjectView(final EditorView ev) {
    super(ev.gui);
    view = ev;
    setLayout(new BorderLayout());

    final DefaultMutableTreeNode invisible = new DefaultMutableTreeNode();
    tree = new BaseXTree(invisible, gui).border(4, 4, 4, 4);
    renderer = new ProjectCellRenderer();
    tree.setExpandsSelectedPaths(true);
    tree.setCellRenderer(renderer);
    tree.addTreeWillExpandListener(this);
    tree.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
          new OpenCmd().execute(gui);
      }
    });

    tree.setCellEditor(new ProjectCellEditor(tree, renderer));
    tree.setEditable(true);

    // choose common parent directories of project directories
    root = new ProjectDir(new IOFile(root()), this);
    invisible.add(root);

    // expand root and child directories
    for(int r = 0; r < 2; r++) tree.expandRow(r);
    tree.setRootVisible(false);
    tree.setSelectionRow(0);

    // add popup
    new BaseXPopup(tree, gui, new OpenCmd(), new OpenExternalCmd(), null,
        new DeleteCmd(), new RenameCmd(), new NewDirCmd(), null,
        new RefreshCmd(), new CopyPathCmd());

    // add scroll bar
    scroll = new JScrollPane(tree);
    scroll.setBorder(new EmptyBorder(0, 0, 0, 0));

    filter = new ProjectFilter(this);
    list = new ProjectList(this);
    BaseXLayout.addInteraction(list, gui);

    final BaseXBack back = new BaseXBack().layout(new BorderLayout(2, 2));
    back.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, GUIConstants.GRAY),
        new EmptyBorder(3, 1, 3, 2)));

    path = new BaseXTextField(gui);
    path.setText(root.file.path());
    path.setEnabled(false);

    final BaseXButton browse = new BaseXButton(DOTS, gui);
    browse.setMargin(new Insets(0, 2, 0, 2));
    browse.setToolTipText(CHOOSE_DIR);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { change(); }
    });

    back.add(path, BorderLayout.CENTER);
    back.add(browse, BorderLayout.EAST);
    back.add(filter, BorderLayout.SOUTH);

    add(back, BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);
  }

  @Override
  public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
    final Object obj = event.getPath().getLastPathComponent();
    if(obj instanceof ProjectNode) {
      final ProjectNode node = (ProjectNode) obj;
      node.expand();
      node.updateTree();
    }
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
    final Object obj = event.getPath().getLastPathComponent();
    if(obj instanceof ProjectNode) {
      final ProjectNode node = (ProjectNode) obj;
      node.collapse();
      node.updateTree();
    }
  }

  /**
   * Refreshes the specified file node.
   * @param file file to be opened
   * @param tr refresh tree or filter
   */
  public void refresh(final IOFile file, final boolean tr) {
    if(tr) {
      final Enumeration<?> en = root.depthFirstEnumeration();
      while(en.hasMoreElements()) {
        final ProjectNode node = (ProjectNode) en.nextElement();
        if(node.file != null && node.file.path().equals(file.path())) node.refresh();
      }
    }
    // check if file to be refreshed is within the root path
    if(canonical(file.file()).getPath().startsWith(canonical(root.file.file()).getPath())) {
      filter.refresh(true);
    }
  }

  /**
   * Focuses the project view.
   * @param filt focus filter or content
   */
  public void focus(final boolean filt) {
    if(filt) {
      filter.focus();
    } else {
      scroll.getViewport().getView().requestFocusInWindow();
    }
  }

  /**
   * Renames a file or directory in the tree.
   * @param node source node
   * @param name new name of file or directory
   * @return new file reference, or {@code null} if operation failed
   */
  IOFile rename(final ProjectNode node, final String name) {
    // check if chosen file name is valid
    if(IOFile.isValidName(name)) {
      final IOFile old = node.file;
      final IOFile updated = new IOFile(old.file().getParent(), name);
      // rename file or show error dialog
      if(!old.rename(updated)) {
        BaseXDialog.error(gui, Util.info(FILE_NOT_RENAMED_X, old));
      } else {
        // update tab references if file or directory could be renamed
        gui.editor.rename(old, updated);
        return updated;
      }
    }
    return null;
  }

  /**
   * Returns a common parent directory.
   * @return root directory
   */
  private String root() {
    final GlobalOptions gopts = gui.context.globalopts;
    final String proj = gui.gopts.get(GUIOptions.PROJECTPATH);
    if(!proj.isEmpty()) return proj;

    final File io1 = new File(gopts.get(GlobalOptions.REPOPATH));
    final File io2 = new File(gopts.get(GlobalOptions.WEBPATH));
    final File fl = new File(gopts.get(GlobalOptions.RESTXQPATH));
    final File io3 = fl.isAbsolute() ? fl : new File(io2, fl.getPath());
    final StringList sl = new StringList();
    for(final File f : new File[] { io1, io2, io3 }) {
      final String p = canonical(f).getParent();
      if(!sl.contains(p)) sl.add(p);
    }
    return sl.unique().get(0);
  }

  /**
   * Returns the canonical or (if not possible) the absolute file reference.
   * @param f file reference
   * @return file
   */
  private File canonical(final File f) {
    try {
      return f.getCanonicalFile();
    } catch(final IOException ex) {
      return f.getAbsoluteFile();
    }
  }

  /**
   * Opens the selected file.
   * @param file file to be opened
   * @param search search string
   */
  void open(final IOFile file, final String search) {
    if(!file.isDir() && view.open(file) != null) {
      final Editor editor = view.getEditor();
      editor.requestFocusInWindow();
      final SearchBar sb = editor.search;
      if(search != null) {
        sb.reset();
        sb.activate(search, true);
      } else {
        sb.deactivate(true);
      }
    }
  }

  /**
   * Returns the selected nodes.
   * @return selected node
   */
  private ArrayList<ProjectNode> selectedNodes() {
    final ArrayList<ProjectNode> nodes = new ArrayList<ProjectNode>();
    final TreePath[] paths = tree.getSelectionPaths();
    if(paths != null) {
      for(final TreePath tp : paths) {
        final Object node = tp.getLastPathComponent();
        if(node instanceof ProjectNode) nodes.add((ProjectNode) node);
      }
    }
    return nodes;
  }

  /**
   * Returns a single selected node, or {@code null} if zero or more than node is selected.
   * @return selected node
   */
  private ProjectNode selectedNode() {
    final TreePath tp = selectedPath();
    if(tp != null) {
      final Object node = tp.getLastPathComponent();
      if(node instanceof ProjectNode) return (ProjectNode) node;
    }
    return null;
  }

  /**
   * Returns the selected path, or returns {@code null} if zero or more than paths are selected.
   * @return path
   */
  private TreePath selectedPath() {
    final TreePath[] tps = tree.getSelectionPaths();
    return tps == null || tps.length > 1 ? null : tps[0];
  }

  /**
   * Changes the root directory.
   */
  private void change() {
    final ProjectNode child = selectedNode();
    final BaseXFileChooser fc = new BaseXFileChooser(CHOOSE_DIR, child.file.path(), gui);
    final IOFile io = fc.select(Mode.DOPEN);
    if(io != null) {
      root.file = io;
      root.refresh();
      filter.reset();
      path.setText(io.path());
      gui.gopts.set(GUIOptions.PROJECTPATH, io.path());
    }
  }

  // COMMANDS =====================================================================================

  /** Refresh command. */
  final class RefreshCmd extends GUIPopupCmd {
    /** Constructor. */
    RefreshCmd() { super(REFRESH, BaseXKeys.REFRESH); }

    @Override public void execute() {
      filter.reset();
      selectedNode().refresh();
    }

    @Override public boolean enabled(final GUI main) {
      return selectedNode() != null;
    }
  }

  /** New directory command. */
  final class NewDirCmd extends GUIPopupCmd {
    /** Constructor. */
    NewDirCmd() { super(NEW_DIR, BaseXKeys.NEWDIR); }

    @Override public void execute() {
      ProjectNode parent = selectedNode();
      if(parent instanceof ProjectFile) parent = (ProjectDir) parent.getParent();

      // choose free name
      String name = '(' + NEW_DIR + ')';
      IOFile file = new IOFile(parent.file, name);
      int c = 1;
      while(file.exists()) {
        name = '(' + NEW_DIR + ' ' + ++c + ')';
        file = new IOFile(parent.file, name);
      }
      if(file.md()) {
        tree.setSelectionPaths(null);
        parent.refresh();
        final int cl = parent.getChildCount();
        for(int i = 0; i < cl; i++) {
          final ProjectNode node = (ProjectNode) parent.getChildAt(i);
          if(node.file.name().equals(name)) {
            final TreePath tp = node.path();
            tree.setSelectionPath(tp);
            tree.startEditingAtPath(tp);
            break;
          }
        }
      }
    }
    @Override public boolean enabled(final GUI main) { return selectedNode() != null; }
  }

  /** Delete command. */
  final class DeleteCmd extends GUIPopupCmd {
    /** Constructor. */
    DeleteCmd() { super(DELETE + DOTS, BaseXKeys.DELETE); }

    @Override public void execute() {
      final ProjectNode node = selectedNode();
      if(BaseXDialog.confirm(gui, Util.info(DELETE_FILE_X, node.file))) {
        final ProjectNode parent = (ProjectNode) node.getParent();
        // delete file or show error dialog
        if(!view.delete(node.file)) {
          BaseXDialog.error(gui, Util.info(FILE_NOT_DELETED_X, node.file));
        } else {
          parent.refresh();
          tree.setSelectionPath(parent.path());
          filter.reset();
        }
      }
    }

    @Override public boolean enabled(final GUI main) {
      final ProjectNode node = selectedNode();
      return node != null && !node.root();
    }
  }

  /** Rename command. */
  final class RenameCmd extends GUIPopupCmd {
    /** Constructor. */
    RenameCmd() { super(RENAME, BaseXKeys.RENAME); }

    @Override public void execute() {
      tree.startEditingAtPath(selectedNode().path());
      filter.reset();
    }

    @Override public boolean enabled(final GUI main) {
      final ProjectNode node = selectedNode();
      return node != null && !node.root();
    }
  }

  /** Change directory command. */
  final class OpenCmd extends GUIPopupCmd {
    /** Constructor. */
    OpenCmd() { super(OPEN, BaseXKeys.ENTER); }

    @Override public void execute() {
      for(final ProjectNode node : selectedNodes()) open(node.file, null);
    }

    @Override public boolean enabled(final GUI main) {
      for(final ProjectNode node : selectedNodes()) {
        if(node.file.isDir()) return false;
      }
      return true;
    }
  }

  /** Change directory command. */
  final class OpenExternalCmd extends GUIPopupCmd {
    /** Constructor. */
    OpenExternalCmd() { super(OPEN_EXTERNALLY, BaseXKeys.OPEN); }

    @Override public void execute() {
      for(final ProjectNode node : selectedNodes()) {
        try {
          node.file.open();
        } catch(final IOException ex) {
          BaseXDialog.error(gui, Util.info(FILE_NOT_OPENED_X, node.file));
        }
      }
    }

    @Override public boolean enabled(final GUI main) {
      for(final ProjectNode node : selectedNodes()) {
        if(node.file.isDir()) return false;
      }
      return true;
    }
  }

  /** Copy path command. */
  final class CopyPathCmd extends GUIPopupCmd {
    /** Constructor. */
    CopyPathCmd() { super(COPY_PATH, BaseXKeys.COPY_PATH); }

    @Override public void execute() {
      BaseXLayout.copy(selectedNode().file.path());
    }

    @Override public boolean enabled(final GUI main) {
      return selectedNode() != null;
    }
  }
}
