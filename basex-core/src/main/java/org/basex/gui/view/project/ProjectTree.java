package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Tree of project view.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class ProjectTree extends BaseXTree implements TreeWillExpandListener {
  /** Project view. */
  final ProjectView view;

  /**
   * Constructor.
   * @param pv project view
   */
  public ProjectTree(final ProjectView pv) {
    super(new DefaultMutableTreeNode(), pv.gui);
    view = pv;

    border(4, 4, 4, 4);
    setExpandsSelectedPaths(true);
    final ProjectCellRenderer renderer = new ProjectCellRenderer();
    // choose common parent directories of project directories
    setCellRenderer(renderer);
    addTreeWillExpandListener(this);
    addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(final MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
          new OpenCmd().execute(pv.gui);
      }
    });

    setCellEditor(new ProjectCellEditor(this, renderer));
    setEditable(true);

    // add popup
    new BaseXPopup(this, pv.gui, new OpenCmd(), new OpenExternalCmd(), null,
        new DeleteCmd(), new RenameCmd(), new NewDirCmd(), null,
        new RefreshCmd(), new CopyPathCmd());
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
  public boolean expand(final ProjectNode node, final String path) {
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

  // PRIVATE METHOS ===============================================================================

  /**
   * Returns a single selected node, or {@code null} if zero or more than node is selected.
   * @return selected node
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
   * Returns the selected nodes.
   * @return selected node
   */
  private ArrayList<ProjectNode> selectedNodes() {
    final ArrayList<ProjectNode> nodes = new ArrayList<ProjectNode>();
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
  final class RefreshCmd extends GUIPopupCmd {
    /** Constructor. */
    RefreshCmd() { super(REFRESH, BaseXKeys.REFRESH); }

    @Override public void execute() {
      view.filter.reset();
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
        setSelectionPaths(null);
        parent.refresh();
        final int cl = parent.getChildCount();
        for(int i = 0; i < cl; i++) {
          final ProjectNode node = (ProjectNode) parent.getChildAt(i);
          if(node.file.name().equals(name)) {
            final TreePath tp = node.path();
            setSelectionPath(tp);
            startEditingAtPath(tp);
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
    DeleteCmd() { super(DELETE + DOTS, BaseXKeys.DELNEXT); }

    @Override public void execute() {
      final ProjectNode node = selectedNode();
      if(BaseXDialog.confirm(view.gui, Util.info(DELETE_FILE_X, node.file))) {
        final ProjectNode parent = (ProjectNode) node.getParent();
        // delete file or show error dialog
        if(!view.editor.delete(node.file)) {
          BaseXDialog.error(view.gui, Util.info(FILE_NOT_DELETED_X, node.file));
        } else {
          parent.refresh();
          setSelectionPath(parent.path());
          view.filter.reset();
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
      startEditingAtPath(selectedNode().path());
      view.filter.reset();
    }

    @Override public boolean enabled(final GUI main) {
      final ProjectNode node = selectedNode();
      return node != null && !node.root();
    }
  }

  /** Open command. */
  final class OpenCmd extends GUIPopupCmd {
    /** Constructor. */
    OpenCmd() { super(OPEN, BaseXKeys.ENTER); }

    @Override public void execute() {
      for(final ProjectNode node : selectedNodes()) view.open(node.file, null);
    }

    @Override public boolean enabled(final GUI main) {
      for(final ProjectNode node : selectedNodes()) {
        if(node.file.isDir()) return false;
      }
      return true;
    }
  }

  /** Open externally command. */
  final class OpenExternalCmd extends GUIPopupCmd {
    /** Constructor. */
    OpenExternalCmd() { super(OPEN_EXTERNALLY, BaseXKeys.OPEN); }

    @Override public void execute() {
      for(final ProjectNode node : selectedNodes()) {
        try {
          node.file.open();
        } catch(final IOException ex) {
          BaseXDialog.error(view.gui, Util.info(FILE_NOT_OPENED_X, node.file));
        }
      }
    }
  }

  /** Copy path command. */
  final class CopyPathCmd extends GUIPopupCmd {
    /** Constructor. */
    CopyPathCmd() { super(COPY_PATH, BaseXKeys.COPYPATH); }

    @Override public void execute() {
      BaseXLayout.copy(selectedNode().file.path());
    }

    @Override public boolean enabled(final GUI main) {
      return selectedNode() != null;
    }
  }
}
