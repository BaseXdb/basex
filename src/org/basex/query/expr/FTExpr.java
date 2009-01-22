package org.basex.query.expr;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;
import org.basex.query.util.Var;

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
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    return this;
  }

  @Override
  public abstract FTNodeIter iter(final QueryContext ctx) throws QueryException;

  @Override
  public final boolean usesPos(final QueryContext ctx) {
    for(final FTExpr e : expr) if(e.usesPos(ctx)) return true;
    return false;
  }

  @Override
  public final boolean usesVar(final Var v) {
    for(final FTExpr e : expr) if(e.usesVar(v)) return true;
    return false;
  }

  @Override
  public final FTExpr removeVar(final Var v) {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].removeVar(v);
    return this;
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return Return.BLN;
  }

  @Override
  @SuppressWarnings("unused")
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic)
      throws QueryException {
    BaseX.notexpected();
    return null;
  }

  @Override
  public final String color() {
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
  protected final FTNodeIter score(final double s) {
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
