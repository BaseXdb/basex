package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.util.IntList;

/**
 * FTOr expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTOr extends FTExpr {
  /** Saving index of positive expressions. */
  private int[] pex;
  /** Saving index of negative (ftnot) expressions. */
  private int[] nex;
  
  /**
   * Constructor.
   * @param e expression list
   */
  public FTOr(final FTExpr... e) {
    super(e);
  }

  @Override
  public FTNodeItem atomic(final QueryContext ctx) throws QueryException {
    FTNodeItem it = null; 
    double d = 0;
    for(final FTExpr e : expr) {
      it = e.atomic(ctx);
      d = ctx.score.or(it.score(), d);
    }
    it.score(d);
    return it;
  }
  
  @Override
  public boolean indexAccessible(final QueryContext ctx,
      final IndexContext ic) throws QueryException {

    final IntList p = new IntList();
    final IntList n = new IntList();
    final int min = ic.is;
    int sum = 0;

    for(int i = 0; i < expr.length; i++) {
      ic.ftnot = false;
      if(!expr[i].indexAccessible(ctx, ic)) return false;
      if(ic.ftnot) {
        if(ic.is > 0) n.add(i);
        else {
          ic.seq = true;
          ic.is = Integer.MAX_VALUE;
          return false;
        }
      } else if(ic.is > 0) {
        p.add(i);
        sum += ic.is;
      }
    }
    nex = n.finish();
    pex = p.finish();

    if(pex.length == 0 && nex.length > 0) {
      ic.seq = true;
      ic.is = Integer.MAX_VALUE;
    } else if(nex.length > 0 && pex.length > 0) {
      ic.seq = true;
      ic.is = Integer.MAX_VALUE;
    } else {
      ic.is = sum > min ? min : sum;
    } 
    return true;
  }
  
  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic)
      throws QueryException {

    for(int i = 0; i < expr.length; i++) {
      expr[i] = expr[i].indexEquivalent(ctx, ic);
    }

    if(pex.length == 0) {
      // !A FTOR !B = !(a ftand b)
      for(int i = 0; i < nex.length; i++) {
        expr[nex[i]] = expr[nex[i]].expr[0];
      }
      return new FTNotIndex(new FTIntersection(pex, nex, expr));
    }

    if(pex.length == 0) return new FTUnion(nex, true, expr);
    if(nex.length == 0) return new FTUnion(pex, true, expr);
    if(pex.length == 1 && nex.length == 0) return expr[pex[0]];
    return new FTUnion(gen(), true, expr);
  }
  
  /**
   * Generate sequence for nex.length > 0 && pex.length > 0.
   * @return sequence
   */
  private int[] gen() {
    final int[] r = new int[expr.length];
    for(int i = 0; i < expr.length; i++) r[i] = i;
    return r;
  }

  @Override
  public String toString() {
    return toString(" " + FTOR + " ");
  }
}
