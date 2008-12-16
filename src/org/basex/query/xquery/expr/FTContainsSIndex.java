package org.basex.query.xquery.expr;

import org.basex.index.FTTokenizer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.FTNodeItem;
import org.basex.query.xquery.iter.FTNodeIter;
import org.basex.query.xquery.iter.Iter;

/**
 * Sequential FTContains expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContainsSIndex extends FTContains {
  /** Current FTNodeItem. */
  public FTNodeItem ftn;
  
  /**
   * Constructor.
   * @param ex contains, select and optional ignore expression
   */
  FTContainsSIndex(final Expr... ex) {
    super(ex);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {    
    final Iter ir = ctx.iter(expr[0]);
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;
        
    final FTNodeIter fti = (FTNodeIter) ctx.iter(expr[1]);
    if(ftn == null) 
      ftn = fti.next();

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
    return Bln.get(d).iter();
  }
  /*
  @Override
  public Iter iter(final XQContext ctx) throws XQException {    
    final Iter ir = ctx.iter(expr[0]);
    final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;
        
    final FTNodeIter fti = (FTNodeIter) ctx.iter(expr[1]);
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
    return Bln.get(d).iter();
  }
*/
  @Override
  public String toString() {
    return toString(" ftcontainsSI ");
  }
}
