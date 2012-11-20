package org.basex.util;

/**
 * This class stores strings in a history.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class History {
  /** Maximum size for storing entries in a history (currently: 10 MB). */
  private static final int MAXBYTES = 10000000;
  /** Maximum number of entries to be stored. */
  private static final int MAX = 1024;

  /** String history. */
  private final byte[][] hist;
  /** Cursor history. */
  private final int[] cur;
  /** Active flag. */
  private final boolean active;

  /** Maximum of stored entries. */
  private int max;
  /** History position. */
  private int pos;
  /** Save position. */
  private int saved;

  /**
   * Constructor.
   * @param text initial text
   */
  public History(final byte[] text) {
    active = text != null;
    if(active) {
      cur = new int[MAX];
      hist = new byte[MAX][];
      hist[0] = text;
    } else {
      cur = null;
      hist = null;
    }
  }

  /**
   * Indicates if the history is active.
   * @return result of check
   */
  public boolean active() {
    return active;
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
    return pos > 0 ? hist[--pos] : null;
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
    if(!active || str == hist[pos] || Token.eq(str, hist[pos])) return;

    // merge consecutive character inputs without deletions
    int len = str.length;
    if(pos > 0 && saved != pos && cur[pos] == oc && oc + 1 == nc &&
        hist[pos - 1].length < len) {
      hist[pos] = str;
      cur[pos] = nc;
      return;
    }

    // summarize and limit size of new and existing entries
    int off = pos + 1;
    for(; off > 0 && len < MAXBYTES; off--) len += hist[off - 1].length;
    // enough space: limit number of entries
    if(off == 0 && pos + 1 == MAX) off = 1;
    // remove entries
    if(off > 0) {
      Array.move(hist, off, -off, MAX - off);
      Array.move(cur, off, -off, MAX - off);
      saved -= off;
      pos -= off;
    }
    // save new entry
    if(pos >= 0) cur[pos] = oc;
    hist[++pos] = str;
    cur[pos] = nc;
    max = pos;
    // remove old entries to save memory
    for(int p = pos + 1; p < MAX; p++) hist[p] = null;
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
