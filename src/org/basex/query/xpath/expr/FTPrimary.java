package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.OPTFTINDEX;

import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.internal.FTIndex;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Token;

/**
 * Fulltext primary expression and FTTimes.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTPrimary extends FTArrayExpr {
  /** Query context. */
  XPContext ctx;

  /**
   * Constructor.
   * @param e expressions
   * @param ftps FTPositionFilter
   */
  public FTPrimary(final Expr[] e, final FTPositionFilter ftps) {
    exprs = e;
    ftpos = ftps;
  }

  /**
   * Constructor.
   * @param e expressions
   */
  public FTPrimary(final Expr[] e) {
    exprs = e;
  }


  @Override
  public NodeSet eval(final XPContext context) {
    // not used
    return null;
  }

  @Override
  public Expr compile(final XPContext context) {
    ctx = context;
    final byte[] lit = ((Literal)  exprs[0]).str();
    ctx.compInfo(OPTFTINDEX);
    
    if (fto != null) {
      if (Token.indexOf(lit, ' ') > 0) {
        return  new FTIndex(lit, fto, false, false); 
      } else {
        return  new FTIndex(lit, fto, false, true);
      }
    }
    return null;
    
  }

  @Override
  public Expr indexEquivalent(final XPContext context, final Step curr) {
    return this;
  }

  @Override
  public int indexSizes(final XPContext context, 
      final Step curr, final int min) {
    return 0;
  }
}