package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.FTNodeItem;
import org.basex.query.xquery.iter.FTNodeIter;
import org.basex.query.xquery.util.Var;

/**
 * This class defines is an abstract class for full-text expressions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class FTExpr extends Expr {
  /** Expression list. */
  public FTExpr[] expr;
  
  /**
   * Constructor.
   * @param e expression
   */
  public FTExpr(final FTExpr... e) {
    expr = e;
  }

  @Override
  public FTExpr comp(final XQContext ctx) throws XQException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    return this;
  }

  @Override
  public abstract FTNodeIter iter(final XQContext ctx) throws XQException;

  @Override
  public boolean usesPos(final XQContext ctx) {
    for(final FTExpr e : expr) if(e.usesPos(ctx)) return true;
    return false;
  }

  @Override
  public boolean usesVar(final Var v) {
    for(final FTExpr e : expr) if(e.usesVar(v)) return true;
    return false;
  }

  @Override
  public FTExpr removeVar(final Var v) {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].removeVar(v);
    return this;
  }

  @Override
  @SuppressWarnings("unused")
  public FTExpr indexEquivalent(final XQContext ctx, final IndexContext ic)
      throws XQException {
    BaseX.notexpected();
    return null;
  }

  @Override
  public String color() {
    return "66FF66";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final FTExpr e : expr) e.plan(ser);
    ser.closeElement();
  }
  
  /**
   * Prints the array with the specified separator.
   * @param sep separator
   * @return string representation
   */
  protected final String toString(final Object sep) {
    final StringBuilder sb = new StringBuilder();
    for(int e = 0; e != expr.length; e++) {
      sb.append((e != 0 ? sep.toString() : "") + expr[e]);
    }
    return sb.toString();
  }
  
  /**
   * Returns a scoring iterator.
   * @param s scoring
   * @return iterator
   */
  protected FTNodeIter score(final double s) {
    return new FTNodeIter() {
      private boolean more;
      @Override
      public FTNodeItem next() {
        more ^= true;
        if(!more) return null;
        final FTNodeItem ftn = new FTNodeItem();
        ftn.score(s);
        return ftn;
      }
      @Override
      public long size() { return 1; }
    };
  }
}
