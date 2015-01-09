package org.basex.gui.layout;

import org.basex.data.*;

/**
 * JTree node which represents a leaf (which represents either a raw file
 * or a document).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
public final class TreeLeaf extends TreeNode {
  /** Represents raw file (true) or document (false). */
  public final boolean raw;
  /** This leaf is a dummy node displaying that node listing is abbreviated. */
  public final boolean abbr;

  /**
   * Constructor.
   * @param nm name of content node
   * @param pth node path
   * @param rw represented node is raw file
   * @param ab abbreviated node listing
   * @param bxt tree reference
   * @param d data reference
   */
  public TreeLeaf(final byte[] nm, final byte[] pth, final boolean rw, final boolean ab,
      final BaseXTree bxt, final Data d) {
    super(nm, pth, bxt, d);
    raw = rw;
    abbr = ab;
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
