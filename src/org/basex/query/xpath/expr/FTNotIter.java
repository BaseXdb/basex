package org.basex.query.xpath.expr;

import org.basex.index.FTNode;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Bln;

/**
 * FTUnaryNotExprs. This expresses the mild combination of ftand and ftnot.
 * The selection A not in B matches a token sequence that matches a, but
 * not when it is part of b.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTNotIter extends FTArrayExpr {
  /** Flag if expression has more results. */
  private boolean m;
  /**
   * Constructor.
   * @param e operands joined with the mild not operator
   */
  public FTNotIter(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public boolean pos() {
    return false;
  }
  
  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    return Bln.get(exprs[0].eval(ctx).bool());
    //return Bln.get(!exprs[0].eval(ctx).bool());
  }
  
  @Override
  public FTNode next(final QueryContext ctx) {
    final FTNode n = m ? exprs[0].next(ctx) : new FTNode(); 
    n.not = true;
    return n; 
  }
  
  @Override 
  public boolean more() {
    m = exprs[0].more();
    return true;
  }
}
