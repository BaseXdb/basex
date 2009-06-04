package org.basex.index;

import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.util.IntList;

/**
 * This abstract class defines methods for the available full-text indexes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class FTIndex extends Index {
  /** Cache for number of hits and data reference per token. */
  final FTTokenMap cache = new FTTokenMap();
  /** Values file. */
  final Data data;
  
  /**
   * Constructor.
   * @param d data reference
   */
  FTIndex(final Data d) {
    data = d;
  }

  /**
   * Returns an iterator for an index entry.
   * @param p pointer on data
   * @param s number of pre/pos values
   * @param da data source
   * @return iterator
   */
  FTIndexIterator iter(final long p, final int s, final DataAccess da) {
    return new FTIndexIterator() {
      boolean f = true;
      long pos = p;
      int lpre, c;
      FTEntry n;

      @Override
      public boolean more() {
        if(c == s) return false;

        if(f) {
          lpre = da.readNum(pos);
          pos = da.pos();
          f = false;
        }

        final IntList il = new IntList();
        int pre = lpre;
        il.add(pre);
        il.add(da.readNum(pos));
        while(++c < s && (lpre = da.readNum()) == pre) il.add(da.readNum());
        pos = da.pos();

        final int[] t = new int[il.size];
        for(int i = 0; i < t.length; i++) t[i] = toknum;
        n = new FTEntry(il, new IntList(t));
        return true;
      }

      @Override
      public FTEntry node() {
        return n;
      }

      @Override
      public int next() {
        return n.pre();
      }
    };
  }
}
