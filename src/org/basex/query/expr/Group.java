/**
 * 
 */
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
 * Implementation of XQuery 1.1 Draft “GROUP BY”.
 * @author michael seiferle
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 */
public class Group extends Expr {
  /** Sequence to be grouped. */
  SeqIter sq;
  /** Group by specification. */
  Grp[] grp;
  
  /**
   * Postgrouped Tuples.
   */
  HashMap<Integer, int[]> groups = null;
 /**
   * Constructor.
   * @param e expressions
   */
  public Group(final Grp[] e) {
    grp = e;
  }
  
  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final Grp g : grp) g.comp(ctx);
    return this;
  }


  /**
   * Iterares over an array and merges its values.
   * @param i first array
   * @param j second array
   * @return i UNION j
   */
  private static int[] merge(final int[]i, final int j) {
    int[] result = new int[i.length + 1];
    int counter = 0;
    for (int k = 0; k < i.length; k++)  result[counter++] = i[k];
    result[i.length] = j; 
    return result;
  }
  /**
   * @param g grouping specifications.
   * @param lastitem se item 
   * *TODO* mach es funktionierend
   */
  @SuppressWarnings("unused")
  protected void group(final int[] g, final int lastitem) {
    if(groups == null) groups = new HashMap<Integer, int[]>();
    Item next = null;
    for(Grp group : this.grp) {
      if(group == null) { System.out.println("CONTIUINUES"); continue;}
      
      for(int i = 0; i < lastitem; i++) { // check all items matching
                                          //the specified Grouping option
        next = group.item(i);
        if(next == null)
          continue;
        if(groups.containsKey(next.hash())) {
          int[] items = groups.get(next.hash());
          groups.put(next.hash(), merge(items, i));
        } else {
          int[] vals = {i};
          groups.put(next.hash(), vals);
        }
      }
      return;
    }

  }
    
  
  
  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      final int e = sq.size();
      int[] group;
      Iter ir;
      int p = -1;
      
      @Override
      public Item next() throws QueryException {
        if(group == null) {
          // enumerate sort array and sort entries
          group = new int[e];
          for(int i = 0; i < e; i++) group[i] = i;
          group(group, e);
          for(final Grp g : grp) g.finish();
          
        }
        final Integer[] hashes =  groups.keySet().toArray(new Integer[groups.size()]);
        while(true) {
          if(ir != null) {
            final Item i = ir.next();
            if(i != null) return i;
            ir = null;
          } else {
            if(++p == hashes.length) return null;
            final int witness = groups.get( hashes[p])[0];
            ir = sq.item[witness].iter();
          }
        }
      }
      @Override
      public String toString() {
        return Group.this.toString();
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




  /**
   * Groups the elements specified by grp.
   */
  private void g() {
    System.out.println(this.groups);
  }


  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(GROUPBY);
    for(int o = 0; o != grp.length - 1; o++) grp[o].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(" group by ");
    for(int l = 0; l != grp.length - 1; l++) {
      sb.append((l != 0 ? ", " : "") + grp[l]);
    }
    return sb.toString();
  }

}
