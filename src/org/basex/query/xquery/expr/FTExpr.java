package org.basex.query.xquery.expr;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;

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
    for(int e = 0; e != expr.length; e++) expr[e] = (FTExpr) ctx.comp(expr[e]);
    return this;
  }

  @Override
  public final boolean uses(final Using u) {
    for(final Expr e : expr) if(e.uses(u)) return true;
    return false;
  }

  @Override
  public String color() {
    return "66FF66";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }
 
  /**
   * Verifies if the fulltext query options comply with the index defaults.
   * Check where this method is overwritten to get more info.
   * @param meta meta data
   * @return result of check
   */
/*  public boolean indexOptions(final MetaData meta) {
    for(final FTExpr e : expr) if(!e.indexOptions(meta)) return false;
    return true;
  }
*/
  
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
