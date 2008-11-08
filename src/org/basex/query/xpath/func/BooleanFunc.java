package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Bln;

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
  public Bln eval(final XPContext ctx) throws QueryException {
    return Bln.get((args.length == 0 ? ctx.item :
      evalArgs(ctx)[0]).bool());
  }

  @Override
  public boolean checkArguments() {
    return args.length <= 1;
  }
}
