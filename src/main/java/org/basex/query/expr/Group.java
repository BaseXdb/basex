package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Implementation of the group by clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class Group extends ParseExpr {

//  /** Sequence to be grouped. */
//  SeqIter sq;

  /** Group by specification. */
  private final GroupBy[] groupby;

//  /**
//   * Post grouped tuples. [MS] remove this
//   */
//  HashMap<String, ArrayList<Item>> groups;
//

  /** Grouping partition. **/
  GroupPartition gp;

  /**
   * Constructor.
   * @param ii input info
   * @param gb group by expression
   */
  public Group(final InputInfo ii, final GroupBy[] gb) {
    super(ii);
    groupby = gb;

  }
  /**
   * Initializes the grouping partition.
   * @param fl ForLet
   */
  public void initgroup(final ForLet[] fl) {
    Var[] vs = new Var[groupby.length];
    Var[] fs = new Var[fl.length];
    for(int i = 0; i < groupby.length; i++) {
      VarCall call = (VarCall) groupby[i].expr;
      vs[i] = call.var;
    }
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

  @Override
  public Iter iter(final QueryContext ctx) {
    return
    new Iter() { // group is blocking => no iterator

      @Override
      public Item next() {
        Main.notexpected(this);
        return null;
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
   * @throws QueryException ex
   */
  public void add(final QueryContext ctx) throws QueryException {
    gp.add(ctx);
  }



}
