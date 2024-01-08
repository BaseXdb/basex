package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Tree of project view.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class ProjectTree extends BaseXTree implements TreeWillExpandListener, ProjectCommands {
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
      if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
        final IOFile file = selectedFile();
        if(file != null && !file.isDir()) view.open(file, "");
      }
    });

    setCellEditor(new ProjectCellEditor(this, renderer));
    setEditable(true);
    final int h = getFontMetrics(getFont()).getHeight();
    if(h > 16) setRowHeight(Math.max(32, h));

    /* Commands for editing nodes. */

    // delete file or show error dialog
    // choose free name
    final GUIPopupCmd[] edit = {
      new GUIPopupCmd(DELETE + DOTS, BaseXKeys.DELNEXT) {
        @Override
        public void execute() {
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

        @Override
        public boolean enabled(final GUI main) {
          final ProjectNode node = selectedNode();
          return node != null && !node.root();
        }
      },

      new GUIPopupCmd(RENAME, BaseXKeys.RENAME) {
        @Override
        public void execute() {
          startEditingAtPath(selectedNode().path());
          view.refresh();
        }

        @Override
        public boolean enabled(final GUI main) {
          final ProjectNode node = selectedNode();
          return node != null && !node.root();
        }
      },

      new GUIPopupCmd(NEW_DIR, BaseXKeys.NEWDIR) {
        @Override
        public void execute() {
          final ProjectNode node = selectedNode(), dir = node instanceof ProjectDir ? node :
            (ProjectNode) node.getParent();

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

        @Override
        public boolean enabled(final GUI main) {
          return selectedNode() != null;
        }
      },
      null
    };
    new BaseXPopup(this, view.gui, commands(edit));
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

  @Override
  public void refresh() {
    view.refresh();
    selectedNode().refresh();
  }

  @Override
  public ProjectView view() {
    return view;
  }

  @Override
  public String search() {
    return "";
  }

  @Override
  public IOFile selectedFile() {
    final ProjectNode node = selectedNode();
    return node != null ? node.file : null;
  }

  @Override
  public List<IOFile> selectedFiles() {
    final ArrayList<IOFile> files = new ArrayList<>();
    final TreePath[] paths = getSelectionPaths();
    if(paths != null) {
      for(final TreePath tp : paths) {
        final Object node = tp.getLastPathComponent();
        if(node instanceof ProjectNode) files.add(((ProjectNode) node).file);
      }
    }
    return files;
  }

  // PRIVATE METHOS ===============================================================================

  /**
   * Returns the selected path, or returns {@code null} if zero or more than paths are selected.
   * @return path
   */
  private TreePath selectedPath() {
    final TreePath[] tps = getSelectionPaths();
    return tps == null || tps.length > 1 ? null : tps[0];
  }
}
