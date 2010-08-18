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
import org.basex.util.TokenBuilder;

/**
 * Implementation of the group by clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class Group extends ParseExpr {
  /** Group by specification. */
  private final Var[] groupby;
  /** Grouping partition. **/
  GroupPartition gp;

  /**
   * Constructor.
   * @param ii input info
   * @param gb group by expression
   */
  public Group(final InputInfo ii, final Var[] gb) {
    super(ii);
    groupby = gb;
  }

  /**
   * Initializes the grouping partition.
   * @param fl ForLet
   */
  public void initgroup(final ForLet[] fl) {
    final Var[] vs = new Var[groupby.length];
    Var[] fs = new Var[fl.length];
    for(int i = 0; i < groupby.length; ++i) {
      vs[i] = groupby[i];
    }
    for(int i = 0; i < fl.length; ++i)
      fs[i] = fl[i].var;
    gp = new GroupPartition(vs, fs);


  }
  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final Var g : groupby)
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
  public boolean uses(final Use use) {
    for(final Var v : groupby) if(v.uses(use)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Var g : groupby) if(g.eq(v)) return false;
    return true;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int o = 0; o != groupby.length; ++o) groupby[o].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return new TokenBuilder(" " + GROUP + " " + BY + " ").add(
        groupby, ", ").toString();
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
