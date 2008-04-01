package org.basex.util;

import static org.basex.util.Token.*;

/**
 * This class assembles methods for fuzzy token matching.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Levenshtein {
  /** Matrix for Levenshtein distance. */
  private static int[][] m;

  /** Private constructor, preventing class instantiation. */
  private Levenshtein() { }
  
  /**
   * Compares two character arrays for similarity.
   * @param tok token to be compared
   * @param sub second token to be compared
   * @return true if the arrays are similar
   */
  public static boolean similar(final byte[] tok, final byte[] sub) {
    // use exact search for too short and too long values
    final int sl = sub.length;
    // ... lower-/uppercase could be checked as well
    return sl < 3 ? eq(tok, sub) : sl > 30 ? contains(tok, sub) :
      ls(tok, 0, tok.length, sub);
  }
  
  /**
   * Calculates a Levenshtein distance.
   * @param tok token to be compared
   * @param ts start position in token
   * @param tl token length to be checked
   * @param sub sub token to be compared
   * @return true if the arrays are similar
   */
  private static boolean ls(final byte[] tok, final int ts, final int tl, 
      final byte[] sub) {

    if(tl == 0) return false;
    final int sl = sub.length;

    // use exact search for too short and too long values
    if(sl < 3 || tl > 30 || sl > 30) return equals(tok, ts, tl, sub);

    // skip different tokens with too different lengths
    final int qr = 80 / Math.max(1, (sl - 1) >> 2);
    if(Math.abs(sl - tl) * qr > 100) return false;

    if(m == null) {
      m = new int[32][32];
      for(int i = 0; i < m.length; i++) { m[0][i] = i; m[i][0] = i; }
    }
    
    int e2 = -1, f2 = -1;
    for(int t = 0; t < tl; t++) {
      final int e = ftNorm(tok[ts + t]);
      int d = 64;
      for(int q = 0; q < sl; q++) {
        final int f = ftNorm(sub[q]);
        int c = min(m[t][q + 1] + 1, m[t + 1][q] + 1,
            m[t][q] + (e == f ? 0 : 1));
        if(e == f2 && f == e2) c = m[t][q];
        m[t + 1][q + 1] = c;
        d = Math.min(d, c);
        f2 = f;
      }
      if(qr * d > 100) return false;
      e2 = e;
    }
    return qr * m[tl][sl] < 100;
  }

  /**
   * Calculates a Levenshtein distance and returns it.
   * @param tok token to be compared
   * @param ts start position in token
   * @param tl token length to be checked
   * @param sub sub token to be compared
   * @param er maximum accepted error
   * @return int levenshtein distance 
   */
  public static int levenshtein(final byte[] tok, final int ts, final int tl, 
      final byte[] sub, final int er) {

    if(tl == 0) return 0;
    final int sl = sub.length;

    //final int qr = 80 / Math.max(1, (sl - 1) >> 2);

    if(m == null) {
      m = new int[32][32];
      for(int i = 0; i < m.length; i++) { m[0][i] = i; m[i][0] = i; }
    }
    
    int e2 = -1, f2 = -1;
    for(int t = 0; t < tl; t++) {
      final int e = ftNorm(tok[ts + t]);
      int d = 64;
      for(int q = 0; q < sl; q++) {
        final int f = ftNorm(sub[q]);
        int c = min(m[t][q + 1] + 1, m[t + 1][q] + 1,
            m[t][q] + (e == f ? 0 : 1));
        if(e == f2 && f == e2) c = m[t][q];
        m[t + 1][q + 1] = c;
        d = Math.min(d, c);
        f2 = f;
        // stop levenshtein if max. error occured
        if (t == q && m[t + 1][q + 1] == er + 1) return er + 1; 
      }
      //if(qr * d > 100) return tl;
      e2 = e;
    }
    //return qr * m[tl][sl];
    return m[tl][sl];
  }

  
  
  /**
   * Gets the minimum of three values.
   * @param a 1st value
   * @param b 2nd value
   * @param c 3rd value
   * @return minimum
   */
  private static int min(final int a, final int b, final int c) {
    final int d = a < b ? a : b;
    return d < c ? d : c;
  }
  
  /**
   * Checks if the first token approximately contains the second fulltext term.
   * @param tok first token
   * @param sub second token
   * @return result of test
   */
  public static boolean contains(final byte[] tok, final byte[] sub) {
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
  private static boolean equals(final byte[] tok, final int ts, final int tl,
      final byte[] sub) {
    if(tl != sub.length) return false;
    for(int t = 0; t < tl; t++) {
      if(ftNorm(tok[ts + t]) != ftNorm(sub[t])) return false;
    }
    return true;
  }
}
