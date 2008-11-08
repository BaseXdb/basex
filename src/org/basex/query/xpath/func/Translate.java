package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Str;
import org.basex.util.Token;

/**
 * Constructor for translate() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Translate extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Translate(final Expr[] arg) {
    super(arg, "translate(item, item, item)");
  }

  @Override
  public Str eval(final XPContext ctx)
      throws QueryException {
    
    final Item[] v = evalArgs(ctx);
    return new Str(Token.translate(v[0].str(),  v[1].str(),
        v[2].str()));
  }

  @Override
  public boolean checkArguments() {
    return args.length == 3;
  }
}
