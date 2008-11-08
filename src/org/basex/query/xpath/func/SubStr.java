package org.basex.query.xpath.func;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Str;
import org.basex.util.Token;

/**
 * Constructor for substring() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class SubStr extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public SubStr(final Expr[] arg) {
    super(arg, "substring(item, item, item?)");
  }

  @Override
  public Str eval(final XPContext ctx) throws QueryException {
    final Item[] v = evalArgs(ctx);
    final byte[] arg1 = v[0].str();
    int arg2 = (int) v[1].num();
    int arg3;
    if(v.length == 2) {
      arg3 = arg1.length;
      if(arg2 < 1) arg2 = 1;
    } else {
      arg3 = (int) v[2].num();
      if(arg2 < 1) {
        arg3 = arg3 - (1 - arg2);
        arg2 = 1;
      }
    }
    arg3 = arg3 + arg2 - 1;
    if(arg2 > arg1.length || arg3 < 0) return new Str(Token.EMPTY);
    if(arg3 > arg1.length) arg3 = arg1.length;
    return new Str(Token.substring(arg1, arg2 - 1, arg3));
  }

  @Override
  public boolean checkArguments() {
    return args.length == 2 || args.length == 3;
  }
}
