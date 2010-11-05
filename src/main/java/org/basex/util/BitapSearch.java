package org.basex.util;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Generalized search algorithm based on the Bitap string matching algorithm.
 * The implementation is based on the implementation in Wikipedia, but uses
 * {@ling BitSet} for fast bit operation.
 * @param <T> type of elements to compare
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 * @see <a href="http://en.wikipedia.org/wiki/Bitap_algorithm"
 *      >http://en.wikipedia.org/wiki/Bitap_algorithm</a>
 */
public final class BitapSearch<T> {
  /** Iterator over the set of elements being searched. */
  private final Iterator<T> haystack;
  /** Subset of elements being searched for. */
  private final T[] needle;
  /** Comparator used for comparing two elements for equality. */
  private final Comparator<T> cmp;
  /** Bit mask, showing which elements from {@link #needle} are equal to the
   * current element of {@link #haystack}. */
  private final BitSet mask;
  /** Is the method {@link #hasNext} already called? */
  private boolean next;
  /** The current position in the {@link #haystack} iterator; first is 0. */
  private int pos;

  /**
   * Constructor.
   * @param h iterator over the set of elements being searched
   * @param n array of elements being searched for
   * @param c comparator for comparing two elements for equality
   */
  public BitapSearch(final Iterator<T> h, final T[] n, final Comparator<T> c) {
    haystack = h;
    needle = n;
    cmp = c;
    if(needle.length == 0) {
      mask = null;
    } else {
      mask = new BitSet(needle.length + 1);
      mask.set(0);
    }
  }

  /**
   * Is there one more match?
   * @return {@code true} if yes
   */
  public boolean hasNext() {
    if(mask == null) return false;
    if(next) return pos >= 0;

    // find next hit:
    next = true;

    while(haystack.hasNext()) {
      final T current = haystack.next();
      pos++;
      for(int k = needle.length; k >= 1; k--)
        mask.set(k, mask.get(k - 1)
            && cmp.compare(current, needle[k - 1]) == 0);
      if(mask.get(needle.length)) return true;
    }

    pos = -1;
    return false;
  }

  /**
   * Position in the haystack of the next match.
   * @return start position of the match; first position is 0
   */
  public int next() {
    if(hasNext()) {
      next = false;
      return pos - needle.length;
    }
    throw new NoSuchElementException();
  }
}
