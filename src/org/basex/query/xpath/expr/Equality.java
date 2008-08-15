package org.basex.query.xpath.expr;

import org.basex.index.IndexToken;
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

    final Item i1 = expr1 instanceof Item ? (Item) expr1 : null;
    final Item i2 = expr2 instanceof Item ? (Item) expr2 : null;
    if(i1 != null && i1.size() == 0 || i2 != null && i2.size() == 0) {
      ctx.compInfo(OPTEQ1);
      return Bool.FALSE;
    }

    if(i1 != null && i2 != null) {
      ctx.compInfo(OPTEQ2);
      return Bool.get(type.eval(i1, i2));
    } else {
      XPOptimizer.addText(expr1, ctx);
      XPOptimizer.addText(expr2, ctx);
    }
    return this;
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq) {
    if (!(expr1 instanceof LocPath) || index == null) return this;
    
    final LocPath path = (LocPath) expr1;
    final LocPath inv = path.invertPath(curr);

    final boolean txt = index.type == IndexToken.TYPE.TXT;
    ctx.compInfo(txt ? OPTINDEX : OPTATTINDEX);
    if(!txt) inv.steps.add(0, Axis.create(Axis.SELF, path.steps.last().test));
    return new Path(new IndexAccess(index), inv);
  }

  /** Index type. */
  private IndexToken index;

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    // check if first expression is no location path
    if(!(expr1 instanceof LocPath)) return Integer.MAX_VALUE;

    // check which index can be applied
    index = ((LocPath) expr1).indexable(ctx, expr2, type);
    if(index == null) return Integer.MAX_VALUE;

    // return number of expected index results
    return ctx.local.data.nrIDs(index);
  }
}
