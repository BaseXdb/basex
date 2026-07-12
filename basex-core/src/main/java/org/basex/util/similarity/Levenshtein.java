package org.basex.util.similarity;

import static org.basex.util.FTToken.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.function.*;

/**
 * <p>Optimal string alignment. Based on the publications from Levenshtein (1965): "Binary codes
 * capable of correcting spurious insertions and deletions of ones", and Damerau (1964):
 * "A technique for computer detection and correction of spelling errors.".</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Levenshtein {
  /** Maximum size for compared strings. */
  public static final int MAX_LENGTH = 10000;

  /** Default number of allowed errors; dynamic calculation if value is 0. */
  private final int maxErrors;

  /**
   * Constructor.
   */
  public Levenshtein() {
    this(0);
  }

  /**
   * Constructor.
   * @param maxErrors maximum number of allowed errors
   */
  public Levenshtein(final int maxErrors) {
    this.maxErrors = maxErrors;
  }

  /**
   * Returns the most similar entry.
   * @param <T> element type
   * @param token input token
   * @param objects objects to be compared
   * @return most similar entry or {@code null}
   */
  public static <T> T similar(final byte[] token, final T[] objects) {
    return similar(token, objects, Function.identity());
  }

  /**
   * Returns the most similar entry.
   * @param <T> element type
   * @param token input token
   * @param objects objects to be compared
   * @param prepare function for preparing the object to be compared for the comparison
   * @return most similar entry or {@code null}
   */
  public static <T> T similar(final byte[] token, final T[] objects,
      final Function<? super T, ?> prepare) {

    T similar = null;
    int err = Integer.MAX_VALUE;
    final Levenshtein ls = new Levenshtein();
    for(final T obj : objects) {
      final byte[] compare = token(prepare.apply(obj));
      if(compare != null) {
        final int d = ls.distance(token, compare, 0);
        if(d < err) {
          similar = obj;
          err = d;
        }
      }
    }
    return err != Integer.MAX_VALUE ? similar : null;
  }

  /**
   * Returns the most similar entry; falls back to the first entry whose prepared name starts
   * with the input if no similar entry is found.
   * @param <T> element type
   * @param token input token
   * @param objects objects to be compared
   * @return most similar or prefix-matching entry, or {@code null}
   */
  public static <T> T similarOrPrefix(final byte[] token, final T[] objects) {
    return similarOrPrefix(token, objects, Function.identity());
  }

  /**
   * Returns the most similar entry; falls back to the first entry whose prepared name starts
   * with the input if no similar entry is found.
   * @param <T> element type
   * @param token input token
   * @param objects objects to be compared
   * @param prepare function for preparing the object to be compared for the comparison
   * @return most similar or prefix-matching entry, or {@code null}
   */
  public static <T> T similarOrPrefix(final byte[] token, final T[] objects,
      final Function<? super T, ?> prepare) {

    final T similar = similar(token, objects, prepare);
    if(similar != null) return similar;
    final byte[] lc = lc(token);
    for(final T obj : objects) {
      final byte[] compare = token(prepare.apply(obj));
      if(compare != null && startsWith(lc(compare), lc)) return obj;
    }
    return null;
  }

  /**
   * Compares two tokens for similarity.
   * @param token input token
   * @param compare token to be compared
   * @return true if the arrays are similar
   */
  public boolean similar(final byte[] token, final byte[] compare) {
    return similar(token, compare, maxErrors);
  }

  /**
   * Compares two tokens for similarity.
   * @param token input token
   * @param compare token to be compared
   * @param max number of allowed errors; dynamic calculation if value is {@code 0}
   * @return true if the arrays are similar
   */
  public boolean similar(final byte[] token, final byte[] compare, final int max) {
    return distance(token, compare, max) != Integer.MAX_VALUE;
  }

  /**
   * Computes the Levenshtein distance.
   * @param token original token
   * @param compare token to be compared
   * @param max maximum number of allowed errors; dynamic calculation if value is {@code 0}
   * @return distance
   */
  private int distance(final byte[] token, final byte[] compare, final int max) {
    // create normalized copies of the tokens
    final int[] tkn = normalize(token), cmp = normalize(compare);
    final int tl = tkn.length, cl = cmp.length;

    // use exact search for too short values
    if(max == 0 && (tl < 4 || cl < 4)) {
      return tl == cl && Arrays.equals(tkn, cmp) ? 0 : Integer.MAX_VALUE;
    }

    final int k = max == 0 ? Math.max(1, cl >> 2) : max;
    final int dist = distance(tkn, cmp, k);
    return dist == -1 ? Integer.MAX_VALUE : dist;
  }

  /**
   * Normalizes a token and returns a codepoints array.
   * @param token token
   * @return normalized token
   */
  private static int[] normalize(final byte[] token) {
    // fold before lowercasing: expansions are case-preserving (ß -> ss, ẞ -> SS)
    final int[] cps = cps(noDiacritics(token));
    final int cl = cps.length;
    for(int c = 0; c < cl; c++) cps[c] = lc(cps[c]);
    return cps;
  }

  /**
   * <p>Computes the edit distance for two codepoint arrays and returns a
   * double value (0.0 - 1.0), which represents the distance. The value is computed as follows:</p>
   *
   * <code>1.0 - distance / max(length of strings)</code>.
   * 1.0 is returned if the strings are equal
   * 0.0 is returned if the strings are different enough.
   *
   * @param token first codepoints array
   * @param compare second codepoints array
   * @return distance (0.0 - 1.0)
   */
  public static double distance(final int[] token, final int[] compare) {
    final int max = Math.max(token.length, compare.length);
    return max == 0 ? 1 : (double) (max - distance(token, compare, -1)) / max;
  }

  /**
   * Computes the edit distance for two codepoint arrays. If {@code max} is not negative, only the
   * diagonal band of cells that can yield a distance within this limit is computed.
   * @param token first codepoints array
   * @param compare second codepoints array
   * @param max maximum distance ({@code -1}: unbounded)
   * @return distance, or {@code -1} if the distance exceeds {@code max}
   */
  public static int distance(final int[] token, final int[] compare, final int max) {
    final int tl = token.length, cl = compare.length;
    if(max >= 0 && Math.abs(tl - cl) > max) return -1;

    final int band = max >= 0 ? Math.min(max, Math.max(tl, cl)) : Math.max(tl, cl);
    final int inf = Integer.MAX_VALUE / 2;

    // a transposition refers to the second last row
    int[] prev2 = new int[cl + 2], prev = new int[cl + 2], curr = new int[cl + 2];
    Arrays.fill(prev2, inf);
    for(int c = 0; c <= cl + 1; c++) prev[c] = c <= band ? c : inf;

    for(int t = 0; t < tl; t++) {
      final int tn = token[t];
      final int lo = Math.max(0, t - band), hi = Math.min(cl - 1, t + band);
      curr[lo] = lo == 0 ? t + 1 : inf;

      int rowMin = curr[lo];
      for(int c = lo; c <= hi; c++) {
        final int cn = compare[c];
        int cost = min(prev[c + 1] + 1, curr[c] + 1, prev[c] + (tn == cn ? 0 : 1));
        if(t > 0 && c > 0 && tn == compare[c - 1] && token[t - 1] == cn)
          cost = Math.min(cost, prev2[c - 1] + 1);
        curr[c + 1] = cost;
        rowMin = Math.min(rowMin, cost);
      }
      // invalidate the cell after the band; it must not be read as a stale value
      if(hi + 2 <= cl + 1) curr[hi + 2] = inf;

      if(max >= 0 && rowMin > max) return -1;
      final int[] tmp = prev2;
      prev2 = prev;
      prev = curr;
      curr = tmp;
    }
    final int dist = prev[cl];
    return max >= 0 && dist > max ? -1 : dist;
  }

  /**
   * Compares the shorter of two codepoint arrays with the best matching substring of the longer
   * one and returns a double value (0.0 - 1.0): {@code 1.0 - distance / length of shorter array}.
   * @param cps1 first codepoints array
   * @param cps2 second codepoints array
   * @return similarity (0.0 - 1.0)
   */
  public static double partial(final int[] cps1, final int[] cps2) {
    // strings of equal length: the best matching substring is the string itself
    if(cps1.length == cps2.length) return distance(cps1, cps2);

    final boolean swap = cps1.length > cps2.length;
    final int[] pattern = swap ? cps2 : cps1, text = swap ? cps1 : cps2;
    final int pl = pattern.length, tl = text.length;
    if(pl == 0) return 0;

    // the first row is zero: the pattern may start at any position of the text
    int[] prev2 = new int[tl + 1], prev = new int[tl + 1], curr = new int[tl + 1];
    for(int p = 1; p <= pl; p++) {
      curr[0] = p;
      for(int t = 1; t <= tl; t++) {
        final int cost = pattern[p - 1] == text[t - 1] ? 0 : 1;
        int cst = min(prev[t] + 1, curr[t - 1] + 1, prev[t - 1] + cost);
        if(p > 1 && t > 1 && pattern[p - 1] == text[t - 2] && pattern[p - 2] == text[t - 1])
          cst = Math.min(cst, prev2[t - 2] + 1);
        curr[t] = cst;
      }
      final int[] tmp = prev2;
      prev2 = prev;
      prev = curr;
      curr = tmp;
    }

    // the pattern may end at any position of the text
    int dist = pl;
    for(int t = 0; t <= tl; t++) dist = Math.min(dist, prev[t]);
    return (double) (pl - dist) / pl;
  }

  /**
   * Gets the minimum of three values.
   * @param a 1st value
   * @param b 2nd value
   * @param c 3rd value
   * @return minimum
   */
  private static int min(final int a, final int b, final int c) {
    return Math.min(Math.min(a, b), c);
  }
}
