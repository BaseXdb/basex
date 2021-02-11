package org.basex.gui.view.project;

import javax.swing.tree.*;

import org.basex.io.*;

/**
 * Single tree node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class ProjectNode extends DefaultMutableTreeNode {
  /** Project view. */
  final ProjectView view;
  /** File reference ({@code null} for invisible dummy). */
  IOFile file;
  /** Error flag. */
  boolean error;

  /**
   * Constructor.
   * @param file file reference ({@code null} for dummy)
   * @param view project view
   */
  ProjectNode(final IOFile file, final ProjectView view) {
    super(file == null ? null : file.name());
    this.file = file;
    this.view = view;
  }

  @Override
  public void setUserObject(final Object uo) {
    final IOFile renamed = view.rename(this, uo.toString());
    if(renamed != null) {
      file = renamed;
      refresh();
    }
  }

  /**
   * Expands the current node.
   */
  abstract void expand();

  /**
   * Collapses the current node.
   */
  abstract void collapse();

  /**
   * Refreshes the current node and its children.
   */
  abstract void refresh();

  /**
   * Returns the node path.
   * @return path
   */
  final TreePath path() {
    final DefaultTreeModel model = (DefaultTreeModel) view.tree.getModel();
    return new TreePath(model.getPathToRoot(this));
  }

  /**
   * Checks if this node is placed on top of the tree.
   * @return result of check
   */
  final boolean root() {
    return getParent().getParent() == null;
  }
}
