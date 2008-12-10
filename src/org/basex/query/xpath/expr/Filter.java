package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.NodeBuilder;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.path.Preds;

/**
 * Filter Expression filtering a nodeset. This Expression is invalid for other
 * types as nodesets.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Filter extends Expr {
  /** Expression to be filtered. */
  private Expr expr;
  /** Predicate. */
  private Preds preds;

  /**
   * Constructor.
   * @param e Expression (has to result in a nodeset!)
   * @param p Predicate to filter nodeset
   */
  public Filter(final Expr e, final Preds p) {
    expr = e;
    preds = p;
  }

  @Override
  public Nod eval(final XPContext ctx) throws QueryException {
    final Nod nodes = (Nod) ctx.eval(expr);
    final NodeBuilder input = new NodeBuilder(nodes.nodes);
    final NodeBuilder result = new NodeBuilder();
    preds.eval(ctx, input, result);
    return new Nod(result.finish(), ctx);
  }

  @Override
  public boolean usesSize() {
    return expr.usesSize() || preds.usesSetSize();
  }
  
  @Override
  public boolean usesPos() {
    return expr.usesPos() || preds.usesPos();
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    expr = expr.comp(ctx);
    return preds.compile(ctx) ? this : new Nod(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr.plan(ser);
    preds.plan(ser);
    ser.closeElement();
  }

  @Override
  public String color() {
    return "FF9999";
  }

  @Override
  public String toString() {
    return BaseX.info("%(%%)", name(), expr, preds);
  }
}
