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
   * @param cps1 first codepoints array
   * @param cps2 second codepoints array
   * @return similarity (0.0 - 1.0)
   */
  public static double sort(final int[] cps1, final int[] cps2) {
    final String[] tokens1 = tokens(cps1), tokens2 = tokens(cps2);
    Arrays.sort(tokens1);
    Arrays.sort(tokens2);
    return ratio(String.join(" ", tokens1), String.join(" ", tokens2));
  }

  /**
   * Computes the token set ratio.
   * @param cps1 first codepoints array
   * @param cps2 second codepoints array
   * @return similarity (0.0 - 1.0)
   */
  public static double set(final int[] cps1, final int[] cps2) {
    final TreeSet<String> set1 = new TreeSet<>(Arrays.asList(tokens(cps1)));
    final TreeSet<String> set2 = new TreeSet<>(Arrays.asList(tokens(cps2)));
    if(set1.isEmpty() && set2.isEmpty()) return 1;
    if(set1.isEmpty() || set2.isEmpty()) return 0;

    // shared tokens, and tokens unique to either string (all sorted)
    final TreeSet<String> shared = new TreeSet<>(set1);
    shared.retainAll(set2);
    final TreeSet<String> rest1 = new TreeSet<>(set1);
    rest1.removeAll(shared);
    final TreeSet<String> rest2 = new TreeSet<>(set2);
    rest2.removeAll(shared);

    final String intersection = String.join(" ", shared);
    final String combined1 = join(shared, rest1), combined2 = join(shared, rest2);
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
  private static String[] tokens(final int[] cps) {
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
    final StringBuilder sb = new StringBuilder();
    for(final String token : tokens1) {
      if(sb.length() != 0) sb.append(' ');
      sb.append(token);
    }
    for(final String token : tokens2) {
      if(sb.length() != 0) sb.append(' ');
      sb.append(token);
    }
    return sb.toString();
  }
}
