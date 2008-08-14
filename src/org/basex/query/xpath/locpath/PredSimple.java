package org.basex.query.xpath.locpath;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Comparison;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.func.Position;
import org.basex.query.xpath.values.Comp;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Num;
import org.basex.query.xpath.values.Item;

/**
 * XPath predicate.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class PredSimple extends Pred {
  /** Predicate expression. */
  private Expr expr;

  /**
   * Empty Constructor.
   */
  protected PredSimple() { }

  /**
   * Constructor.
   * @param exp Expression to evaluate
   */
  public PredSimple(final Expr exp) {
    expr = exp;
  }

  @Override
  NodeBuilder eval(final XPContext ctx, final NodeBuilder set)
      throws QueryException {

    final int[] nodes = set.nodes;
    final int size = set.size;
    final NodeBuilder tmp = new NodeBuilder();
    ctx.local.currSize = size;

    for(int n = 0; n != size; n++) {
      final int pre = nodes[n];
      ctx.local.set(pre);
      ctx.local.currPos = n + 1;

      final Item v = ctx.eval(expr);
      if(v instanceof Num) {
        if(v.num() == n + 1) tmp.add(pre);
      } else if(v.bool()) {
        tmp.add(pre);
      }
    }
    return tmp;
  }

  @Override
  boolean eval(final XPContext ctx, final NodeSet nodes, final int pos)
      throws QueryException {

    nodes.currPos = pos;
    final Item v = ctx.eval(expr);
    return v instanceof Num ? v.num() == pos : v.bool();
  }

  @Override
  boolean usesSize() {
    return expr.usesSize();
  }

  @Override
  boolean usesPos() {
    return expr.usesPos();
  }

  @Override
  int posPred() {
    return 0;
  }

  @Override
  Pred compile(final XPContext ctx) throws QueryException {
    expr = expr.compile(ctx);

    if(expr instanceof LocPath) {
      ((LocPath) expr).addPosPred(ctx);
      return this;
    }

    if(expr instanceof Num) {
      ctx.compInfo(OPTPOS);
      final int pos = (int) ((Num) expr).num();
      return new PredPos(pos, pos);
    }

    final int pos = getPos(expr);
    if(pos >= 0) {
      ctx.compInfo(OPTPOS);
      final Comparison cmp = (Comparison) expr;
      if(cmp.type == Comp.EQ) return new PredPos(pos, pos);
      if(cmp.type == Comp.GT) return new PredPos(pos + 1, Integer.MAX_VALUE);
      if(cmp.type == Comp.GE) return new PredPos(pos, Integer.MAX_VALUE);
      if(cmp.type == Comp.LT) return new PredPos(1, pos - 1);
      if(cmp.type == Comp.LE) return new PredPos(1, pos);
    }
    return this;
  }

  /**
   * Returns a position, defined in a position function.
   * @param ex expression to be checked
   * @return position
   */
  private int getPos(final Expr ex) {
    if(ex instanceof Comparison) {
      final Comparison cmp = (Comparison) ex;
      if(cmp.expr1 instanceof Position && cmp.expr2 instanceof Num) {
        return (int) ((Num) cmp.expr2).num();
      }
    }
    return -1;
  }

  @Override
  boolean alwaysFalse() {
    return expr instanceof Item ? !((Item) expr).bool() : false;
  }

  @Override
  boolean alwaysTrue() {
    return expr instanceof Item ? ((Item) expr).bool() : false;
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step step, 
      final boolean seq)
      throws QueryException {
    return expr.indexEquivalent(ctx, step, seq);
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    return expr.indexSizes(ctx, curr, min);
  }

  @Override
  public boolean sameAs(final Pred pred) {
    return pred instanceof PredSimple && expr.sameAs(((PredSimple) pred).expr);
  }

  @Override
  public String toString() {
    return '[' + expr.toString() + ']';
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    expr.plan(ser);
    ser.closeElement(this);
  }
  /**
   * Getter for expr.
   * 
   * @return expr Expression
   */
  public Expr getExpr() {
    return expr;
  }
}
