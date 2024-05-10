package org.basex.util.similarity;

import static org.basex.util.FTToken.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.function.*;

/**
 * <p>Damerau-Levenshtein algorithm. Based on the publications from Levenshtein (1965):
 * "Binary codes capable of correcting spurious insertions and deletions of ones", and
 * Damerau (1964): "A technique for computer detection and correction of spelling errors.".</p>
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Levenshtein {
  /** Size of matrix (and maximum supported length for comparing tokens). */
  private static final int MAX_LENGTH = 128;

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
    matrix = new byte[MAX_LENGTH + 2][MAX_LENGTH + 2];
    final int ml = matrix.length;
    for(int m = 0; m < ml; ++m) {
      matrix[0][m] = (byte) m;
      matrix[m][0] = (byte) m;
    }
  }

  /**
   * Returns the most similar entry.
   * @param token input token
   * @param objects objects to be compared
   * @return most similar entry or {@code null}
   */
  public static Object similar(final byte[] token, final Object[] objects) {
    return similar(token, objects, Function.identity());
  }

  /**
   * Returns the most similar entry.
   * @param token input token
   * @param objects objects to be compared
   * @param prepare function for preparing the object to be compared for the comparison
   * @return most similar entry or {@code null}
   */
  public static Object similar(final byte[] token, final Object[] objects,
      final Function<Object, Object> prepare) {

    Object similar = null;
    int err = Integer.MAX_VALUE;
    final Levenshtein ls = new Levenshtein();
    for(final Object obj : objects) {
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
    if(max == 0 && (tl < 4 || cl < 4) || tl > MAX_LENGTH || cl > MAX_LENGTH) {
      return dlen == 0 && Arrays.equals(tkn, cmp) ? 0 : Integer.MAX_VALUE;
    }

    // skip different tokens with too different lengths
    final int k = max == 0 ? Math.max(1, cl >> 2) : max;
    if(dlen > k) return Integer.MAX_VALUE;

    // compute distance
    final byte[][] m = matrix;
    for(int f = -1, g = -1, t = 0; t < tl; t++) {
      final int tn = tkn[t];
      int d = Integer.MAX_VALUE;
      for(int c = 0; c < cl; c++) {
        final int cn = cmp[c];
        int cost = min(m[t][c + 1] + 1, m[t + 1][c] + 1, m[t][c] + (tn == cn ? 0 : 1));
        if(tn == g && cn == f) cost = m[t][c];
        m[t + 1][c + 1] = (byte) cost;
        d = Math.min(d, cost);
        g = cn;
      }
      if(d > k) return Integer.MAX_VALUE;
      f = tn;
    }
    final int d = m[tl][cl];
    return d <= k ? d : Integer.MAX_VALUE;
  }

  /**
   * Normalizes a token and returns a codepoint array.
   * @param token token
   * @return normalized token
   */
  private static int[] normalize(final byte[] token) {
    final int tl = token.length;
    int cl = 0;
    for(int t = 0; t < tl; t += cl(token, t)) cl++;
    final int[] cps = new int[cl];
    for(int c = 0, t = 0; c < cl; c++, t += cl(token, t)) {
      cps[c] = noDiacritics(lc(cp(token, t)));
    }
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
    final int tl = token.length, cl = compare.length, max = Math.max(tl, cl);
    if(max == 0) return 1;
    if(Math.abs(tl - cl) >= max) return 0;

    final char[][] m = new char[tl + 1][cl + 1];
    for(int t = 0; t <= tl; t++) m[t][0] = (char) t;
    for(int c = 0; c <= cl; c++) m[0][c] = (char) c;

    for(int f = -1, g = -1, t = 0; t < tl; t++) {
      final int tn = token[t];
      for(int c = 0; c < cl; c++) {
        final int cn = compare[c];
        int cost = min(m[t][c + 1] + 1, m[t + 1][c] + 1, m[t][c] + (tn == cn ? 0 : 1));
        if(tn == g && cn == f) cost = m[t][c];
        m[t + 1][c + 1] = (char) cost;
        g = cn;
      }
      f = tn;
    }
    return (double) (max - m[tl][cl]) / max;
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
