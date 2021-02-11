package org.basex.index.stats;

import static org.basex.core.Text.*;

import org.basex.util.*;

/**
 * This class assembles some index statistics.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IndexStats {
  /** Number of entries to print. */
  private final int max;
  /** Minimum occurrences. */
  private final int[] occMin;
  /** Maximum occurrences. */
  private final int[] occMax;
  /** Minimal occurring tokens. */
  private final byte[][] txtMin;
  /** Maximal occurring tokens. */
  private final byte[][] txtMax;
  /** Number of index entries. */
  private int size;

  /**
   * Default constructor.
   * @param max maximum number of index occurrences to print
   */
  public IndexStats(final int max) {
    this.max = max;
    occMin = new int[max];
    occMax = new int[max];
    txtMin = new byte[max][];
    txtMax = new byte[max][];
    for(int t = 0; t < max; ++t) {
      txtMin[t] = Token.EMPTY;
      txtMax[t] = Token.EMPTY;
      occMin[t] = Integer.MAX_VALUE;
    }
  }

  /**
   * Checks if the specified number of occurrence will be remembered.
   * @param oc number of occurrences
   * @return result of check
   */
  public boolean adding(final int oc) {
    if(oc == 0) return false;
    ++size;
    return oc > occMax[max - 1] || oc < occMin[max - 1];
  }

  /**
   * Adds the specified token.
   * @param tx token to be added
   * @param oc number of occurrences
   */
  public void add(final byte[] tx, final int oc) {
    final boolean dsc = oc > occMax[max - 1];
    final byte[][] txt = dsc ? txtMax : txtMin;
    final int[] ocs = dsc ? occMax : occMin;
    for(int a = max - 1; a >= 0; a--) {
      if(a == 0 || dsc && oc < ocs[a - 1] || !dsc && oc > ocs[a - 1]) {
        txt[a] = tx;
        ocs[a] = oc;
        break;
      }
      txt[a] = txt[a - 1];
      ocs[a] = ocs[a - 1];
    }
  }

  /**
   * Prints the list to the specified token builder.
   * @param tb token builder reference
   */
  public void print(final TokenBuilder tb) {
    tb.add(LI_ENTRIES).addInt(size).add(NL);
    int m = 0;
    int c = 0;
    for(int o = 0; o < max; ++o) {
      int tl = txtMin[o].length;
      if(tl == 0) ++c;
      else if(m < tl) m = tl;
      tl = txtMax[o].length;
      if(tl == 0) ++c;
      else if(m < tl) m = tl;
    }

    print(tb, txtMax, occMax, m + 2);
    if(c == 0) tb.add("  ").add(DOTS).add(NL);
    print(tb, txtMin, occMin, m + 2);
  }

  /**
   * Internal method for printing the list.
   * @param tb token builder reference
   * @param txt text reference
   * @param ocs occurrences
   * @param len text length
   */
  private static void print(final TokenBuilder tb, final byte[][] txt, final int[] ocs,
      final int len) {

    final int ol = ocs.length;
    for(int o = 0; o < ol; ++o) {
      final int tl = txt[o].length;
      if(tl == 0) continue;
      tb.add("  ").add(txt[o]);
      final int jl = len - tl;
      for(int j = 0; j < jl; j++) tb.add(' ');
      tb.addInt(ocs[o]).add('x').add(NL);
    }
  }
}
