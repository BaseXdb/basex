package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Num;
import org.basex.query.xpath.values.Item;
import static org.basex.query.xpath.XPText.*;

/**
 * Constructor for round() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Round extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Round(final Expr[] arg) {
    super(arg, "round(item)");
  }

  @Override
  public Num eval(final XPContext ctx) throws QueryException {
    return new Num(Math.round(evalArgs(ctx)[0].num()));
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
      return new Num(Math.round(((Item) args[0]).num()));
    }
    return this;
  }
}
