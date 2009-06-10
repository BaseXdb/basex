package org.basex.query.ft;

import org.basex.ft.Tokenizer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.query.iter.Iter;

/**
 * Sequential FTContains expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTContainsSIndex extends FTContains {
  /** Flag for visualizing ftdata. */ 
  private final boolean vis;
  /** Current node item. */
  FTItem ftn;
  /** Node iterator. */
  FTIter fti;
  
  /**
   * Constructor.
   * @param e contains, select and optional ignore expression
   * @param fte full-text expression
   * @param v visualize ft results
   */
  public FTContainsSIndex(final Expr e, final FTExpr fte, final boolean v) {
    super(e, fte);
    vis = v;
  }

  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {    
    final Iter ir = expr.iter(ctx);
    final Tokenizer tmp = ctx.fttoken;
    ctx.fttoken = ft;
        
    if(fti == null) fti = ftexpr.iter(ctx);
    if(ftn == null) ftn = fti.next();

    double d = 0;
    DBNode n;
    while((n = (DBNode) ir.next()) != null) {
      n.score(1);

      while(ftn != null && !ftn.empty() && n.pre > ftn.fte.pre) {
        ftn = fti.next();
      }

      if(ftn != null) {
        final boolean not = ftn.fte.not;
        if(ftn.fte.pre == n.pre) {
          ftn = null;
          d = not ? 0 : n.score();
          break;
        }
        if(not) {
          d = n.score();
          break;
        }
      } else {
        fti = null;
      }
    }
    ctx.fttoken = tmp;

    // add entry to visualization
    if(ctx.ftpos != null && vis && d != 0 && ftn != null &&
        ftn.fte.poi != null) ctx.ftpos.add(ftn.fte);
    
    return Bln.get(d);
  }

  @Override
  public String toString() {
    return expr + " ftcontainsSI " + ftexpr;
  }
}
