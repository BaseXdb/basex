package org.basex.query.util.collation;

import java.util.function.*;

import org.basex.util.list.*;

/**
 * Collation elements of a string, together with the offsets of the characters they stem from.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class CollationElements {
  /** Collation elements. */
  private final IntList elements = new IntList();
  /** Start offsets of the characters of the elements. */
  private final IntList starts = new IntList();
  /** End offsets of the characters of the elements. */
  private final IntList ends = new IntList();

  /** Iterator function that returns the next element (can be {@code null}). */
  private IntSupplier next;
  /** Iterator function that returns the current string offset (can be {@code null}). */
  private final IntSupplier offset;
  /** String offset after the last iterator element. */
  private int before;
  /** Start offset of the character of the last iterator element. */
  private int start;

  /**
   * Constructor for collation element iterators, which are consumed on demand.
   * @param next returns the next element ({@code 0} if ignorable, {@code -1} at the end)
   * @param offset returns the current string offset
   */
  CollationElements(final IntSupplier next, final IntSupplier offset) {
    this.next = next;
    this.offset = offset;
    before = offset.getAsInt();
  }

  /**
   * Constructor for strings whose folded codepoints are compared.
   * @param string string
   * @param fold folding function, which yields as many codepoints per character in any context
   */
  CollationElements(final String string, final UnaryOperator<String> fold) {
    offset = null;
    final String folded = fold.apply(string);
    // if the number of codepoints is unchanged, each character yields one codepoint
    final boolean single = folded.codePointCount(0, folded.length()) ==
      string.codePointCount(0, string.length());
    for(int s = 0, e, f = 0, sl = string.length(); s < sl; s = e) {
      final int cp = string.codePointAt(s);
      e = s + Character.charCount(cp);
      final long fl = single || cp < 0x80 ? 1 :
        fold.apply(Character.toString(cp)).codePoints().count();
      for(long n = 0; n < fl; n++) {
        final int fcp = folded.codePointAt(f);
        add(fcp, s, e);
        f += Character.charCount(fcp);
      }
    }
  }

  /**
   * Returns the position of the first or last occurrence of the specified elements.
   * @param sub elements to be found
   * @param last search last occurrence
   * @return position, or {@code -1} if the elements are not found
   */
  int indexOf(final CollationElements sub, final boolean last) {
    final int ss = sub.size();
    if(last) {
      for(int e = size() - ss; e >= 0; e--) {
        if(matches(sub, e)) return e;
      }
    } else {
      for(int e = 0; has(e + ss - 1); e++) {
        if(matches(sub, e)) return e;
      }
    }
    return -1;
  }

  /**
   * Checks if the specified elements occur at the specified position.
   * @param sub elements to be found
   * @param pos position
   * @return result of check
   */
  boolean matches(final CollationElements sub, final int pos) {
    final int ss = sub.size();
    if(pos < 0 || !has(pos + ss - 1)) return false;
    for(int s = 0; s < ss; s++) {
      if(elements.get(pos + s) != sub.elements.get(s)) return false;
    }
    return true;
  }

  /**
   * Returns the number of elements.
   * @return number of elements
   */
  int size() {
    has(Integer.MAX_VALUE);
    return elements.size();
  }

  /**
   * Returns the string offset before the specified element.
   * @param pos position of the element
   * @return string offset
   */
  int start(final int pos) {
    return starts.get(pos);
  }

  /**
   * Returns the string offset after the specified element.
   * @param pos position of the element
   * @return string offset
   */
  int end(final int pos) {
    return ends.get(pos);
  }

  /**
   * Checks if an element exists at the specified position, consuming iterator elements on demand.
   * @param pos position
   * @return result of check
   */
  private boolean has(final int pos) {
    while(pos >= elements.size() && next != null) {
      final int element = next.getAsInt();
      if(element == -1) {
        next = null;
      } else {
        final int after = offset.getAsInt();
        // further elements of an expansion have no extent and belong to the previous character
        if(before != after) {
          start = before;
          before = after;
        }
        if(element != 0) add(element, start, after);
      }
    }
    return pos < elements.size();
  }

  /**
   * Adds a collation element.
   * @param element collation element
   * @param from string offset before the character of the element
   * @param to string offset after the character of the element
   */
  private void add(final int element, final int from, final int to) {
    elements.add(element);
    starts.add(from);
    ends.add(to);
  }
}
