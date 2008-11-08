package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Item;
import org.basex.util.Token;

/**
 * Constructor for ends-with() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class EndsWith extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public EndsWith(final Expr[] arg) {
    super(arg, "ends-with(item, item)");
  }

  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    return Bln.get(Token.endsWith(v[0].str(), v[1].str()));
  }

  @Override
  public boolean checkArguments() {
    return args.length == 2;
  }
}
