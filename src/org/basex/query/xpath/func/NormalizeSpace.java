package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Str;
import org.basex.util.Token;

/**
 * Constructor for string() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class NormalizeSpace extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public NormalizeSpace(final Expr[] arg) {
    super(arg, "normalize-space(item)");
  }

  @Override
  public Str eval(final XPContext ctx) throws QueryException {
    return new Str(Token.norm(evalArgs(ctx)[0].str()));
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }
}
