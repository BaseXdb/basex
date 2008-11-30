package org.basex.query.xpath.expr;

import org.basex.index.IndexToken;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.internal.IndexAccess;
import org.basex.query.xpath.item.Comp;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.path.Axis;
import org.basex.query.xpath.path.LocPath;
import org.basex.query.xpath.path.Step;

import static org.basex.query.xpath.XPText.*;

/**
 * Equality Expression, evaluating comparisons.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Equality extends Cmp {
  /** Index type. */
  private IndexToken index;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   * @param t {@link Comp#EQ} or {@link Comp#NE}
   */
  public Equality(final Expr e1, final Expr e2, final Comp t) {
    super(e1, e2);
    // evaluate location path first
    if(expr[1] instanceof LocPath && expr[0] instanceof Item) {
      expr[0] = e2;
      expr[1] = e1;
    }
    type = t;
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq) {

    // no index access possible - return self reference
    if(index == null) return this;

    final LocPath path = (LocPath) expr[0];
    final LocPath inv = path.invertPath(curr);

    final boolean txt = index.type == IndexToken.Type.TXT;
    ctx.compInfo(txt ? OPTINDEX : OPTATTINDEX);
    if(!txt) inv.steps.add(0, Axis.create(Axis.SELF, path.steps.last().test));
    return new Path(new IndexAccess(index), inv);
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    // check if first expression is no location path
    if(!(expr[0] instanceof LocPath)) return Integer.MAX_VALUE;

    // check which index can be applied
    index = ((LocPath) expr[0]).indexable(ctx, expr[1], type);
    if(index == null) return Integer.MAX_VALUE;

    // return number of expected index results
    return ctx.item.data.nrIDs(index);
  }
}
