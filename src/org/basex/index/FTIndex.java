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
  Data data;

  /**
   * Reads pre and position values from the specified data source and
   * returns them via an iterator.
   * @param p pointer on data
   * @param s number of pre/pos values
   * @param da data source
   * @return iterator
   */
  IndexArrayIterator data(final long p, final int s, final DataAccess da) {
    return new IndexArrayIterator() {
      boolean f = true;
      long pos = p;
      int lpre, c;
      FTNode n;

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
        n = new FTNode(il);
        return true;
      }

      @Override
      public FTNode node() {
        n.genPointer(toknum);
        n.setToken(tok);
        return n;
      }

      @Override
      public int next() {
        return n.pre();
      }
    };
  }
}
