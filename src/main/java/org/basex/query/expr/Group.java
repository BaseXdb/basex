package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import java.util.ArrayList;
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
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class Group extends Expr {
  /** Sequence to be grouped. */
  SeqIter sq;
  /** Group by specification. */
  final GroupBy[] groupby;
  /**
   * Post grouped tuples. [MS] remove this
   */
  HashMap<String, ArrayList<Item>> groups;

  /** Grouping partition. **/
  GroupPartition gp;
  
  /**
   * Constructor.
   * @param gb group by Expressiosn
   */
  public Group(final GroupBy[] gb) {
    groupby = gb;

  }
  /**
   * Initializes the grouping partition.
   * @param fl ForLet
   */
  public void initgroup(final ForLet[] fl) {
    Var[] vs = new Var[groupby.length];
    Var[] fs = new Var[fl.length];
    for(int i = 0; i < groupby.length; i++)
      vs[i] = ((VarCall) groupby[i].expr).var;
    for(int i = 0; i < fl.length; i++)
      fs[i] = fl[i].var;
    gp = new GroupPartition(vs, fs);


  }
  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final GroupBy g : groupby)
      g.comp(ctx);
    return this;
  }

//  /**
//   * Groups the items.
//   * @throws QueryException exception.
//   */
//  protected void grouping() throws QueryException {
//    groups = new HashMap<String, ArrayList<Item>>();
//
//    for(GroupBy gb : groupby) {
//      Var key = ((VarCall) gb.expr).var;
//      ArrayList<Item> partition = new ArrayList<Item>();
//      Item it;
//
//      for(int i = 0; i < gb.seq.size(); i++) {
//        it = gb.item(i);
//        boolean found = false;
//        for(final Item cand : partition) {
//          if(it.e()) {
//            found = true;
//          } else if(cand.eq(it)) { // cand.hashCode() == it.hashCode()
//            found = true;
//            break;
//          }
//        }
//        if(!found) partition.add(it);
//      }
//      groups.put(key.toString(), partition);
//    }
//  }

  // *TODO* delete this?
  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() { // group is blocking => no iterator
      Iter ir;
      int p = -1;

      @Override
      public Item next() throws QueryException {
        if(groups == null) {
          // group();
          for(final GroupBy g : groupby)
            g.finish();
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
            final int witness = 0; // groups.get(hashes[p])[0];
            ir = sq.item[witness].iter();
          }
        }
      }
    };
  }

  @Override
  public boolean uses(final Use use, final QueryContext ctx) {
    for(final GroupBy g : groupby)
      if(g.uses(use, ctx)) return true;
    return false;
  }

  @Override
  public Group remove(final Var v) {
    for(int o = 0; o < groupby.length; o++)
      groupby[o] = groupby[o].remove(v);
    return this;
  }

  @Override
  public String color() {
    return "66FF66";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int o = 0; o != groupby.length; o++)
      groupby[o].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(" " + GROUP + " " + BY + " ");
    for(int l = 0; l != groupby.length; l++) {
      sb.append(l != 0 ? ", " : "").append(groupby[l]);
    }
    return sb.toString();
  }

  /**
   * Adds the current context to the grouping partition.
   * @param ctx context
   */
  public void add(final QueryContext ctx) {
    gp.add(ctx);
  }



}
