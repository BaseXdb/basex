package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Dbl;

/**
 * Constructor for number() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class NumFunc extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public NumFunc(final Expr[] arg) {
    super(arg, "number(item?)");
  }

  @Override
  public Dbl eval(final XPContext ctx) throws QueryException {
    return new Dbl((args.length == 0 ? ctx.item : evalArgs(ctx)[0]).num());
  }

  @Override
  public boolean checkArguments() {
    return args.length <= 1;
  }

  /* comment added to simplify allow tests without optimizations
  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);
    if(args.length != 0 && args[0] instanceof Item) {
      ctx.compInfo(OPTFUNC, desc);
      return new Dbl(((Item) args[0]).num());
    }
    return this;
  }*/
}
