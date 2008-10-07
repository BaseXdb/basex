package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Literal;

/**
 * Constructor for string() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class StringFunc extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public StringFunc(final Expr[] arg) {
    super(arg, "string(item?)");
  }

  @Override
  public Literal eval(final XPContext ctx) throws QueryException {
    return new Literal((args.length == 0 ? ctx.item :
      evalArgs(ctx)[0]).str());
  }

  @Override
  public boolean checkArguments() {
    return args.length <= 1;
  }
}
