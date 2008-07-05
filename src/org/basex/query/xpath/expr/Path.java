package org.basex.query.xpath.expr;

import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;

/**
 * Path Expression.
 * This Expression represents a relative location path operating
 * on a nodeset (return value of expression).
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Path extends DualExpr {
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
    final Item val = ctx.eval(expr1);
    final NodeSet local = ctx.local;
    ctx.local = (NodeSet) val;
    int[][] ftprepos =  ctx.local.ftidpos;
    int[] ftpoin = ctx.local.ftpointer;
    final NodeSet ns = (NodeSet) ctx.eval(expr2);
    ns.ftidpos = ftprepos;
    ns.ftpointer = ftpoin;
    ctx.local = local;
    return ns;
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    expr1 = expr1.compile(ctx);
    expr2 = expr2.compile(ctx);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    expr1.plan(ser);
    expr2.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FF9900";
  }

  @Override
  public String toString() {
    return "Path(" + expr1 + ", " + expr2 + ')';
  }
}
