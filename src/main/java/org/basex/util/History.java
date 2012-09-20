package org.basex.util;

/**
 * This class stores strings in a history.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class History {
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
  /** Save position. */
  private int saved;

  /**
   * Constructor.
   */
  public History() {
    hist[0] = Token.EMPTY;
  }

  /**
   * Checks if the history points to the first entry.
   * @return result of check
   */
  public boolean first() {
    return pos == 0;
  }

  /**
   * Checks if the history points to the last entry.
   * @return result of check
   */
  public boolean last() {
    return pos == max;
  }

  /**
   * Returns the previous string and decreases the pointer, or returns {@code null}.
   * @return previous string
   */
  public byte[] prev() {
    return pos != 0 ? hist[--pos] : null;
  }

  /**
   * Returns the next string and increases the pointer, or returns {@code null}.
   * @return previous string
   */
  public byte[] next() {
    return pos < max ?  hist[++pos] : null;
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
   * @param oc old cursor position
   * @param nc new cursor position
   */
  public void store(final byte[] str, final int oc, final int nc) {
    if(str == hist[pos] || Token.eq(str, hist[pos]) || str.length >= MAXSIZE) return;
    cur[pos] = Math.min(hist[pos].length, oc);
    if(pos + 1 == MAX) {
      Array.move(hist, 1, -1, pos);
      Array.move(cur, 1, -1, pos--);
      saved--;
    }
    hist[++pos] = str;
    cur[pos] = nc;
    max = pos;
  }

  /**
   * Sets the saved position.
   */
  public void save() {
    saved = pos;
  }

  /**
   * Checks if the file has been modified.
   * @return result of check
   */
  public boolean modified() {
    return saved != pos;
  }
}
