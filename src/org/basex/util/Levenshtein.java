package org.basex.util;

import org.basex.core.Prop;

/**
 * This class assembles methods for fuzzy token matching.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Levenshtein {
  /** Static matrix for Levenshtein distance. */
  private int[][] m = new int[52][52];

  /**
   * Constructor.
   */
  public Levenshtein() {
    for(int i = 0; i < m.length; i++) {
      m[0][i] = i;
      m[i][0] = i;
    }
  }

  /**
   * Compares two character arrays for similarity.
   * @param token token to be compared
   * @param sub second token to be compared
   * @return true if the arrays are similar
   */
  public boolean similar(final byte[] token, final byte[] sub) {
    int tl = token.length;
    if(tl == 0) return false;
    final int sl = sub.length;

    // use exact search for too short and too long values
    if(sl < 4 || tl > 50 || sl > 50) return eq(token, tl, sub);

    // skip different tokens with too different lengths
    int k = Prop.lserr;
    if(k == 0) k = Math.max(1, sl >> 2);
    return Math.abs(sl - tl) <= k && ls(token, tl, sub, k);
  }

  /**
   * Calculates a Levenshtein distance.
   * @param tk token to be compared
   * @param tl token length to be checked
   * @param sb sub token to be compared
   * @param k maximum number of accepted errors
   * @return true if the arrays are similar
   */
  private boolean ls(final byte[] tk, final int tl, final byte[] sb,
      final int k) {
    int e2 = -1, f2 = -1;
    final int sl = sb.length;
    for(int t = 0; t < tl; t++) {
      final int e = Token.norm(tk[t]);
      int d = Integer.MAX_VALUE;
      for(int q = 0; q < sl; q++) {
        final int f = Token.norm(sb[q]);
        int c = m(m[t][q + 1] + 1, m[t + 1][q] + 1, m[t][q] + (e == f ? 0 : 1));
        if(e == f2 && f == e2) c = m[t][q];
        m[t + 1][q + 1] = c;
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
  private int m(final int a, final int b, final int c) {
    final int d = a < b ? a : b;
    return d < c ? d : c;
  }

  /**
   * Compares two character arrays for equality.
   * @param tk token to be compared
   * @param tl length of token
   * @param sb second token to be compared
   * @return true if the arrays are equal
   */
  private boolean eq(final byte[] tk, final int tl, final byte[] sb) {
    if(tl != sb.length) return false;
    for(int t = 0; t < tl; t++) {
      if(Token.norm(tk[t]) != Token.norm(sb[t])) return false;
    }
    return true;
  }
}
