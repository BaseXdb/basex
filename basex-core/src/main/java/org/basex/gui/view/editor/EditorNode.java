package org.basex.gui.view.editor;

import javax.swing.tree.*;

import org.basex.io.*;

/**
 * Single tree node.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class EditorNode extends DefaultMutableTreeNode {
  /** Model. */
  protected final EditorTree tree;
  /** Path. */
  protected IOFile file;

  /**
   * Constructor.
   * @param io file reference
   * @param tr tree
   */
  EditorNode(final IOFile io, final EditorTree tr) {
    super(io == null ? null : io.name());
    file = io;
    tree = tr;
  }

  @Override
  public void setUserObject(final Object uo) {
    final IOFile renamed = tree.rename(this, uo.toString());
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
    final DefaultTreeModel model = (DefaultTreeModel) tree.tree.getModel();
    model.nodeStructureChanged(this);
    tree.repaint();
  }

  /**
   * Returns the node path.
   * @return path
   */
  final TreePath path() {
    final DefaultTreeModel model = (DefaultTreeModel) tree.tree.getModel();
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
