package org.basex.gui.view.tree;

/**
 * This class is used to store subtree borders.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller
 */
public class TreeBorder {
  /** Real Level. */
  public int level;
  /** Start index. */
  public int start;
  /** Size. */
  public int size;

  /**
   * Stores subtree borders.
   * @param lv level
   * @param st start
   * @param si size
   */
  public TreeBorder(final int lv, final int st, final int si) {
    level = lv;
    start = st;
    size = si;

  }
}
