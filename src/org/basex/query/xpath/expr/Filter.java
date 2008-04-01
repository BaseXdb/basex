package org.basex.query.xpath.expr;

import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.Preds;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;

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
  public NodeSet eval(final XPContext ctx) throws QueryException {
    final NodeSet nodes = (NodeSet) ctx.eval(expr);
    // <cg> maybe this causes problems???
    final NodeBuilder input = new NodeBuilder(nodes.nodes, nodes.ftidpos, 
        nodes.ftpointer);
    final NodeBuilder result = new NodeBuilder();
    preds.eval(ctx, input, result);
    return new NodeSet(result.finish(), ctx);
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
  public Expr compile(final XPContext ctx) throws QueryException {
    expr = expr.compile(ctx);
    return preds.compile(ctx) ? this : new NodeSet(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    expr.plan(ser);
    preds.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FF9999";
  }

  @Override
  public String toString() {
    return "Filter(" + expr.toString() + preds.toString() + ')';
  }
}
