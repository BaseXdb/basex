package org.basex.util.ft;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ft.*;
import org.basex.util.list.*;

/**
 * Generalized search algorithm based on the Bitap string matching algorithm.
 * The implementation is based on the implementation in Wikipedia, but uses
 * {@link BitSet} for fast bit operation. This version works with a set of
 * needles and each one of it can be matched in the haystack.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 * @see <a href="http://en.wikipedia.org/wiki/Bitap_algorithm"
 *      >http://en.wikipedia.org/wiki/Bitap_algorithm</a>
 */
public final class FTBitapSearch {
  /** Iterator over the set of elements being searched. */
  private final FTIterator haystack;
  /** Subset of elements being searched for. */
  private final FTTokens needles;
  /** Comparator used for comparing two elements for equality. */
  private final TokenComparator cmp;
  /** Bit masks, showing which elements from a {@link #needles} are equal to
   * the current element of {@link #haystack}. */
  private final BitSet[] masks;
  /** Needle indexes in {@link #needles} sorted by the length of the needle. */
  private final int[] sorted;
  /** Is the method {@link #hasNext} already called? */
  private boolean next;
  /** The current position in the {@link #haystack} iterator; first is 0. */
  private int pos;
  /** Index of the needle which is matched. */
  private int match;

  /**
   * Constructor.
   * @param h iterator over the set of elements being searched ("haystack")
   * @param n a list of "needles" (a needle is an array of elements being
   *          searched for)
   * @param c comparator for comparing two elements for equality
   */
  public FTBitapSearch(final FTIterator h, final FTTokens n, final TokenComparator c) {
    haystack = h;
    cmp = c;
    needles = n;
    sorted = new int[n.size()];

    // skip empty needles:
    int count = -1;
    for(int i = 0; i < sorted.length; i++) {
      if(n.get(i) != null && n.get(i).size() > 0) sorted[++count] = i;
    }

    masks = new BitSet[++count];
    // sort the needles by length (longest first):
    for(int i = 0; i < count; i++) {
      for(int j = i; j > 0
          && n.get(sorted[j]).size() > n.get(sorted[j - 1]).size(); j--) {
        final int t = sorted[j];
        sorted[j] = sorted[j - 1];
        sorted[j - 1] = t;
      }
      // initialize the bit masks, too:
      masks[i] = new BitSet();
      masks[i].set(0);
    }
  }

  /**
   * Is there one more match?
   * @return {@code true} if yes
   * @throws QueryException if an error occurs during search
   */
  public boolean hasNext() throws QueryException {
    if(masks.length == 0) return false;
    if(next) return pos >= 0;

    // find next hit:
    next = true;

    while(haystack.hasNext()) {
      final byte[] current = haystack.nextToken();
      ++pos;

      // check each needle for a match:
      boolean matched = false;
      for(int i = 0; i < masks.length; i++) {
        final int id = sorted[i];
        final TokenList n = needles.get(id);
        final BitSet m = masks[id];
        // compare each element from the needle and set the corresponding bit:
        for(int k = n.size(); k >= 1; k--)
          m.set(k, m.get(k - 1) && cmp.equal(current, n.get(k - 1)));
        // if the last element of the needle's mask is true, then all elements
        // of the needle are matched:
        if(m.get(n.size()) && !matched) {
          match = id;
          matched = true;
        }
      }
      if(matched) return true;
    }

    // nothing was found and the whole haystack was checked:
    pos = -1;
    return false;
  }

  /**
   * Position in the haystack of the next match.
   * @return start position of the match; first position is 0
   * @throws QueryException if an error occurs during search
   */
  public int next() throws QueryException {
    if(hasNext()) {
      next = false;
      return pos - needles.get(match).size();
    }
    throw new NoSuchElementException();
  }

  /**
   * Token comparator.
   *
   * @author BaseX Team 2005-13, BSD License
   * @author Dimitar Popov
   */
  public interface TokenComparator {
    /**
     * Check if two tokens are equal.
     * @param t1 first token
     * @param t2 second token
     * @return {@code true} if the two are equal
     * @throws QueryException if an error occurs during comparison
     */
    boolean equal(final byte[] t1, final byte[] t2) throws QueryException;
  }
}
