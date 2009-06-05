package org.basex.data;

/**
 * This class contains full-text positions.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTPos {
  /** Pre value. */
  public int pre;
  /** Positions. */
  public int[] pos;
  /** Pointers. */
  public byte[] poi;

  /**
   * Constructor.
   * @param p pre value
   * @param ps positions
   * @param pi pointers
   */
  FTPos(final int p, final int[] ps, final byte[] pi) {
    pre = p;
    pos = ps;
    poi = pi;
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return pos.length;
  }
}

