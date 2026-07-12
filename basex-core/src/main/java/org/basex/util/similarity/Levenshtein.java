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
  /** Size of matrix (and maximum supported length for comparing tokens). */
  private static final int SIZE = 128;

  /** Default number of allowed errors; dynamic calculation if value is 0. */
  private final int maxErrors;
  /** Matrix for calculating Levenshtein distance. */
  private final byte[][] matrix;

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
    matrix = new byte[SIZE + 2][SIZE + 2];
    final int ml = matrix.length;
    for(int m = 0; m < ml; ++m) {
      matrix[0][m] = (byte) m;
      matrix[m][0] = (byte) m;
    }
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

    // use exact search for too short and too long values
    final int dlen = Math.abs(cl - tl);
    if(max == 0 && (tl < 4 || cl < 4) || tl > SIZE || cl > SIZE) {
      return dlen == 0 && Arrays.equals(tkn, cmp) ? 0 : Integer.MAX_VALUE;
    }

    // skip different tokens with too different lengths
    final int k = max == 0 ? Math.max(1, cl >> 2) : max;
    if(dlen > k) return Integer.MAX_VALUE;

    // compute distance
    final byte[][] m = matrix;
    for(int t = 0; t < tl; t++) {
      final int tn = tkn[t];
      int d = Integer.MAX_VALUE;
      for(int c = 0; c < cl; c++) {
        final int cn = cmp[c];
        int cost = min(m[t][c + 1] + 1, m[t + 1][c] + 1, m[t][c] + (tn == cn ? 0 : 1));
        if(t > 0 && c > 0 && tn == cmp[c - 1] && tkn[t - 1] == cn)
          cost = Math.min(cost, m[t - 1][c - 1] + 1);
        m[t + 1][c + 1] = (byte) cost;
        d = Math.min(d, cost);
      }
      if(d > k) return Integer.MAX_VALUE;
    }
    final int d = m[tl][cl];
    return d <= k ? d : Integer.MAX_VALUE;
  }

  /**
   * Normalizes a token and returns a codepoints array.
   * @param token token
   * @return normalized token
   */
  private static int[] normalize(final byte[] token) {
    final int[] cps = cps(token);
    final int cl = cps.length;
    for(int c = 0; c < cl; c++) cps[c] = noDiacritics(lc(cps[c]));
    return cps;
  }

  /**
   * <p>Computes the full Damerau-Levenshtein distance for two codepoint arrays and returns a
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
   * Computes the full Damerau-Levenshtein distance for two codepoint arrays. If {@code max} is
   * not negative, only the diagonal band of cells that can still yield a distance within this
   * limit is computed, and the computation is aborted as soon as the limit is exceeded.
   * @param token first codepoints array
   * @param compare second codepoints array
   * @param max maximum distance ({@code -1}: unbounded)
   * @return distance, or {@code -1} if the distance exceeds {@code max}
   */
  public static int distance(final int[] token, final int[] compare, final int max) {
    final int tl = token.length, cl = compare.length;
    // the distance is at least as large as the difference of the string lengths
    if(max >= 0 && Math.abs(tl - cl) > max) return -1;

    // width of the diagonal band; cells outside the band cannot yield a distance within the limit
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
