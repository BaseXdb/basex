package org.basex.util;

/**
 * This class stores strings in a history.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Undo {
  /** Maximum number of strings to be stored. */
  private static final int MAX = 200;
  /** String history. */
  private byte[][] hist = new byte[MAX][];
  /** Cursor history. */
  private int[] cur = new int[MAX];
  /** Maximum of stored entries. */
  private int max;
  /** History position. */
  private int pos;

  /**
   * Constructor.
   */
  public Undo() {
    hist[0] = Token.EMPTY;
  }

  /**
   * Returns the previous string.
   * @return previous string
   */
  public boolean first() {
    return pos == 0;
  }

  /**
   * Returns the previous string.
   * @return previous string
   */
  public boolean last() {
    return pos == max;
  }

  /**
   * Returns the previous string.
   * @return previous string
   */
  public byte[] prev() {
    return hist[pos == 0 ? pos : --pos];
  }

  /**
   * Returns the next string.
   * @return previous string
   */
  public byte[] next() {
    return hist[pos == max ? pos : ++pos];
  }

  /**
   * Returns the cursor position.
   * @return cursor position
   */
  public int cursor() {
    return cur[pos];
  }

  /**
   * Stores a string in the history. 
   * @param str string to be stored
   * @param c cursor position
   */
  public void store(final byte[] str, final int c) {
    if(Token.eq(str, hist[pos])) return;
    if(pos + 1 == MAX) {
      Array.move(hist, 1, -1, pos);
      Array.move(cur, 1, -1, pos--);
    }
    hist[++pos] = str;
    cur[pos] = c;
    max = pos;
  }
}

