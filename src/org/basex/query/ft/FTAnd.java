package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.util.IntList;

/**
 * FTAnd expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTAnd extends FTExpr {
  /** Saving index of positive expressions. */
  private int[] pex;
  /** Saving index of negative (ftnot) expressions. */
  private int[] nex;
  
  /**
   * Constructor.
   * @param e expression list
   */
  public FTAnd(final FTExpr[] e) {
    super(e);
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    FTItem it = null; 
    double s = 0;
    for(final FTExpr e : expr) {
      it = e.atomic(ctx).union(it);
      final double d = s;
      s = it.score();
      if(s == 0) break;
      s = ctx.score.and(s, d);
    }
    it.score(s);
    return it;
  }
  
  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    final IntList ip = new IntList();
    final IntList in = new IntList();
    int nmin = ic.is;
    for(int i = 0; i < expr.length; i++) {
      final boolean ftnot = ic.ftnot;
      ic.ftnot = false;
      final boolean ia = expr[i].indexAccessible(ic);
      final boolean ftn = ic.ftnot;
      ic.ftnot = ftnot;
      if(!ia) return false;
      
      if(!ftn) {
        ip.add(i);
        nmin = ic.is < nmin ? ic.is : nmin;
      } else if(ic.is > 0) {
        in.add(i);
      }
    }
    pex = ip.finish();
    nex = in.finish();
    ic.seq |= ip.size == 0;
    ic.is = ip.size > 0 ? nmin : Integer.MAX_VALUE;
    return true;
  }
  
  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    for(int i = 0; i < expr.length; i++) expr[i] = expr[i].indexEquivalent(ic);

    if(pex.length == 0) {
      // !A FTAnd !B = !(a ftor b)
      for(int i = 0; i < expr.length; i++) expr[i] = expr[i].expr[0];
      return new FTNotIndex(new FTUnion(nex, false, expr));
    }
    return new FTIntersection(pex, nex, expr);
  }

  @Override
  public String toString() {
    return toString(" " + FTAND + " ");
  }
}
