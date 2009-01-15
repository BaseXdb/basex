package org.basex.index;

import static org.basex.Text.*;
import org.basex.core.Prop;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class assembles some index statistics.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IndexStats {
  /** Minimum occurrences. */
  private int[] occMin;
  /** Maximum occurrences. */
  private int[] occMax;
  /** Minimal occurring tokens. */
  private byte[][] txtMin;
  /** Maximal occurring tokens. */
  private byte[][] txtMax;
  /** Number of entries to print. */
  private int nr;
  /** Number of index entries. */
  private int size;
  /** Current number of occurrence. */
  private int co;

  /**
   * Default constructor.
   */
  IndexStats() {
    nr = Prop.indexocc;
    occMin = new int[nr];
    occMax = new int[nr];
    txtMin = new byte[nr][];
    txtMax = new byte[nr][];
    for(int o = 0; o < txtMin.length; o++) {
      txtMin[o] = Token.EMPTY;
      txtMax[o] = Token.EMPTY;
      occMin[o] = Integer.MAX_VALUE;
    }
  }

  /**
   * Checks if the specified number of occurrence will be remembered.
   * @param oc number of occurrences
   * @return result of check
   */
  boolean adding(final int oc) {
    co = oc;
    size++;
    return oc > occMax[nr - 1] || oc < occMin[nr - 1];
  }
  
  /**
   * Adds the specified token.
   * @param tx token to be added
   */
  void add(final byte[] tx) {
    final boolean dsc = co > occMax[nr - 1];
    final byte[][] txt = dsc ? txtMax : txtMin;
    final int[] ocs = dsc ? occMax : occMin;
    for(int a = nr - 1; a >= 0; a--) {
      if(a == 0 || dsc && co < ocs[a - 1] || !dsc && co > ocs[a - 1]) {
        txt[a] = tx;
        ocs[a] = co;
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
  void print(final TokenBuilder tb) {
    tb.add(IDXENTRIES + size + NL);
    int max = 0;
    int c = 0;
    for(int o = 0; o < nr; o++) {
      int tl = txtMin[o].length;
      if(tl == 0) c++;
      else if(max < tl) max = tl;
      tl = txtMax[o].length;
      if(tl == 0) c++;
      else if(max < tl) max = tl;
    }

    print(tb, txtMax, occMax, max + 2);
    if(c == 0) tb.add("  " + DOTS + NL);
    print(tb, txtMin, occMin, max + 2);
  }

  /**
   * Internal method for printing the list.
   * @param tb token builder reference
   * @param txt text reference
   * @param ocs occurrences
   * @param len text length
   */
  private void print(final TokenBuilder tb, final byte[][] txt, final int[] ocs,
      final int len) {
    for(int o = 0; o < ocs.length; o++) {
      if(txt[o].length == 0) continue;
      tb.add("  ");
      tb.add(txt[o]);
      for(int j = 0; j < len - txt[o].length; j++) tb.add(' ');
      tb.add(ocs[o]);
      tb.add('x');
      tb.add(NL);
    }
  }
}
