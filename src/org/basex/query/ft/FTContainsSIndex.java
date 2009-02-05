package org.basex.query.ft;

import org.basex.index.FTTokenizer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;
import org.basex.query.iter.Iter;

/**
 * Sequential FTContains expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContainsSIndex extends FTContains {
  /** Current FTNodeItem. */
  FTNodeItem ftn;
  
  /**
   * Constructor.
   * @param e contains, select and optional ignore expression
   * @param fte fulltext expression
   */
  public FTContainsSIndex(final Expr e, final FTExpr fte) {
    super(e, fte);
  }

  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {    
    final Iter ir = expr.iter(ctx);
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;
        
    final FTNodeIter fti = ftexpr.iter(ctx);
    if(ftn == null) ftn = fti.next();

    double d = 0;
    DBNode n;
    while((n = (DBNode) ir.next()) != null) {
      n.score(1);
      while(ftn != null && ftn.ftn.size > 0 && n.pre > ftn.ftn.getPre()) {
        ftn = fti.next();
      }
      
      if(ftn != null) {
        final boolean not = ftn.ftn.not;
        if(ftn.ftn.getPre() == n.pre) {
          ftn = null;
          d = not ? 0 : n.score();
          break;
        }
        if(not) {
          d = n.score();
          break;
        }
      }
    }
    ctx.ftitem = tmp;

    if (Bln.get(d).bool() && ftn != null && ctx.ftpos != null &&
        ftn.ftn.ip != null && ftn.ftn.p !=  null && ctx.ftdata != null)
      ctx.ftdata.add(ftn.ftn.ip.finish(), ftn.ftn.p.finish());
    
    return Bln.get(d);
  }

  @Override
  public String toString() {
    return expr + " ftcontainsSI " + ftexpr;
  }
}
