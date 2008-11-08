package org.basex.query.xpath.func;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Dbl;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;

/**
 * Constructor for sum() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Sum extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Sum(final Expr[] arg) {
    super(arg, "sum(item)");
  }

  @Override
  public Dbl eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    if(!(v[0] instanceof Nod)) return new Dbl(v[0].num());

    final Nod set = (Nod) v[0];
    double sum = 0;
    final Data data = ctx.item.data;
    for(final int node : set.nodes) sum += data.atomNum(node);
    return new Dbl(sum);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }
}
