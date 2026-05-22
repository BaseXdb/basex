package org.basex.util.similarity;

import java.util.*;

/**
 * N-gram similarity, computed via the Sørensen-Dice coefficient on the sets of character
 * n-grams of two strings.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NGram {
  /** Private constructor, preventing instantiation. */
  private NGram() { }

  /**
   * Computes the n-gram similarity of two strings.
   * @param cps1 first codepoints array
   * @param cps2 second codepoints array
   * @param n n-gram length (must be positive)
   * @return similarity (0.0 - 1.0): 1.0 if the n-gram sets are equal, 0.0 if they are disjoint
   */
  public static double similarity(final int[] cps1, final int[] cps2, final int n) {
    final Set<String> grams1 = new HashSet<>(grams(cps1, n));
    final Set<String> grams2 = new HashSet<>(grams(cps2, n));
    final int size1 = grams1.size(), size2 = grams2.size();
    if(size1 == 0 && size2 == 0) return 1;
    if(size1 == 0 || size2 == 0) return 0;

    // count n-grams that occur in both sets
    final Set<String> min = size1 < size2 ? grams1 : grams2;
    final Set<String> max = size1 < size2 ? grams2 : grams1;
    int common = 0;
    for(final String gram : min) {
      if(max.contains(gram)) common++;
    }
    return 2d * common / (size1 + size2);
  }

  /**
   * Returns the character n-grams of a string.
   * @param cps codepoints array
   * @param n n-gram length (must be positive)
   * @return n-grams
   */
  public static List<String> grams(final int[] cps, final int n) {
    final int cl = cps.length;
    final List<String> grams = new ArrayList<>();
    if(cl > 0 && cl < n) {
      grams.add(new String(cps, 0, cl));
    } else {
      for(int i = 0; i <= cl - n; i++) grams.add(new String(cps, i, n));
    }
    return grams;
  }
}
