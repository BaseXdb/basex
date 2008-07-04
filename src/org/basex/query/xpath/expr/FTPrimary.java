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

  @Override
  public NodeSet eval(final XPContext context) {
    // not used
    return null;
  }

  @Override
  public Expr compile(final XPContext context) {
    ctx = context;
    final byte[] lit = ((Literal) exprs[0]).str();
    
    if(fto != null && context.ftcount++ == 0) {
      ctx.compInfo(OPTFTINDEX);
      return new FTIndex(lit, fto, false, Token.indexOf(lit, ' ') < 0);
    }
    return exprs[0];
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