package org.basex.query.ft;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Return;
import org.basex.query.item.FTNodeItem;
import org.basex.query.item.Item;
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
  protected FTExpr(final FTExpr... e) {
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
  public boolean uses(final Use use, final QueryContext ctx) {
    for(final FTExpr e : expr) if(e.uses(use, ctx)) return true;
    return false;
  }

  @Override
  public final FTExpr remove(final Var v) {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].remove(v);
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
  
  /**
   * Returns a scoring iterator.
   * @param s scoring
   * @return iterator
   */
  protected final FTNodeIter score(final double s) {
    return new FTNodeIter() {
      private boolean more;
      @Override
      public FTNodeItem next() { return (more ^= true) ? item() : null; }
      @Override
      public int size() { return 1; }
      @Override
      public Item get(final long i) { return i == 0 ? item() : null; }

      private FTNodeItem item() {
        final FTNodeItem ftn = new FTNodeItem();
        ftn.score(s);
        return ftn;
      }
    };
  }

  /**
   * Checks if sub expressions of a mild not operator
   * do not violate the grammar.
   * @return result of check
   */
  protected boolean usesExclude() {
    for(final FTExpr e : expr) if(e.usesExclude()) return true;
    return false;
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
}
