package org.basex.gui.text;

import static org.basex.util.FTToken.*;
import static org.basex.util.Token.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Returns an iterator for the visualized text.
 *
 * @author BaseX Team 2005-21, BSD License
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

    // find next token boundary
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
   * Tests if the current text position is selected.
   * @return result of check
   */
  boolean selectStart() {
    return start != end && (inSelect() ||
        (start < end ? start >= pos && start < posEnd : end >= pos && end < posEnd));
  }

  /**
   * Tests if the current text position is selected.
   * @return result of check
   */
  boolean inSelect() {
    return start < end ? pos >= start && pos < end : pos >= end && pos < start;
  }

  /**
   * Returns true if the cursor focuses a search string.
   * @return result of check
   */
  boolean searchStart() {
    if(searchResults == null) return false;
    if(searchIndex == searchResults[0].size()) return false;
    while(pos > searchResults[1].get(searchIndex)) {
      if(++searchIndex == searchResults[0].size()) return false;
    }
    return posEnd > searchResults[0].get(searchIndex);
  }

  /**
   * Tests if the current position is within a search term.
   * @return result of check
   */
  boolean inSearch() {
    final IntList starts = searchResults[0], ends = searchResults[1];
    final int i = searchIndex;
    if(i >= starts.size() || pos < starts.get(i)) return false;
    final boolean in = pos < ends.get(i);
    if(!in) searchIndex++;
    return in;
  }

  /**
   * Tests if the current token is erroneous.
   * @return result of check
   */
  boolean erroneous() {
    return errPos >= pos && errPos < posEnd;
  }

  /**
   * Returns the error position.
   * @return error position
   */
  int error() {
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
