package org.basex.gui.view.project;

import javax.swing.tree.*;

import org.basex.io.*;

/**
 * Single tree node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
abstract class ProjectNode extends DefaultMutableTreeNode {
  /** Project view. */
  final ProjectView project;
  /** Path. */
  IOFile file;

  /**
   * Constructor.
   * @param io file reference
   * @param proj project view
   */
  ProjectNode(final IOFile io, final ProjectView proj) {
    super(io == null ? null : io.name());
    file = io;
    project = proj;
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
  final void refresh() {
    collapse();
    expand();
    updateTree();
  }

  /**
   * Updates the tree structure.
   */
  final void updateTree() {
    final DefaultTreeModel model = (DefaultTreeModel) project.tree.getModel();
    model.nodeStructureChanged(this);
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
