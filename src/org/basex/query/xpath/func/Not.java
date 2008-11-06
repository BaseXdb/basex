package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.values.Bool;

/**
 * Constructor for not() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Not extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Not(final Expr[] arg) {
    super(arg, "not(item)");
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    return Bool.get(!evalArgs(ctx)[0].bool());
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);
    if(args[0] instanceof LocPath) {
      ((LocPath) args[0]).addPosPred(ctx);
    }
    return this;
  }
}
