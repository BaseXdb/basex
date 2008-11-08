package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Dbl;

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
  public Dbl eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    return new Dbl((v.length == 0 ? ctx.item : v[0]).str().length);
  }

  @Override
  public boolean checkArguments() {
    return args.length <= 1;
  }
}
