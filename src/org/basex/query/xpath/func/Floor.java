package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Num;
import org.basex.query.xpath.values.Item;
import static org.basex.query.xpath.XPText.*;

/**
 * Constructor for floor() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Floor extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Floor(final Expr[] arg) {
    super(arg, "floor(item)");
  }

  @Override
  public Num eval(final XPContext ctx) throws QueryException {
    return new Num(Math.floor(evalArgs(ctx)[0].num()));
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    super.compile(ctx);
    if(args[0] instanceof Item) {
      ctx.compInfo(OPTFUNC, desc);
      return new Num(Math.floor(((Item) args[0]).num()));
    }
    return this;
  }
}
