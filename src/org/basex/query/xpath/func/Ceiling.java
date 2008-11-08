package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Dbl;
import static org.basex.query.xpath.XPText.*;

/**
 * Constructor for ceiling() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Ceiling extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Ceiling(final Expr[] arg) {
    super(arg, "ceiling(item)");
  }

  @Override
  public Dbl eval(final XPContext ctx) throws QueryException {
    return new Dbl(Math.ceil(evalArgs(ctx)[0].num()));
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);
    if(args[0] instanceof Item) {
      ctx.compInfo(OPTFUNC, desc);
      return new Dbl(Math.ceil(((Item) args[0]).num()));
    }
    return this;
  }
}
