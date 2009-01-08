package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.path.LocPath;

/**
 * Path Expression.
 * This Expression represents a relative location path operating
 * on a nodeset (return value of expression).
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Path extends Expr {
  /** Expression to be filtered. */
  public Expr expr;
  /** Location Path. */
  public LocPath path;
  
  /**
   * Constructor for a relative location path.
   * @param e expression evaluating to a nodeset
   * @param p location path (or maybe other Expression after optimization)
   */
  public Path(final Expr e, final LocPath p) {
    expr = e;
    path = p;
  }

  @Override
  public Item eval(final XPContext ctx) throws QueryException {
    final Item val = ctx.eval(expr);
    final Nod local = ctx.item;
    ctx.item = (Nod) val;
    final Nod ns = (Nod) ctx.eval(path);
    ctx.item = local;
    return ns;
  }

  @Override
  public boolean usesSize() {
    return expr.usesSize() || path.usesSize();
  }
  
  @Override
  public boolean usesPos() {
    return expr.usesPos() || path.usesPos();
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    expr = expr.comp(ctx);
    path = (LocPath) path.comp(ctx);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr.plan(ser);
    path.plan(ser);
    ser.closeElement();
  }

  @Override
  public String color() {
    return "FF9999";
  }

  @Override
  public String toString() {
    return BaseX.info("%(%, %)", name(), expr, path);
  }
}
