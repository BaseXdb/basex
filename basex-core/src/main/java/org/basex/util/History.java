package org.basex.util;

import java.util.*;

/**
 * This class stores the undo history of a text as change records.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class History {
  /** A single change: replaces the removed bytes at an offset with the inserted bytes. */
  private static final class Change {
    /** Change offset. */
    private final int offset;
    /** Removed bytes. */
    private final byte[] oldMid;
    /** Caret position before the change. */
    private final int oldCaret;
    /** Inserted bytes (extended when consecutive input is folded in). */
    private byte[] newMid;
    /** Caret position after the change (updated when input is folded in). */
    private int newCaret;

    /**
     * Constructor.
     * @param offset change offset
     * @param oldMid removed bytes
     * @param newMid inserted bytes
     * @param oldCaret caret position before the change
     * @param newCaret caret position after the change
     */
    private Change(final int offset, final byte[] oldMid, final byte[] newMid,
        final int oldCaret, final int newCaret) {
      this.offset = offset;
      this.oldMid = oldMid;
      this.newMid = newMid;
      this.oldCaret = oldCaret;
      this.newCaret = newCaret;
    }
  }

  /** Active flag. */
  private final boolean active;
  /** Stored changes (unbounded); change {@code i} transitions state {@code i} to {@code i + 1}. */
  private final ArrayList<Change> changes = new ArrayList<>();

  /** Current text (state at {@link #pos}). */
  private byte[] current;
  /** Caret position of the current state. */
  private int caret;
  /** History position (number of applied changes). */
  private int pos;
  /** Save position. */
  private int saved;
  /** Navigation flag: an undo or redo breaks the current typing run. */
  private boolean navigated;

  /**
   * Constructor.
   * @param text initial text
   */
  public History(final byte[] text) {
    active = text != null;
    if(active) init(text);
  }

  /**
   * Initializes the history with a text.
   * @param text initial text
   */
  public void init(final byte[] text) {
    changes.clear();
    current = text;
    caret = 0;
    pos = 0;
    saved = 0;
    navigated = false;
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
    return pos == changes.size();
  }

  /**
   * Returns the previous text and decreases the pointer, or returns {@code null}.
   * @return previous text or {@code null}
   */
  public byte[] prev() {
    if(pos == 0) return null;
    // rebuild the previous state by replacing the inserted bytes with the removed ones
    final Change c = changes.get(--pos);
    current = splice(current, c.offset, c.newMid.length, c.oldMid);
    // undoing a change returns the caret to where it was before that change
    caret = c.oldCaret;
    navigated = true;
    return current;
  }

  /**
   * Returns the next text and increases the pointer, or returns {@code null}.
   * @return next text or {@code null}
   */
  public byte[] next() {
    if(pos == changes.size()) return null;
    // rebuild the next state by replacing the removed bytes with the inserted ones
    final Change c = changes.get(pos++);
    current = splice(current, c.offset, c.oldMid.length, c.newMid);
    // redoing a change places the caret where that change left it
    caret = c.newCaret;
    navigated = true;
    return current;
  }

  /**
   * Returns the caret position.
   * @return caret position
   */
  public int caret() {
    return caret;
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
    final byte[] oMid = Arrays.copyOfRange(current, prefix, current.length - suffix);
    final byte[] nMid = Arrays.copyOfRange(str, prefix, str.length - suffix);

    // fold a single typed character into the current change (contiguous, growing insertion)
    final Change last = pos > 0 ? changes.get(pos - 1) : null;
    if(last != null && !navigated && saved != pos && last.newCaret == oc && oc + 1 == nc &&
        current.length - last.newMid.length + last.oldMid.length < str.length &&
        prefix >= last.offset && prefix + oMid.length <= last.offset + last.newMid.length) {
      last.newMid = splice(last.newMid, prefix - last.offset, oMid.length, nMid);
      last.newCaret = nc;
      current = str;
      caret = nc;
      return;
    }

    // discard the invalidated redo branch, then append the new change
    if(saved > pos) saved = -1;
    changes.subList(pos, changes.size()).clear();
    changes.add(new Change(prefix, oMid, nMid, oc, nc));
    pos++;
    current = str;
    caret = nc;
    navigated = false;
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
