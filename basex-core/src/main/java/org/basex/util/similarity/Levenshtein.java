package org.basex.util.similarity;

import static org.basex.util.Token.*;
import static org.basex.util.FTToken.*;

/**
 * <p>Damerau-Levenshtein algorithm. Based on the publications from Levenshtein (1965):
 * "Binary codes capable of correcting spurious insertions and deletions of ones", and
 * Damerau (1964): "A technique for computer detection and correction of spelling errors.".</p>
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class Levenshtein {
  /** Maximum token size. */
  private static final int MAX = 50;

  /** Default number of allowed errors; dynamic calculation if value is 0. */
  private final int error;
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
   * @param error allowed errors
   */
  public Levenshtein(final int error) {
    this.error = error;
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
    final int sl = sub.length, tl = token.length;
    int slen = 0, tlen = 0;
    for(int s = 0; s < sl; s += cl(sub, s)) ++slen;
    for(int t = 0; t < tl; t += cl(token, t)) ++tlen;
    if(tlen == 0) return false;

    // use exact search for too short and too long values
    if(err == 0 && slen < 4 || tlen > MAX || slen > MAX) return slen == tlen && same(token, sub);

    // skip different tokens with too different lengths
    final int k = err == 0 ? Math.max(1, slen >> 2) : err;
    return Math.abs(slen - tlen) <= k && ls(token, tlen, sub, slen, k);
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
  private boolean ls(final byte[] tk, final int tl, final byte[] sb, final int sl, final int k) {
    int[][] mx = matrix;
    if(mx == null) {
      mx = new int[MAX + 2][MAX + 2];
      final int ml = mx.length;
      for(int m = 0; m < ml; ++m) {
        mx[0][m] = m;
        mx[m][0] = m;
      }
      matrix = mx;
    }

    int e2 = -1, f2 = -1;
    for(int t = 0; t < tl; t += cl(tk, t)) {
      final int e = noDiacritics(lc(cp(tk, t)));
      int d = Integer.MAX_VALUE;
      for(int s = 0; s < sl; s += cl(sb, s)) {
        final int f = noDiacritics(lc(cp(sb, s)));
        int c = m(mx[t][s + 1] + 1, mx[t + 1][s] + 1, mx[t][s] + (e == f ? 0 : 1));
        if(e == f2 && f == e2) c = mx[t][s];
        mx[t + 1][s + 1] = c;
        d = Math.min(d, c);
        f2 = f;
      }
      if(d > k) return false;
      e2 = e;
    }
    return mx[tl][sl] <= k;
  }

  /**
   * <p>Computes the full Damerau-Levenshtein distance for two codepoint arrays and returns a
   * double value (0.0 - 1.0), which represents the distance. The value is computed as follows:</p>
   *
   * <pre>  1.0 - distance / max(length of strings)</pre>
   *
   * <p>1.0 is returned if the strings are equal; 0.0 is returned if all strings are
   * completely different.</p>
   *
   * @param cps1 first array
   * @param cps2 second array
   * @return distance (0.0 - 1.0)
   */
  public static double distance(final int[] cps1, final int[] cps2) {
    final int l1 = cps1.length, l2 = cps2.length, lMax = Math.max(l1, l2);
    if(lMax == 0) return 1;

    final int[][] m = new int[lMax + 1][lMax + 1];
    for(int f2 = -1, f1 = -1, p1 = 0; p1 < lMax; p1++) {
      final int c1 = p1 < l1 ? cps1[p1] : 0;
      for(int p2 = 0; p2 < lMax; p2++) {
        final int c2 = p2 < l2 ? cps2[p2] : 0;
        int c = m(m[p1][p2 + 1] + 1, m[p1 + 1][p2] + 1, m[p1][p2] + (c1 == c2 ? 0 : 1));
        if(c1 == f1 && c2 == f2) c = m[p1][p2];
        m[p1 + 1][p2 + 1] = c;
        f1 = c2;
      }
      f2 = c1;
    }
    return (double) (lMax - m[lMax][lMax]) / lMax;
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
    final int tl = tk.length, sl = sb.length;
    for(int s = 0, t = 0; t < tl && s < sl; t += cl(tk, t), s += cl(sb, s)) {
      if(lc(noDiacritics(cp(tk, t))) != lc(noDiacritics(cp(sb, t)))) return false;
    }
    return true;
  }
}
