package org.basex.util;

import static org.basex.util.Token.*;

/**
 * Damerau-Levenshtein implementation. Based on the publications from
 * Levenshtein (1965): Binary codes capable of correcting spurious insertions
 * and deletions of ones, and Damerau (1964): A technique for computer
 * detection and correction of spelling errors.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Levenshtein {
  /** Maximum token size. */
  private static final int MAX = 50;

  /** Default number of allowed errors; dynamic calculation if value is 0. */
  public final int error;
  /** Matrix for calculating Levenshtein distance. */
  private int[][] matrix;

  /**
   * Constructor.
   */
  public Levenshtein() {
    this(0);
  }

  /**
   * Constructor.
   * @param err allowed errors
   */
  public Levenshtein(final int err) {
    error = err;
  }

  /**
   * Compares two character arrays for similarity.
   * @param token token to be compared
   * @param sub second token to be compared
   * @return true if the arrays are similar
   */
  public boolean similar(final byte[] token, final byte[] sub) {
    return similar(token, sub, error);
  }

  /**
   * Compares two character arrays for similarity.
   * @param token token to be compared
   * @param sub second token to be compared
   * @param err number of allowed errors; dynamic calculation if value is 0
   * @return true if the arrays are similar
   */
  public boolean similar(final byte[] token, final byte[] sub, final int err) {
    int sl = 0, tl = 0;
    for(int s = 0; s < sub.length; s += cl(sub, s)) ++sl;
    for(int t = 0; t < token.length; t += cl(token, t)) ++tl;
    if(tl == 0) return false;

    // use exact search for too short and too long values
    if(sl < 4 || tl > MAX || sl > MAX) return sl == tl && same(token, sub);

    // skip different tokens with too different lengths
    final int k = err == 0 ? Math.max(1, sl >> 2) : err;
    return Math.abs(sl - tl) <= k && ls(token, tl, sub, sl, k);
  }

  /**
   * Calculates a Levenshtein distance.
   * @param tk token to be compared
   * @param tl token length
   * @param sb sub token to be compared
   * @param sl string length
   * @param k maximum number of accepted errors
   * @return true if the arrays are similar
   */
  private boolean ls(final byte[] tk, final int tl, final byte[] sb, final int sl,
      final int k) {

    int[][] m = matrix;
    if(m == null) {
      m = new int[MAX + 2][MAX + 2];
      for(int i = 0; i < m.length; ++i) {
        m[0][i] = i;
        m[i][0] = i;
      }
      matrix = m;
    }

    int e2 = -1, f2 = -1;
    for(int t = 0; t < tl; t += cl(tk, t)) {
      final int e = norm(lc(cp(tk, t)));
      int d = Integer.MAX_VALUE;
      for(int s = 0; s < sl; s += cl(sb, s)) {
        final int f = norm(lc(cp(sb, s)));
        int c = m(m[t][s + 1] + 1, m[t + 1][s] + 1, m[t][s] + (e == f ? 0 : 1));
        if(e == f2 && f == e2) c = m[t][s];
        m[t + 1][s + 1] = c;
        d = Math.min(d, c);
        f2 = f;
      }
      if(d > k) return false;
      e2 = e;
    }
    return m[tl][sl] <= k;
  }

  /**
   * Gets the minimum of three values.
   * @param a 1st value
   * @param b 2nd value
   * @param c 3rd value
   * @return minimum
   */
  private static int m(final int a, final int b, final int c) {
    final int d = a < b ? a : b;
    return d < c ? d : c;
  }

  /**
   * Compares two character arrays for equality.
   * @param tk token to be compared
   * @param sb second token to be compared
   * @return true if the arrays are equal
   */
  private static boolean same(final byte[] tk, final byte[] sb) {
    int t = 0, s = 0;
    for(; t < tk.length && s < sb.length; t += cl(tk, t), s += cl(sb, s)) {
      if(lc(norm(cp(tk, t))) != lc(norm(cp(sb, t)))) return false;
    }
    return true;
  }
}
