package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.Item;
import org.basex.util.TokenBuilder;

/**
 * Constructor for concat() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Concat extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Concat(final Expr[] arg) {
    super(arg, "concat(item, item+)");
  }

  @Override
  public Literal eval(final XPContext ctx) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Item v : evalArgs(ctx)) tb.add(v.str());
    return new Literal(tb.finish());
  }

  @Override
  public boolean checkArguments() {
    return args.length > 1;
  }
}
