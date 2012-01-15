package org.basex.gui.layout;

import javax.swing.JTree;

import org.basex.data.Data;

/**
 * JTree node which represents a leaf (which represents either a raw file
 * or a document).
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class TreeLeaf extends TreeNode {

  /** Represents raw file (true) or document (false). */
  final boolean r;

  /**
   * Constructor.
   * @param path node path
   * @param name name of content node
   * @param raw represented node is raw file
   * @param jtree JTree reference
   * @param data data reference
   */
  public TreeLeaf(final byte[] name, final byte[] path, final boolean raw,
      final JTree jtree, final Data data) {
    super(name, path, jtree, data);
    r = raw;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  void load() {
    // leafs cannot be expanded
  }
}
