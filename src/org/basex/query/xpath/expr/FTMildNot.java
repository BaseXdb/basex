package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;

/**
 * Logical FTMildNot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTMildNot extends FTArrayExpr {
  /**
   * Constructor.
   * @param e expressions
   */
  public FTMildNot(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    boolean f = false;
    for(final Expr e : exprs) f |= e.eval(ctx).bool();
    return Bool.get(f);
  }

  @Override
  public FTArrayExpr compile(final XPContext ctx) throws QueryException {
    for(int i = 0; i != exprs.length; i++) {
      if(exprs[i].fto == null) exprs[i].fto = fto;
      exprs[i] = exprs[i].compile(ctx);
    }
    return this;
  }

  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr,
      final boolean seq) throws QueryException {

    // assumption 1: ftcontains "a" not in "a b" not in "a c"
    // and ftcontains "a" not in "a b" ftand "a" not in "a c" are equivalent
    // assumption 2: ftcontains "a" not in ftnot ***
    // and ftcontains "a" are equivalent

    final FTArrayExpr[] indexExprs = new FTArrayExpr[2];
    final FTMildNotExprs[] mne = new FTMildNotExprs[exprs.length - 1];
    final int[] pex = new int[exprs.length - 1];
    indexExprs[0] = exprs[0].indexEquivalent(ctx, curr, seq);
    for (int i = 1; i < exprs.length; i++) {
      indexExprs[1] = exprs[i].indexEquivalent(ctx, curr, seq);
      mne[i - 1] = new FTMildNotExprs(indexExprs);
      pex[i - 1] = i - 1;
    }
    if (mne.length == 1) {
      return mne[0];
    }

    return new FTIntersection(mne, pex, new int[]{});
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    final int mmin = exprs[0].indexSizes(ctx, curr, min);
    return mmin > min ? min : mmin;
  }
}
