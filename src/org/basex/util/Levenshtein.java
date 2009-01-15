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
  private int[][] m = new int[32][32];

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
   * @param tok token to be compared
   * @param sub second token to be compared
   * @return true if the arrays are similar
   */
  public boolean similar(final byte[] tok, final byte[] sub) {
    // use exact search for too short and too long values
    final int sl = sub.length;
    // small word - use exact search
    if(sl < 4) return Token.eq(tok, sub);
    // large word - search for substrings
    if(sl > 50) return contains(tok, sub);

    return ls(tok, 0, tok.length, sub);
  }

  /**
   * Calculates a Levenshtein distance.
   * @param tok token to be compared
   * @param ts start position in token
   * @param tl token length to be checked
   * @param sub sub token to be compared
   * @return true if the arrays are similar
   */
  private boolean ls(final byte[] tok, final int ts, final int tl,
      final byte[] sub) {

    if(tl == 0) return false;
    final int sl = sub.length;

    // use exact search for too short and too long values
    if(sl < 4 || tl > 50 || sl > 50) return equals(tok, ts, tl, sub);

    // skip different tokens with too different lengths
    int k = Prop.lserr;
    if(k == 0) k = Math.max(1, sl >> 2);
    if(Math.abs(sl - tl) > k) return false;

    return ls(tok, ts, tl, sub, k) <= k;
  }

  /**
   * Calculates a Levenshtein distance.
   * @param tok token to be compared
   * @param ts start position in token
   * @param tl token length to be checked
   * @param sub sub token to be compared
   * @param k maximum number of accepted errors
   * @return true if the arrays are similar
   */
  public int ls(final byte[] tok, final int ts, final int tl,
      final byte[] sub, final int k) {

    int e2 = -1, f2 = -1;
    final int sl = sub.length;
    for(int t = 0; t < tl; t++) {
      final int e = ftNorm(tok[ts + t]);
      int d = 32;
      for(int q = 0; q < sl; q++) {
        final int f = ftNorm(sub[q]);
        int c = min(m[t][q + 1] + 1, m[t + 1][q] + 1,
            m[t][q] + (e == f ? 0 : 1));
        if(e == f2 && f == e2) c = m[t][q];
        m[t + 1][q + 1] = c;
        d = Math.min(d, c);
        f2 = f;
      }
      if(d > k) return Integer.MAX_VALUE;
      e2 = e;
    }
    return m[tl][sl];
  }

  /**
   * Gets the minimum of three values.
   * @param a 1st value
   * @param b 2nd value
   * @param c 3rd value
   * @return minimum
   */
  private int min(final int a, final int b, final int c) {
    final int d = a < b ? a : b;
    return d < c ? d : c;
  }

  /**
   * Checks if the first token approximately contains the second fulltext term.
   * @param tok first token
   * @param sub second token
   * @return result of test
   */
  public boolean contains(final byte[] tok, final byte[] sub) {
    final int tl = tok.length;
    final int sl = sub.length;
    if(sl == 0) return false;

    // compare tokens character wise
    int ts = 0;
    int t = -1;
    while(++t < tl) {
      if(ftChar(tok[t])) continue;
      if(ls(tok, ts, t - ts, sub)) return true;
      while(++t < tl && !ftChar(tok[t]));
      ts = t;
    }
    return ts == tl ? false : ls(tok, ts, t - ts, sub);
  }

  /**
   * Compares two character arrays for equality.
   * @param tok token to be compared
   * @param ts start of token
   * @param tl length of token
   * @param sub second token to be compared
   * @return true if the arrays are equal
   */
  private boolean equals(final byte[] tok, final int ts, final int tl,
      final byte[] sub) {
    if(tl != sub.length) return false;
    for(int t = 0; t < tl; t++) {
      if(ftNorm(tok[ts + t]) != ftNorm(sub[t])) return false;
    }
    return true;
  }

  /**
   * Checks if the specified character is a letter; special characters are
   * converted to the standard ASCII charset.
   * Note that this method does not support unicode characters.
   * @param ch character to be converted
   * @return converted character
   */
  public boolean ftChar(final byte ch) {
    return Token.letterOrDigit(ft(ch));
  }

  /**
   * Returns a lowercase ASCII character of the specified fulltext character.
   * Note that this method does not support unicode characters.
   * @param ch character to be converted
   * @return converted character
   */
  public int ftNorm(final int ch) {
    return Token.lc(ft(ch));
  }

  /**
   * Returns a fulltext character of the specified character.
   * Note that this method does not support unicode characters.
   * @param ch character to be converted
   * @return converted character
   */
  private int ft(final int ch) {
    return ch < 0 && ch > -64 ? Token.NORM[ch + 64] : ch;
  }
}
