package org.basex.gui.text;

import java.util.*;

import org.basex.util.list.*;

/**
 * Line-offset cache: document-space y and text position at each text line start. Allows
 * {@link TextRenderer} to render and address only the visible region of a document, and to
 * rebuild only the region affected by an edit (see {@link #beginUpdate}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TextLineCache {
  /** Document-space y at each line start. */
  private final IntList ys = new IntList();
  /** Text position at each line start. */
  private final IntList ps = new IntList();
  /** Highlighter state at each line start. */
  private final ArrayList<int[]> states = new ArrayList<>();
  /** Text the cache was built for (staleness guard and edit-diff reference). */
  private byte[] text;
  /** Text width the cache was built for (staleness guard). */
  private int width = -1;
  /** Border offset the cache was built for (staleness guard). */
  private int offset = -1;
  /** Document-space y after the last line (total text height). */
  private int endY;

  /** Snapshot of the reusable tail during an incremental update: y at each line. */
  private int[] tailY;
  /** Snapshot of the reusable tail: text position at each line. */
  private int[] tailPos;
  /** Snapshot of the reusable tail: highlighter state at each line. */
  private int[][] tailState;
  /** Text-position delta between old and new text (new minus old). */
  private int delta;
  /** End of the edited region (new coordinates); a line at or after it may be reused. */
  private int changeEnd;

  /**
   * Clears the cache.
   */
  void reset() {
    ys.reset();
    ps.reset();
    states.clear();
    text = null;
  }

  /**
   * Appends a line start.
   * @param y document-space y
   * @param p text position
   * @param state highlighter state at the line start
   */
  void add(final int y, final int p, final int[] state) {
    ys.add(y);
    ps.add(p);
    states.add(state);
  }

  /**
   * Marks the cache as built for the specified text, width and offset.
   * @param txt text
   * @param w text width
   * @param off border offset
   * @param ey total text height
   */
  void finish(final byte[] txt, final int w, final int off, final int ey) {
    text = txt;
    width = w;
    offset = off;
    endY = ey;
    tailY = null;
    tailPos = null;
    tailState = null;
  }

  /**
   * Checks whether the cache is present and up to date.
   * @param len current text length
   * @param w current text width
   * @return result of check
   */
  boolean valid(final int len, final int w) {
    return !ps.isEmpty() && text != null && text.length == len && width == w;
  }

  /**
   * Checks whether the cache was already built for the specified text array (compared by identity)
   * and width.
   * @param txt current text
   * @param w current text width
   * @return result of check
   */
  boolean built(final byte[] txt, final int w) {
    return text == txt && width == w;
  }

  /**
   * Checks whether the cache can be used to jump to a line, tolerating a pending length change.
   * @param w text width
   * @return result of check
   */
  boolean positionable(final int w) {
    return !ps.isEmpty() && text != null && width == w;
  }

  /**
   * Returns the number of cached lines.
   * @return number of lines
   */
  int size() {
    return ps.size();
  }

  /**
   * Returns the total text height (document-space y after the last line).
   * @return height
   */
  int endY() {
    return endY;
  }

  /**
   * Returns the index of the last line starting at or above the specified document-space y.
   * @param y document-space y
   * @return line index
   */
  int indexByY(final int y) {
    return index(ys, y);
  }

  /**
   * Returns the index of the line containing the specified text position.
   * @param p text position
   * @return line index
   */
  int indexByPos(final int p) {
    return index(ps, p);
  }

  /**
   * Returns the document-space y at the specified line index.
   * @param idx line index
   * @return y
   */
  int y(final int idx) {
    return ys.get(idx);
  }

  /**
   * Returns the text position at the specified line index.
   * @param idx line index
   * @return text position
   */
  int pos(final int idx) {
    return ps.get(idx);
  }

  /**
   * Returns the highlighter state at the specified line index.
   * @param idx line index
   * @return state
   */
  int[] state(final int idx) {
    return states.get(idx);
  }

  // INCREMENTAL UPDATE ===========================================================================

  /**
   * Prepares an incremental update by diffing the cached text against the new text, keeping the
   * lines before the edit and snapshotting the unchanged tail for {@link #splice}.
   * @param newText new text
   * @param w text width
   * @param off border offset
   * @return index of the line to resume rendering from, or {@code -1} for a full rebuild
   */
  int beginUpdate(final byte[] newText, final int w, final int off) {
    // reuse requires a valid previous cache with an identical layout
    if(text == null || width != w || offset != off || ps.isEmpty()) return -1;

    final byte[] old = text;
    final int oldLen = old.length, newLen = newText.length;
    // first differing byte (length of the common prefix)
    final int prefix = Arrays.mismatch(old, newText);
    if(prefix < 0) return -1;
    // matching trailing bytes (length of the common suffix), not crossing the prefix
    int suffix = 0;
    final int max = Math.min(oldLen, newLen) - prefix;
    while(suffix < max && old[oldLen - 1 - suffix] == newText[newLen - 1 - suffix]) suffix++;

    delta = newLen - oldLen;
    changeEnd = newLen - suffix;

    // resume at the line with the first change; its start lies before the edit and is unchanged
    final int r0 = indexByPos(prefix);

    // snapshot the reusable tail, capture the total height, then truncate the live lines
    final int sz = ps.size(), tl = sz - r0;
    tailY = new int[tl];
    tailPos = new int[tl];
    tailState = new int[tl][];
    for(int i = 0; i < tl; i++) {
      tailY[i] = ys.get(r0 + i);
      tailPos[i] = ps.get(r0 + i);
      tailState[i] = states.get(r0 + i);
    }
    ys.size(r0);
    ps.size(r0);
    states.subList(r0, sz).clear();
    return r0;
  }

  /**
   * Returns the document-space y of the resumed line.
   * @return y
   */
  int startY() {
    return tailY[0];
  }

  /**
   * Returns the text position of the resumed line.
   * @return text position
   */
  int startPos() {
    return tailPos[0];
  }

  /**
   * Returns the highlighter state of the resumed line.
   * @return state
   */
  int[] startState() {
    return tailState[0];
  }

  /**
   * Ends the incremental update if the new line coincides with an unchanged old line, appending
   * the remaining old lines with their positions and y shifted.
   * @param p text position of the new line
   * @param y document-space y of the new line
   * @param state highlighter state at the new line
   * @return {@code true} if the tail was reused and rendering can stop
   */
  boolean splice(final int p, final int y, final int[] state) {
    // a line before the end of the edit cannot match the shifted old text
    if(p < changeEnd) return false;
    // locate the old line at the matching position
    final int target = p - delta;
    int lo = 0, hi = tailPos.length - 1, j = -1;
    while(lo <= hi) {
      final int mid = lo + hi >>> 1, v = tailPos[mid];
      if(v < target) lo = mid + 1;
      else if(v > target) hi = mid - 1;
      else { j = mid; break; }
    }
    if(j < 0 || !Arrays.equals(tailState[j], state)) return false;

    // append the unchanged tail, shifted by the position and height deltas
    final int dy = y - tailY[j];
    for(int i = j, tl = tailPos.length; i < tl; i++) {
      ys.add(tailY[i] + dy);
      ps.add(tailPos[i] + delta);
      states.add(tailState[i]);
    }
    endY += dy;
    return true;
  }

  /**
   * Binary search for the largest index whose value is at or below the target.
   * @param list ascending values
   * @param target target value
   * @return index
   */
  private static int index(final IntList list, final int target) {
    int lo = 0, hi = list.size() - 1, idx = 0;
    while(lo <= hi) {
      final int mid = lo + hi >>> 1;
      if(list.get(mid) <= target) {
        idx = mid;
        lo = mid + 1;
      } else {
        hi = mid - 1;
      }
    }
    return idx;
  }
}
