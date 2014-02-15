package org.basex.util;

/**
 * This class stores strings in a history.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class History {
  /** Maximum size for storing entries in a history (currently: 10 MB). */
  private static final int MAXBYTES = 10000000;
  /** Maximum number of entries to be stored. */
  private static final int MAX = 1024;

  /** String history. */
  private final byte[][] hist;
  /** Caret history. */
  private final int[] caret;
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
      caret = new int[MAX];
      hist = new byte[MAX][];
      init(text);
    } else {
      caret = null;
      hist = null;
    }
  }

  /**
   * Initializes the history with a text.
   * @param text initial text
   */
  public void init(final byte[] text) {
    hist[0] = text;
    pos = 0;
    max = 0;
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
   * Returns the caret position.
   * @return caret position
   */
  public int caret() {
    return caret[pos];
  }

  /**
   * Stores a string in the history.
   * @param str string to be stored
   * @param oc old caret position
   * @param nc new caret position
   */
  public void store(final byte[] str, final int oc, final int nc) {
    if(!active || str == hist[pos] || Token.eq(str, hist[pos])) return;

    // merge consecutive character inputs without deletions
    int len = str.length;
    if(pos > 0 && saved != pos && caret[pos] == oc && oc + 1 == nc &&
        hist[pos - 1].length < len) {
      hist[pos] = str;
      caret[pos] = nc;
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
      Array.move(caret, off, -off, MAX - off);
      saved -= off;
      pos -= off;
    }
    // save new entry
    if(pos >= 0) caret[pos] = oc;
    hist[++pos] = str;
    caret[pos] = nc;
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
