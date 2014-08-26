package org.basex.gui.text;

import static org.basex.util.Token.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Returns an iterator for the visualized text.
 *
 * @author BaseX Team 2005-14, BSD License
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
  private final IntList[] searchPos;

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
    searchPos = et.searchPos;
  }

  /**
   * Checks if the text contains more words.
   * @return result of check
   */
  boolean moreTokens() {
    int p = posEnd;
    pos = p;
    if(p >= length) return false;

    // find next token boundary
    int ch = cp(text, p);
    p += cl(text, p);
    if(ftChar(ch)) {
      while(p < length) {
        ch = cp(text, p);
        if(!ftChar(ch)) break;
        p += cl(text, p);
      }
    }
    posEnd = p;
    return true;
  }

  /**
   * Returns the token as string.
   * @return string
   */
  String nextString() {
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
   * Checks if the character position equals the word end.
   * @return result of check
   */
  boolean more() {
    return pos < posEnd;
  }

  /**
   * Moves one character forward.
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
        (start < end ? start >= pos && start < posEnd :
          end >= pos && end < posEnd));
  }

  /**
   * Tests if the current text position is selected.
   * @return result of check
   */
  boolean inSelect() {
    return start < end ? pos >= start && pos < end :
      pos >= end && pos < start;
  }

  /**
   * Returns true if the cursor focuses a search string.
   * @return result of check
   */
  boolean searchStart() {
    if(searchPos == null) return false;
    if(searchIndex == searchPos[0].size()) return false;
    while(pos > searchPos[1].get(searchIndex)) {
      if(++searchIndex == searchPos[0].size()) return false;
    }
    return posEnd > searchPos[0].get(searchIndex);
  }

  /**
   * Tests if the current position is within a search term.
   * @return result of check
   */
  boolean inSearch() {
    if(searchIndex >= searchPos[0].size() || pos < searchPos[0].get(searchIndex)) return false;
    final boolean in = pos < searchPos[1].get(searchIndex);
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
   * @return link string
   */
  String link() {
    if(!link) return null;

    // find beginning and end of link
    int ls = pos, le = ls;
    while(ls > 0 && text[ls - 1] != TokenBuilder.ULINE) ls--;
    while(le < length && text[le] != TokenBuilder.ULINE) le++;
    return string(text, ls, le - ls);
  }
}
