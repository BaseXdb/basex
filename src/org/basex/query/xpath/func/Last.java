package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Num;
import static org.basex.query.xpath.XPText.*;

/**
 * Constructor for last() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Last extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Last(final Expr[] arg) {
    super(arg, "last()");
  }

  @Override
  public Num eval(final XPContext ctx) throws QueryException {
    if(ctx.item.currSize > 0) return new Num(ctx.item.currSize);
    throw new QueryException(LASTEXC);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 0;
  }

  @Override
  public boolean usesSize() {
    return true;
  }
}
