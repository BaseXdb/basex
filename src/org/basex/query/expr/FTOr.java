package org.basex.query.expr;

import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;
import org.basex.query.util.Scoring;
import org.basex.util.IntList;

/**
 * FTOr expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    double d = 0;
    for(final FTExpr e : expr) {
      final FTNodeItem it = e.iter(ctx).next();
      final double s = it.score();
      if(s != 0) d = Scoring.or(d, s);
    }
    return score(d);
  }

  @Override
  public String toString() {
    return toString(" ftor ");
  }
  
  @Override
  public void indexAccessible(final QueryContext ctx, final IndexContext ic)
      throws QueryException {
    final IntList p = new IntList();
    final IntList n = new IntList();
    final int min = ic.is;
    int sum = 0;

    for (int i = 0; i < expr.length; i++) {
      ic.ftnot = false;
      expr[i].indexAccessible(ctx, ic);
      if (!ic.io) return;
      if (!ic.ftnot && ic.is > 0) {
        p.add(i);
        sum += ic.is;
      } else if (ic.ftnot) {
        if (ic.is > 0) n.add(i);
        else {
          ic.iu = false;
          ic.seq = true;
          ic.is = Integer.MAX_VALUE;
          return;
        }
      }
    }
    nex = n.finish();
    pex = p.finish();

    if (pex.length == 0 && nex.length > 0) {
      ic.seq = true;
      ic.is = Integer.MAX_VALUE;
    } else if (nex.length > 0 && pex.length > 0) {
      ic.seq = true;
      ic.is = Integer.MAX_VALUE;
    } else {
      ic.is = sum > min ? min : sum;
    } 
  }
  
  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic)
    throws QueryException {

    for (int i = 0; i < expr.length; i++) {
      expr[i] = expr[i].indexEquivalent(ctx, ic);
    }
    
    if (pex.length == 0) {
      // !A FTOR !B = !(a ftand b)
      for (int i = 0; i < nex.length; i++) {
        expr[nex[i]] = expr[nex[i]].expr[0];
      }
      final FTIntersection fta = new FTIntersection(pex, nex, expr);
      final FTNotIndex ftn = new FTNotIndex(fta);
      return ftn; 
    }

    if (pex.length == 0) return new FTUnion(nex, true, expr);
    else if (nex.length == 0) return new FTUnion(pex, true, expr);
    else if (pex.length == 1 && nex.length == 0) return expr[pex[0]]; 
    else return new FTUnion(gen(), true, expr);

  }
  
  /**
   * Generate sequence for nex.length > 0 && pex.length > 0.
   * @return sequence
   */
  private int[] gen() {
    final int[] r = new int[expr.length];
    for (int i = 0; i < expr.length; i++) r[i] = i;
    return r;
  }
}
