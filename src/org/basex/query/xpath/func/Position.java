package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Num;
import static org.basex.query.xpath.XPText.*;

/**
 * Constructor for position() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Position extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Position(final Expr[] arg) {
    super(arg, "position()");
  }

  @Override
  public Num eval(final XPContext ctx) throws QueryException {
    // size == 0 means no size (nodeset) available
    // size == -1 means we are in early predicate evaluation
    if(ctx.local.currSize != 0) return new Num(ctx.local.currPos);
    throw new QueryException(INVALIDPOS);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 0;
  }

  @Override
  public boolean usesPos() {
    return true;
  }
}
