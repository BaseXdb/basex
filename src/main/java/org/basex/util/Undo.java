package org.basex.util;

/**
 * This class stores strings in a history.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Undo {
  /** Maximum size for storing entries in a history. */
  public static final int MAXSIZE = 100000;
  /** Maximum number of strings to be stored. */
  private static final int MAX = 500;
  /** String history. */
  private final byte[][] hist = new byte[MAX][];
  /** Cursor history. */
  private final int[] cur = new int[MAX];
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
   * Sets the current cursor position before adding a new entry.
   * @param c cursor position
   */
  public void cursor(final int c) {
    cur[pos] = Math.min(hist[pos].length, c);
  }

  /**
   * Stores a string in the history.
   * @param str string to be stored
   * @param c cursor position
   */
  public void store(final byte[] str, final int c) {
    if(Token.eq(str, hist[pos]) || str.length >= MAXSIZE) return;
    if(pos + 1 == MAX) {
      Array.move(hist, 1, -1, pos);
      Array.move(cur, 1, -1, pos--);
    }
    hist[++pos] = str;
    cur[pos] = c;
    max = pos;
  }

  /**
   * Resets the undo history with the specified text.
   * @param text initial text
   */
  public void reset(final byte[] text) {
    hist[0] = text;
    pos = 0;
    max = 0;
  }
}

