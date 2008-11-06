package org.basex.query.xpath.func;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Num;
import org.basex.query.xpath.values.Item;

/**
 * Interface for XPath functions.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public abstract class Func extends Expr {
  /** Function results. */
  final Expr[] args;
  /** Function description. */
  public final String desc;

  /**
   * Function constructor.
   * @param arg arguments
   * @param dsc expected function pattern
   */
  public Func(final Expr[] arg, final String dsc) {
    args = arg;
    desc = dsc;
  }

  /**
   * Evaluates the arguments.
   * @param ctx query context
   * @return xpath values
   * @throws QueryException evaluation exception
   */
  public final Item[] evalArgs(final XPContext ctx) throws QueryException {
    /** Function arguments. */
    final Item[] arg = new Item[args.length];
    for(int a = 0; a < args.length; a++) arg[a] = ctx.eval(args[a]);
    return arg;
  }

  /**
   * Checks validity/number of arguments.
   * @return result of comparison
   */
  public abstract boolean checkArguments();

  @Override
  public boolean usesSize() {
    return false;
  }
  
  @Override
  public boolean usesPos() {
    return returnedValue() == Num.class;
  }
  
  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    for(int a = 0; a < args.length; a++) args[a] = args[a].comp(ctx);
    return this;
  }

  @Override
  public final String toString() {
    final StringBuilder inf = new StringBuilder();
    inf.append(desc.substring(0, desc.indexOf("(") + 1));
    for(int a = 0; a < args.length; a++) {
      if(a != 0) inf.append(", ");
      inf.append(args[a]);
    }
    return inf + ")";
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr a : args) a.plan(ser);
    ser.closeElement();
  }
}
