package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import java.util.HashMap;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Var;

/**
 * Implementation of the group by clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Michael Seiferle
 */
public class Group extends Expr {
  /** Sequence to be grouped. */
  SeqIter sq;

  /** Resulting Sequence (Sequence Grouped). **/
  SeqIter sg;
  /** Group by specification. */
  Grp[] grp;

  /**
   * Post grouped Tuples.
   */
  HashMap<Integer, int[]> groups = null;

  /**
   * Constructor.
   * @param g groups
   */
  public Group(final Grp[] g) {
    grp = g;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final Grp g : grp) g.comp(ctx);
    return this;
  }

  /**
   * Iterates over an array and merges its values.
   * @param i first array
   * @param j second array
   * @return i UNION j
   */
  private static int[] merge(final int[]i, final int j) {
    final int[] result = new int[i.length + 1];
    int counter = 0;
    for(final int r : i) result[counter++] = r;
    result[i.length] = j;
    return result;
  }

  /**
   * Groups the Items.
   */
  protected void group() {
    if(groups == null) groups = new HashMap<Integer, int[]>();
    Item next = null;
    for(final Grp group : grp) {
      if(group == null) { continue; }
      final int lastitem = sq.size();
      for(int i = 0; i < lastitem; i++) { // check all items matching
                                          //the specified Grouping option
        next = group.item(i);
        if(next == null)
          continue;
        if(groups.containsKey(next.hash())) {
          final int[] items = groups.get(next.hash());
          groups.put(next.hash(), merge(items, i));
        } else {
          final int[] vals = {i};
          groups.put(next.hash(), vals);
        }
      }
      return;
    }
  }



  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      Iter ir;
      int p = -1;

      @Override
      public Item next() throws QueryException {
        if(groups == null) {
          group();
          for(final Grp g : grp) g.finish();
        }
        final int s = groups.size();
        final Integer[] hashes = groups.keySet().toArray(new Integer[s]);

        while(true) {
          if(ir != null) {
            final Item i = ir.next();
            if(i != null) return i;
            ir = null;
          } else {
            if(++p == hashes.length) return null;
            final int witness = groups.get(hashes[p])[0];

            // [MS] fill witness with all of its
            // fellow group members children
            ir = sq.item[witness].iter();
          }
        }
      }
    };
  }


  /**
   * Adds the items to be grouped.
   * @param ctx query context
   * @throws QueryException evaluation exception
   */
  public void add(final QueryContext ctx) throws QueryException {
    for(final Grp g : grp) g.add(ctx);

  }

  @Override
  public boolean uses(final Use use, final QueryContext ctx) {
    for(final Grp g : grp) if(g.uses(use, ctx)) return true;
    return false;
  }

  @Override
  public Group remove(final Var v) {
    for(int o = 0; o < grp.length; o++) grp[o] = grp[o].remove(v);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(GROUPBY);
    for(int o = 0; o != grp.length; o++) grp[o].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(" " + GROUP + " " + BY + " ");
    for(int l = 0; l != grp.length; l++) {
      sb.append((l != 0 ? ", " : "") + grp[l]);
    }
    return sb.toString();
  }

}
