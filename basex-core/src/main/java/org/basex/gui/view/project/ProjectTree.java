package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Tree of project view.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ProjectTree extends BaseXTree implements TreeWillExpandListener {
  /** Project view. */
  private final ProjectView view;

  /**
   * Constructor.
   * @param view project view
   */
  ProjectTree(final ProjectView view) {
    super(view.gui, new DefaultMutableTreeNode());
    this.view = view;

    border(4, 4, 4, 4);
    setExpandsSelectedPaths(true);
    final ProjectCellRenderer renderer = new ProjectCellRenderer();
    // choose common parent directories of project directories
    setCellRenderer(renderer);
    addTreeWillExpandListener(this);
    addMouseListener((MouseClickedListener) e -> {
      if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
        new OpenCmd().execute(view.gui);
    });

    setCellEditor(new ProjectCellEditor(this, renderer));
    setEditable(true);
    final int h = getFontMetrics(getFont()).getHeight();
    if(h > 16) setRowHeight(Math.max(32, h));

    // add popup
    new BaseXPopup(this, view.gui,
      new OpenCmd(), new OpenExternalCmd(), new TestCmd(), new SetContextCmd(), null,
      new DeleteCmd(), new RenameCmd(), new NewDirCmd(), null,
      new RefreshCmd(), null, new CopyPathCmd()
    );
  }

  /**
   * Initializes the tree view.
   * @param node root node
   */
  void init(final ProjectNode node) {
    ((DefaultMutableTreeNode) getModel().getRoot()).add(node);

    // expand root and child directories
    for(int r = 0; r < 2; r++) expandRow(r);
    setRootVisible(false);
    setSelectionRow(0);
  }

  /**
   * Expands a node and its sub node that matches the specified path.
   * @param node node
   * @param path file path
   * @return {@code true} if file was found
   */
  boolean expand(final ProjectNode node, final String path) {
    final TreePath tp = node.path();
    if(!isExpanded(tp)) expandPath(tp);

    final int ns = node.getChildCount();
    for(int n = 0; n < ns; n++) {
      final ProjectNode ch = (ProjectNode) node.getChildAt(n);
      final String np = ch.file.path();
      // path found
      if(path.equals(np)) {
        final TreePath cp = ch.path();
        setSelectionPath(cp);
        scrollPathToVisible(cp);
        return true;
      }
      // expand child path
      if(path.startsWith(np)) return expand(ch, path);
    }
    return false;
  }

  @Override
  public void treeWillExpand(final TreeExpansionEvent event) {
    final Object obj = event.getPath().getLastPathComponent();
    if(obj instanceof ProjectNode) {
      final ProjectNode node = (ProjectNode) obj;
      node.expand();
    }
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent event) {
    final Object obj = event.getPath().getLastPathComponent();
    if(obj instanceof ProjectNode) {
      final ProjectNode node = (ProjectNode) obj;
      node.collapse();
    }
  }

  // PRIVATE METHOS ===============================================================================

  /**
   * Returns a single selected node, or {@code null} if zero or more than node is selected.
   * @return selected node or {@code null}
   */
  ProjectNode selectedNode() {
    final TreePath tp = selectedPath();
    if(tp != null) {
      final Object node = tp.getLastPathComponent();
      if(node instanceof ProjectNode) return (ProjectNode) node;
    }
    return null;
  }

  /**
   * Returns all selected nodes.
   * @return selected nodes
   */
  private ArrayList<ProjectNode> selectedNodes() {
    final ArrayList<ProjectNode> nodes = new ArrayList<>();
    final TreePath[] paths = getSelectionPaths();
    if(paths != null) {
      for(final TreePath tp : paths) {
        final Object node = tp.getLastPathComponent();
        if(node instanceof ProjectNode) nodes.add((ProjectNode) node);
      }
    }
    return nodes;
  }

  /**
   * Returns the selected path, or returns {@code null} if zero or more than paths are selected.
   * @return path
   */
  private TreePath selectedPath() {
    final TreePath[] tps = getSelectionPaths();
    return tps == null || tps.length > 1 ? null : tps[0];
  }

  // COMMANDS =====================================================================================

  /** Refresh command. */
  private final class RefreshCmd extends GUIPopupCmd {
    /** Constructor. */
    RefreshCmd() { super(REFRESH, BaseXKeys.REFRESH); }

    @Override public void execute() {
      view.refresh();
      selectedNode().refresh();
    }

    @Override public boolean enabled(final GUI main) {
      return selectedNode() != null;
    }
  }

  /** New directory command. */
  private final class NewDirCmd extends GUIPopupCmd {
    /** Constructor. */
    NewDirCmd() { super(NEW_DIR, BaseXKeys.NEWDIR); }

    @Override public void execute() {
      final ProjectNode node = selectedNode();
      final ProjectNode dir = node instanceof ProjectDir ? node : (ProjectNode) node.getParent();

      // choose free name
      String name = '(' + NEW_DIR + ')';
      IOFile file = new IOFile(dir.file, name);
      int c = 1;
      while(file.exists()) {
        name = '(' + NEW_DIR + ' ' + ++c + ')';
        file = new IOFile(dir.file, name);
      }
      if(file.md()) {
        dir.expand();

        final String fn = name;
        new Thread(() -> {
          final Enumeration<?> children = dir.children();
          while(children.hasMoreElements()) {
            final ProjectNode child = (ProjectNode) children.nextElement();
            if(child.file != null && child.file.name().equals(fn)) {
              final TreePath tp = child.path();
              setSelectionPath(tp);
              startEditingAtPath(tp);
              break;
            }
          }
        }).start();
      }
    }
    @Override public boolean enabled(final GUI main) {
      return selectedNode() != null;
    }
  }

  /** Delete command. */
  private final class DeleteCmd extends GUIPopupCmd {
    /** Constructor. */
    DeleteCmd() { super(DELETE + DOTS, BaseXKeys.DELNEXT); }

    @Override public void execute() {
      final ProjectNode node = selectedNode();
      final GUI gui = view.gui;
      if(BaseXDialog.confirm(gui, Util.info(DELETE_FILE_X, node.file))) {
        final ProjectNode parent = (ProjectNode) node.getParent();
        // delete file or show error dialog
        if(gui.editor.delete(node.file)) {
          parent.refresh();
          setSelectionPath(parent.path());
          view.refresh();
        } else {
          BaseXDialog.error(gui, Util.info(FILE_NOT_DELETED_X, node.file));
        }
      }
    }

    @Override public boolean enabled(final GUI main) {
      final ProjectNode node = selectedNode();
      return node != null && !node.root();
    }
  }

  /** Rename command. */
  private final class RenameCmd extends GUIPopupCmd {
    /** Constructor. */
    RenameCmd() { super(RENAME, BaseXKeys.RENAME); }

    @Override public void execute() {
      startEditingAtPath(selectedNode().path());
      view.refresh();
    }

    @Override public boolean enabled(final GUI main) {
      final ProjectNode node = selectedNode();
      return node != null && !node.root();
    }
  }

  /** Open command. */
  private final class OpenCmd extends GUIPopupCmd {
    /** Constructor. */
    OpenCmd() { super(OPEN, BaseXKeys.ENTER); }

    @Override public void execute() {
      for(final ProjectNode node : selectedNodes()) view.open(node.file, "");
    }

    @Override public boolean enabled(final GUI main) {
      for(final ProjectNode node : selectedNodes()) {
        if(node.file.isDir()) return false;
      }
      return true;
    }
  }

  /** Open externally command. */
  private final class OpenExternalCmd extends GUIPopupCmd {
    /** Constructor. */
    OpenExternalCmd() { super(OPEN_EXTERNALLY, BaseXKeys.SHIFT_ENTER); }

    @Override public void execute() {
      for(final ProjectNode node : selectedNodes()) {
        try {
          node.file.open();
        } catch(final IOException ex) {
          Util.debug(ex);
          BaseXDialog.error(view.gui, Util.info(FILE_NOT_OPENED_X, node.file));
        }
      }
    }

    @Override public boolean enabled(final GUI main) {
      return selectedNode() != null;
    }
  }

  /** Test command. */
  private final class TestCmd extends GUIPopupCmd {
    /** Constructor. */
    TestCmd() { super(RUN_TESTS, BaseXKeys.UNIT); }

    @Override public void execute() {
      for(final ProjectNode node : selectedNodes()) {
        view.gui.execute(new Test(node.file.path()));
      }
    }

    @Override public boolean enabled(final GUI main) {
      return selectedNode() != null;
    }
  }

  /** Copy path command. */
  private final class CopyPathCmd extends GUIPopupCmd {
    /** Constructor. */
    CopyPathCmd() { super(COPY_PATH, BaseXKeys.COPYPATH); }

    @Override public void execute() {
      BaseXLayout.copyPath(selectedNode().file.path());
    }

    @Override public boolean enabled(final GUI main) {
      return selectedNode() != null;
    }
  }

  /** Set context command. */
  private final class SetContextCmd extends GUIPopupCmd {
    /** Constructor. */
    SetContextCmd() { super(SET_CONTEXT); }

    @Override public void execute() {
      view.gui.editor.setContext(selectedNode().file);
    }

    @Override public boolean enabled(final GUI main) {
      final ProjectNode pn = selectedNode();
      return pn != null && !pn.file.isDir() && pn.file.hasSuffix(view.gui.gopts.xmlSuffixes());
    }
  }
}
