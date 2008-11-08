package org.basex.query.xpath.func;

import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Bln;

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
  public Bln eval(final XPContext ctx) {
    return Bln.FALSE;
  }

  @Override
  public boolean checkArguments() {
    return args.length == 0;
  }

  @Override
  public Bln comp(final XPContext ctx) {
    return Bln.FALSE;
  }
}
