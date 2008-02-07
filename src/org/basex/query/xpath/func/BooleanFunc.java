package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Bool;

/**
 * Constructor for boolean() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class BooleanFunc extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public BooleanFunc(final Expr[] arg) {
    super(arg, "boolean(item?)");
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    return Bool.get((args.length == 0 ? ctx.local :
      evalArgs(ctx)[0]).bool());
  }

  @Override
  public boolean checkArguments() {
    return args.length <= 1;
  }
}
