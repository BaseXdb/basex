package org.basex.util;

import java.util.*;

/**
 * This class stores the undo history of a text as change records.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class History {
  /** Maximum size of stored change records. */
  private static final long MAXBYTES = 100_000_000;
  /** Maximum number of entries to be stored. */
  private static final int MAX = 1024;

  /** Change offsets (index {@code i} describes the transition from state {@code i - 1}). */
  private final int[] offset;
  /** Removed bytes per change. */
  private final byte[][] oldMid;
  /** Inserted bytes per change. */
  private final byte[][] newMid;
  /** Caret history (caret position per state). */
  private final int[] caret;
  /** Active flag. */
  private final boolean active;

  /** Current text (state at {@link #pos}). */
  private byte[] current;
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
      offset = new int[MAX];
      oldMid = new byte[MAX][];
      newMid = new byte[MAX][];
      init(text);
    } else {
      caret = null;
      offset = null;
      oldMid = null;
      newMid = null;
    }
  }

  /**
   * Initializes the history with a text.
   * @param text initial text
   */
  public void init(final byte[] text) {
    current = text;
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
   * Returns the previous text and decreases the pointer, or returns {@code null}.
   * @return previous text or {@code null}
   */
  public byte[] prev() {
    if(pos == 0) return null;
    // rebuild the previous state: replace the inserted bytes with the removed ones
    current = apply(pos, newMid[pos], oldMid[pos]);
    pos--;
    return current;
  }

  /**
   * Returns the next text and increases the pointer, or returns {@code null}.
   * @return next text or {@code null}
   */
  public byte[] next() {
    if(pos == max) return null;
    pos++;
    // rebuild the next state: replace the removed bytes with the inserted ones
    current = apply(pos, oldMid[pos], newMid[pos]);
    return current;
  }

  /**
   * Rebuilds a neighboring state from the current text.
   * @param i change index
   * @param del bytes to be replaced
   * @param ins replacement bytes
   * @return rebuilt text
   */
  private byte[] apply(final int i, final byte[] del, final byte[] ins) {
    final int o = offset[i];
    return splice(current, o, del.length, ins);
  }

  /**
   * Returns the caret position.
   * @return caret position
   */
  public int caret() {
    return caret[pos];
  }

  /**
   * Stores a text in the history.
   * @param str text to be stored
   * @param oc old caret position
   * @param nc new caret position
   */
  public void store(final byte[] str, final int oc, final int nc) {
    if(!active || str == current || Token.eq(str, current)) return;

    // diff the current text against the new text: keep only the changed middle part
    final int prefix = Arrays.mismatch(current, str);
    int suffix = 0;
    final int mx = Math.min(current.length, str.length) - prefix;
    while(suffix < mx && current[current.length - 1 - suffix] == str[str.length - 1 - suffix]) {
      suffix++;
    }
    final int off = prefix;
    final byte[] oMid = Arrays.copyOfRange(current, prefix, current.length - suffix);
    final byte[] nMid = Arrays.copyOfRange(str, prefix, str.length - suffix);

    // merge consecutive character inputs without deletions
    final int prevLen = pos > 0 ? current.length - newMid[pos].length + oldMid[pos].length : 0;
    if(pos > 0 && saved != pos && caret[pos] == oc && oc + 1 == nc && prevLen < str.length &&
        off >= offset[pos] && off + oMid.length <= offset[pos] + newMid[pos].length) {
      // fold the new bytes into the inserted part of the current change
      newMid[pos] = splice(newMid[pos], off - offset[pos], oMid.length, nMid);
      current = str;
      caret[pos] = nc;
      return;
    }

    // summarize and limit size of stored records
    long len = (long) oMid.length + nMid.length;
    int cut = pos + 1;
    for(; cut > 0 && len < MAXBYTES; cut--) {
      final int idx = cut - 1;
      if(idx >= 1) len += oldMid[idx].length + newMid[idx].length;
    }
    // enough space: limit number of entries
    if(cut == 0 && pos + 1 == MAX) cut = 1;
    // remove entries
    if(cut > 0) {
      Array.remove(offset, 0, cut, MAX);
      Array.remove(oldMid, 0, cut, MAX);
      Array.remove(newMid, 0, cut, MAX);
      Array.remove(caret, 0, cut, MAX);
      saved -= cut;
      pos -= cut;
    }
    // save new entry
    if(pos >= 0) caret[pos] = oc;
    if(saved > pos) saved = -1;
    max = ++pos;
    offset[pos] = off;
    oldMid[pos] = oMid;
    newMid[pos] = nMid;
    caret[pos] = nc;
    current = str;
    // remove old entries to save memory
    Arrays.fill(oldMid, pos + 1, MAX, null);
    Arrays.fill(newMid, pos + 1, MAX, null);
  }

  /**
   * Replaces {@code delLen} bytes at {@code from} in an array with the specified bytes.
   * @param array source array
   * @param from start offset
   * @param delLen number of bytes to be replaced
   * @param ins bytes to be inserted
   * @return new array
   */
  private static byte[] splice(final byte[] array, final int from, final int delLen,
      final byte[] ins) {
    final byte[] result = new byte[array.length - delLen + ins.length];
    System.arraycopy(array, 0, result, 0, from);
    System.arraycopy(ins, 0, result, from, ins.length);
    System.arraycopy(array, from + delLen, result, from + ins.length,
        array.length - from - delLen);
    return result;
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
