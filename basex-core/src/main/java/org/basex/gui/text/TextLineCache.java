package org.basex.gui.text;

import java.util.*;

import org.basex.util.list.*;

/**
 * Line-offset cache: document-space y and text position at each text line start. Allows
 * {@link TextRenderer} to render and address only the visible region of a document.
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
  /** Text length the cache was built for (staleness guard). */
  private int length = -1;
  /** Text width the cache was built for (staleness guard). */
  private int width = -1;

  /**
   * Clears the cache.
   */
  void reset() {
    ys.reset();
    ps.reset();
    states.clear();
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
   * Marks the cache as built for the specified text length and width.
   * @param len text length
   * @param w text width
   */
  void finish(final int len, final int w) {
    length = len;
    width = w;
  }

  /**
   * Checks whether the cache is present and up to date.
   * @param len current text length
   * @param w current text width
   * @return result of check
   */
  boolean valid(final int len, final int w) {
    return !ps.isEmpty() && length == len && width == w;
  }

  /**
   * Returns the number of cached lines.
   * @return number of lines
   */
  int size() {
    return ps.size();
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
