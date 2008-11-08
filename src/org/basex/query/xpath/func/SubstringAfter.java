package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Str;
import org.basex.util.Token;

/**
 * Constructor for substring-after() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class SubstringAfter extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public SubstringAfter(final Expr[] arg) {
    super(arg, "substring-after(item, item)");
  }

  @Override
  public Str eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    final byte[] b1 = v[0].str();
    final byte[] b2 = v[1].str();
    final int i = Token.indexOf(b1, b2);
    return i == -1 ? new Str(Token.EMPTY) :
      new Str(Token.substring(b1, i + b2.length, b1.length));
  }

  @Override
  public boolean checkArguments() {
    return args.length == 2;
  }
}
