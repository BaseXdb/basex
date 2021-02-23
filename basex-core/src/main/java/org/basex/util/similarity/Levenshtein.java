package org.basex.util.similarity;

import static org.basex.util.FTToken.*;
import static org.basex.util.Token.*;

import java.util.function.*;

/**
 * <p>Damerau-Levenshtein algorithm. Based on the publications from Levenshtein (1965):
 * "Binary codes capable of correcting spurious insertions and deletions of ones", and
 * Damerau (1964): "A technique for computer detection and correction of spelling errors.".</p>
 *
 * @author BaseX Team 2005-21, BSD License
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
   * Returns the most similar entry.
   * @param token token to be compared
   * @param objects objects to be compared
   * @return most similar token or {@code null}
   */
  public static Object similar(final byte[] token, final Object[] objects) {
    return similar(token, objects, Function.identity());
  }

  /**
   * Returns the most similar entry.
   * @param token token to be compared
   * @param objects objects to be compared
   * @param prepare function for preparing the object to be compared for the comparison
   * @return most similar token or {@code null}
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
   * @param token token to be compared
   * @param compare second token to be compared
   * @return true if the arrays are similar
   */
  public boolean similar(final byte[] token, final byte[] compare) {
    return similar(token, compare, error);
  }

  /**
   * Compares two tokens for similarity.
   * @param token token to be compared
   * @param compare second token to be compared
   * @param err number of allowed errors; dynamic calculation if value is 0
   * @return true if the arrays are similar
   */
  public boolean similar(final byte[] token, final byte[] compare, final int err) {
    return distance(token, compare, err) != Integer.MAX_VALUE;
  }

  /**
   * Computes the Levenshtein distance.
   * @param token original token
   * @param compare token to be compared
   * @param err number of allowed errors; dynamic calculation if value is 0
   * @return distance
   */
  private int distance(final byte[] token, final byte[] compare, final int err) {
    final int sl = compare.length, tl = token.length;
    int clen = 0, tlen = 0;
    for(int c = 0; c < sl; c += cl(compare, c)) ++clen;
    for(int t = 0; t < tl; t += cl(token, t)) ++tlen;

    // use exact search for too short and too long values
    final int dlen = Math.abs(clen - tlen);
    if(err == 0 && (tlen < 4 || clen < 4) || tlen > MAX || clen > MAX)
      return dlen == 0 && same(token, compare) ? 0 : Integer.MAX_VALUE;

    // skip different tokens with too different lengths
    final int k = err == 0 ? Math.max(1, clen >> 2) : err;
    if(dlen > k) return Integer.MAX_VALUE;

    // compute distance
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

    int f = -1, g = -1;
    for(int t = 0; t < tlen; t += cl(token, t)) {
      final int tn = noDiacritics(lc(cp(token, t)));
      int d = Integer.MAX_VALUE;
      for(int c = 0; c < clen; c += cl(compare, c)) {
        final int cn = noDiacritics(lc(cp(compare, c)));
        int e = m(mx[t][c + 1] + 1, mx[t + 1][c] + 1, mx[t][c] + (tn == cn ? 0 : 1));
        if(tn == g && cn == f) e = mx[t][c];
        mx[t + 1][c + 1] = e;
        d = Math.min(d, e);
        g = cn;
      }
      if(d > k) return Integer.MAX_VALUE;
      f = tn;
    }
    final int d = mx[tlen][clen];
    return d <= k ? d : Integer.MAX_VALUE;
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
    return Math.min(Math.min(a, b), c);
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
