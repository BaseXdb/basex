package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.Item;
import org.basex.util.Token;

/**
 * Constructor for substring-before() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class SubstringBefore extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public SubstringBefore(final Expr[] arg) {
    super(arg, "substring-before(item, item)");
  }

  @Override
  public Literal eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    final byte[] b1 = v[0].str();
    final byte[] b2 = v[1].str();
    final int i = Token.indexOf(b1, b2);
    final byte[] b = i == -1 ? Token.EMPTY : Token.substring(b1, 0, i);
    return new Literal(b);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 2;
  }
}
