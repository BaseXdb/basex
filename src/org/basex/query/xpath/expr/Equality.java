package org.basex.query.xpath.expr;

import org.basex.index.Index;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.internal.IndexAccess;
import org.basex.query.xpath.locpath.Axis;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.LocPathRel;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Comp;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.Item;
import static org.basex.query.xpath.XPText.*;

/**
 * Equality Expression, evaluation comparisons.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Equality extends Comparison {
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   * @param t EqualityExpr.EQUALS or EqualityExpr.NOT_EQUALS
   */
  public Equality(final Expr e1, final Expr e2, final Comp t) {
    super(e1, e2);
    if(expr2 instanceof LocPathRel && expr1 instanceof Item) {
      expr1 = e2;
      expr2 = e1;
    }
    type = t;
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    expr1 = expr1.compile(ctx);
    expr2 = expr2.compile(ctx);

    if(expr1 instanceof Item && ((Item) expr1).size() == 0 ||
       expr2 instanceof Item && ((Item) expr2).size() == 0) {

      ctx.compInfo(OPTEQ1);
      return Bool.FALSE;
    }

    XPOptimizer.addText(expr1, ctx);
    XPOptimizer.addText(expr2, ctx);

    if(expr1 instanceof Item && expr2 instanceof Item) {
      ctx.compInfo(OPTEQ2);
      return Bool.get(type.eval((Item) expr1, (Item) expr2));
    }
    return this;
  }

  @Override
  public Path indexEquivalent(final XPContext ctx, final Step curr) {
    final LocPath path = (LocPath) expr1;
    final byte[] lit = ((Literal) expr2).str();
    final LocPath inv = path.invertPath(curr);

    ctx.compInfo(indexType == Index.TYPE.TXT ? OPTINDEX :
      indexType == Index.TYPE.ATV ? OPTATTINDEX : OPTWORDINDEX);
    if(indexType == Index.TYPE.ATV) {
      inv.steps.add(0, Axis.get(Axis.SELF, path.steps.last().test));
    }
    return new Path(new IndexAccess(indexType, lit), inv);
  }

  /** Index type. */
  private Index.TYPE indexType;

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    // check which expression is a location path
    if(!(expr1 instanceof LocPath)) return Integer.MAX_VALUE;

    // check which index can be applied
    indexType = ((LocPath) expr1).indexable(ctx, expr2, type);
    if(indexType == null) return Integer.MAX_VALUE;

    // skip too long tokens
    final byte[] token = ((Literal) expr2).str();

    final int nrIDs = ctx.local.data.nrIDs(indexType, token);
    return nrIDs < min ? nrIDs : Integer.MAX_VALUE;
  }
}
