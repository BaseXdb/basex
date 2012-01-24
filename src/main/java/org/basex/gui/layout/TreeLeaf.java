package org.basex.gui.layout;

import org.basex.data.Data;


/**
 * JTree node which represents a leaf (which represents either a raw file
 * or a document).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class TreeLeaf extends TreeNode {
  /** Represents raw file (true) or document (false). */
  public final boolean raw;

  /**
   * Constructor.
   * @param pth node path
   * @param nm name of content node
   * @param rw represented node is raw file
   * @param bxt tree reference
   * @param d data reference
   */
  public TreeLeaf(final byte[] nm, final byte[] pth, final boolean rw,
      final BaseXTree bxt, final Data d) {
    super(nm, pth, bxt, d);
    raw = rw;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  void load() {
    // leaves cannot be expanded
  }
}
