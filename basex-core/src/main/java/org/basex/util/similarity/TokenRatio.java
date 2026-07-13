package org.basex.util.similarity;

import java.util.*;

/**
 * Token-based similarity ratios, inspired by
 * <a href="https://github.com/seatgeek/thefuzz">TheFuzz</a>.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TokenRatio {
  /** Private constructor, preventing instantiation. */
  private TokenRatio() { }

  /**
   * Computes the token sort ratio.
   * @param tokens1 first tokens
   * @param tokens2 second tokens
   * @return similarity (0.0 - 1.0)
   */
  public static double sort(final String[] tokens1, final String[] tokens2) {
    final String[] sorted1 = tokens1.clone(), sorted2 = tokens2.clone();
    Arrays.sort(sorted1);
    Arrays.sort(sorted2);
    return ratio(String.join(" ", sorted1), String.join(" ", sorted2));
  }

  /**
   * Computes the token set ratio.
   * @param tokens1 first tokens
   * @param tokens2 second tokens
   * @return similarity (0.0 - 1.0)
   */
  public static double set(final String[] tokens1, final String[] tokens2) {
    final TreeSet<String> set1 = new TreeSet<>(Arrays.asList(tokens1));
    final TreeSet<String> set2 = new TreeSet<>(Arrays.asList(tokens2));
    if(set1.isEmpty() && set2.isEmpty()) return 1;
    if(set1.isEmpty() || set2.isEmpty()) return 0;

    // shared tokens, and tokens unique to either string (all sorted)
    final TreeSet<String> shared = new TreeSet<>(set1);
    shared.retainAll(set2);
    set1.removeAll(shared);
    set2.removeAll(shared);

    final String intersection = String.join(" ", shared);
    final String combined1 = join(shared, set1), combined2 = join(shared, set2);
    return Math.max(ratio(intersection, combined1),
        Math.max(ratio(intersection, combined2), ratio(combined1, combined2)));
  }

  /**
   * Computes the normalized Levenshtein similarity of two strings.
   * @param value1 first string
   * @param value2 second string
   * @return similarity (0.0 - 1.0)
   */
  private static double ratio(final String value1, final String value2) {
    return Levenshtein.distance(value1.codePoints().toArray(), value2.codePoints().toArray());
  }

  /**
   * Splits a string into tokens, using whitespace as separator.
   * @param cps codepoints array
   * @return tokens (without empty strings)
   */
  public static String[] tokens(final int[] cps) {
    final ArrayList<String> tokens = new ArrayList<>();
    final StringBuilder token = new StringBuilder();
    for(final int cp : cps) {
      if(Character.isWhitespace(cp)) {
        if(token.length() != 0) {
          tokens.add(token.toString());
          token.setLength(0);
        }
      } else {
        token.appendCodePoint(cp);
      }
    }
    if(token.length() != 0) tokens.add(token.toString());
    return tokens.toArray(new String[0]);
  }

  /**
   * Joins two token collections, separated by spaces.
   * @param tokens1 first tokens
   * @param tokens2 second tokens
   * @return joined string
   */
  private static String join(final Collection<String> tokens1, final Collection<String> tokens2) {
    final ArrayList<String> tokens = new ArrayList<>(tokens1);
    tokens.addAll(tokens2);
    return String.join(" ", tokens);
  }
}
