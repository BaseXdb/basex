package org.basex.gui.text;

import static org.basex.util.FTToken.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Returns an iterator for the visualized text.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class TextIterator {
  /** Text. */
  private final byte[] text;
  /** Text length. */
  private final int length;
  /** Caret position. */
  private final int caret;
  /** Start position of a text selection. */
  private final int start;
  /** End position of a text selection (+1). */
  private final int end;
  /** Start position of an error highlighting. */
  private final int errPos;
  /** Start and end positions of search terms. */
  private final IntList[] searchResults;

  /** Current start position. */
  private int pos;
  /** Current end position. */
  private int posEnd;
  /** Current search index. */
  private int searchIndex;
  /** Indicates if current token is part of a link. */
  private boolean link;

  /**
   * Constructor.
   * @param et text reference
   */
  TextIterator(final TextEditor et) {
    text = et.text();
    length = text.length;
    caret = et.pos();
    start = et.start;
    end = et.end;
    errPos = et.error;
    searchResults = et.searchResults;
  }

  /**
   * Checks if the text contains more strings.
   * @param max maximum number of characters to check (long strings will be chopped later on)
   * @return result of check
   */
  boolean moreStrings(final int max) {
    final int l = length;
    int p = posEnd;
    pos = p;
    if(p >= l) return false;

    // find next token boundary (excessively long tokens will be wrapped later on)
    final int e = pos + max;
    final byte[] txt = text;
    int ch = cp(txt, p);
    p += cl(txt, p);
    if(lod(ch)) {
      while(p < l && p < e) {
        ch = cp(txt, p);
        if(!lod(ch)) break;
        p += cl(txt, p);
      }
    }
    posEnd = p;
    return true;
  }

  /**
   * Returns the current string.
   * @return string
   */
  String currString() {
    return posEnd <= length ? string(text, pos, posEnd - pos) : "";
  }

  /**
   * Returns a substring.
   * @param s start position
   * @param e end position
   * @return string
   */
  String substring(final int s, final int e) {
    return string(text, s, e - s);
  }

  /**
   * Checks if the caret is in the current line.
   * @param first first call
   * @return iterator position
   */
  boolean caretLine(final boolean first) {
    for(int p = pos + (first ? 0 : 1); p < length; p++) {
      if(p == caret) return true;
      if(text[p] == '\n') return false;
    }
    return caret == length;
  }

  /**
   * Returns the current character.
   * @return current character
   */
  int curr() {
    return cp(text, pos);
  }

  /**
   * Returns the iterator position.
   * @return iterator position
   */
  int pos() {
    return pos;
  }

  /**
   * Returns the iterator end position.
   * @return iterator end position
   */
  int posEnd() {
    return posEnd;
  }

  /**
   * Sets the iterator position.
   * @param p iterator position
   */
  void pos(final int p) {
    pos = p;
  }

  /**
   * Sets the iterator end position.
   * @param p iterator position
   */
  void posEnd(final int p) {
    posEnd = p;
  }

  /**
   * Checks if the character position equals the word end.
   * @return result of check
   */
  boolean more() {
    return pos < posEnd;
  }

  /**
   * Returns the current character and moves one character forward.
   * @return current character
   */
  int next() {
    final int c = curr();
    pos += cl(text, pos);
    return c;
  }

  /**
   * Checks if the caret position is within the current token.
   * @return result of check
   */
  boolean edited() {
    return caret >= pos && caret < posEnd;
  }

  /**
   * Returns the position of the text cursor.
   * @return cursor position
   */
  int caret() {
    return caret;
  }

  /**
   * Returns a selection range.
   * @return range or {@code null}
   */
  int[] selection() {
    if(start != end) {
      final boolean asc = start < end;
      final int s = asc ? start : end, e = asc ? end : start;
      if(pos >= s && pos < e || s >= pos && s < posEnd) return new int[] { s, e };
    }
    return null;
  }

  /** Search results. */
  private final ArrayList<int[]> results = new ArrayList<>();

  /**
   * Returns the next search result range.
   * @return range or {@code null}
   */
  ArrayList<int[]> searchResults() {
    results.clear();
    if(searchResults != null) {
      final IntList starts = searchResults[0], ends = searchResults[1];
      int si = searchIndex;
      for(final int ss = starts.size(); si < ss; ++si) {
        final int s = starts.get(si), e = ends.get(si);
        if(s >= posEnd) break;
        results.add(new int[] { s, e });
      }
      searchIndex = results.isEmpty() ? si : si - 1;
    }
    return results;
  }

  /**
   * Tests if the current token is erroneous.
   * @return result of check
   */
  boolean error() {
    return errPos >= pos && errPos < posEnd;
  }

  /**
   * Returns the error position.
   * @return error position
   */
  int errorPos() {
    return errPos;
  }

  /**
   * Sets the link flag.
   * @param lnk flag
   */
  void link(final boolean lnk) {
    link = lnk;
  }

  /**
   * Retrieves the current hyperlink.
   * @return link string or {@code null}
   */
  String link() {
    if(!link) return null;
    // find beginning and end of link
    int ls = pos, le = ls;
    while(--ls > 0 && (text[ls] < -64 || cp(text, ls) != TokenBuilder.ULINE));
    while(++le < length && (text[le] < -64 || cp(text, le) != TokenBuilder.ULINE));
    ls += cl(text, ls);
    return string(text, ls, le - ls);
  }
}
