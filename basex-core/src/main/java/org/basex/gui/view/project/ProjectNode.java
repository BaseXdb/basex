package org.basex.gui.view.project;

import javax.swing.tree.*;

import org.basex.io.*;

/**
 * Single tree node.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
abstract class ProjectNode extends DefaultMutableTreeNode {
  /** Project view. */
  final ProjectView project;
  /** File reference ({@code null} for invisible dummy). */
  IOFile file;
  /** Error flag. */
  boolean error;

  /**
   * Constructor.
   * @param file file reference ({@code null} for dummy)
   * @param project project view
   */
  ProjectNode(final IOFile file, final ProjectView project) {
    super(file == null ? null : file.name());
    this.file = file;
    this.project = project;
  }

  @Override
  public void setUserObject(final Object uo) {
    final IOFile renamed = project.rename(this, uo.toString());
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
   * Refreshes the current node.
   */
  abstract void refresh();

  /**
   * Updates the tree structure.
   */
  final void updateTree() {
    ((DefaultTreeModel) project.tree.getModel()).nodeStructureChanged(this);
    project.repaint();
  }

  /**
   * Returns the node path.
   * @return path
   */
  final TreePath path() {
    final DefaultTreeModel model = (DefaultTreeModel) project.tree.getModel();
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
