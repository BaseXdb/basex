package org.basex.util.similarity;

/**
 * <p>Jaro-Winkler algorithm, developed by Matthew A. Jaro and William E. Winkler.</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JaroWinkler {
  /** Maximum length of the common prefix that is rewarded. */
  private static final int PREFIX = 4;
  /** Scaling factor for the common prefix. */
  private static final double SCALE = 0.1;
  /** Minimum similarity for rewarding the common prefix. */
  private static final double BOOST = 0.7;

  /** Private constructor, preventing instantiation. */
  private JaroWinkler() { }

  /**
   * Computes the Jaro Winkler distance.
   * @param cps1 first codepoints array
   * @param cps2 second codepoints array
   * @return distance (0.0 - 1.0)
   */
  public static double distance(final int[] cps1, final int[] cps2) {
    return cps1.length < cps2.length ? dst(cps1, cps2) : dst(cps2, cps1);
  }

  /**
   * Computes the distance.
   * @param min shorter codepoints array
   * @param max longer codepoints array
   * @return distance
   */
  private static double dst(final int[] min, final int[] max) {
    final int mn = min.length, mx = max.length, r = Math.max((mx >> 1) - 1, 0);
    if(mx == 0) return 1;

    final boolean[] o1 = new boolean[mn], o2 = new boolean[mx];
    int m = 0;
    for(int i = 0; i < mn; i++) {
      final int c = min[i], jl = Math.min(i + r + 1, mx);
      for(int j = Math.max(i - r, 0); j < jl; j++) {
        if(!o2[j] && c == max[j]) {
          o1[i] = o2[j] = true;
          m++;
          break;
        }
      }
    }
    if(m == 0) return 0;

    final int [] ms1 = new int[m], ms2 = new int[m];
    for(int i = 0, si = 0; i < mn; i++) {
      if(o1[i]) ms1[si++] = min[i];
    }
    for(int i = 0, si = 0; i < mx; i++) {
      if(o2[i]) ms2[si++] = max[i];
    }
    int t = 0;
    for(int i = 0; i < m; i++) {
      if(ms1[i] != ms2[i]) t++;
    }
    // common prefix, limited to 4 characters
    int p = 0;
    for(int i = 0; i < mn && i < PREFIX && min[i] == max[i]; i++) p++;

    final double d = m, j = (d / mn + d / mx + (d - (t >> 1)) / d) / 3;
    final double jw = j < BOOST ? j : j + SCALE * p * (1 - j);
    return Math.round(jw * 100) / 100d;
  }
}
