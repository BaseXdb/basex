package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;

/**
 * Path Expression.
 * This Expression represents a relative location path operating
 * on a nodeset (return value of expression).
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Path extends Arr {
  /**
   * Constructor for a relative location path.
   * @param e Expression evaluating to a nodeset
   * @param p Location Path (or maybe other Expression after optimization)
   */
  public Path(final Expr e, final Expr p) {
    super(e, p);
  }

  @Override
  public Item eval(final XPContext ctx) throws QueryException {
    final Item val = ctx.eval(expr[0]);
    final Nod local = ctx.item;
    ctx.item = (Nod) val;
    final Nod ns = (Nod) ctx.eval(expr[1]);
    /*ns.ftidpos = ctx.local.ftidpos;
    ns.ftpointer = ctx.local.ftpointer;*/
    ctx.item = local;
    return ns;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr[0].plan(ser);
    expr[1].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return "Path(" + expr[0] + ", " + expr[1] + ')';
  }
}
