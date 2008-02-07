package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Num;
import org.basex.query.xpath.values.Item;

/**
 * Constructor for string-length() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class StringLength extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public StringLength(final Expr[] arg) {
    super(arg, "string-length(item?)");
  }

  @Override
  public Num eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    return new Num((v.length == 0 ? ctx.local : v[0]).str().length);
  }

  @Override
  public boolean checkArguments() {
    return args.length <= 1;
  }
}
