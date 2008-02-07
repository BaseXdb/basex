package org.basex.query.xpath.func;

import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Bool;

/**
 * Constructor for false() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class False extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public False(final Expr[] arg) {
    super(arg, "false()");
  }

  @Override
  public Bool eval(final XPContext ctx) {
    return Bool.FALSE;
  }

  @Override
  public boolean checkArguments() {
    return args.length == 0;
  }

  @Override
  public Bool compile(final XPContext ctx) {
    return Bool.FALSE;
  }
}
