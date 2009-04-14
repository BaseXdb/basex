package org.basex.query.ft;

import org.basex.ft.Tokenizer;
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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTContainsSIndex extends FTContains {
  /** Current FTNodeItem. */
  FTNodeItem ftn;
  /** Flag for visualizing ftdata. */ 
  private final boolean vis;
  
  /**
   * Constructor.
   * @param e contains, select and optional ignore expression
   * @param fte fulltext expression
   * @param v visualize ft results
   */
  public FTContainsSIndex(final Expr e, final FTExpr fte, final boolean v) {
    super(e, fte);
    vis = v;
  }

  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {    
    final Iter ir = expr.iter(ctx);
    final Tokenizer tmp = ctx.ftitem;
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

    if (vis && Bln.get(d).bool() && ftn != null && ctx.ftpos != null &&
        ftn.ftn.ip != null && ftn.ftn.p !=  null && ctx.ftdata != null)
      ctx.ftdata.add(ftn.ftn.ip.finish(), ftn.ftn.p.finish());
    
    return Bln.get(d);
  }

  @Override
  public String toString() {
    return expr + " ftcontainsSI " + ftexpr;
  }
}
