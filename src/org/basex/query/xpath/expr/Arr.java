package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;

/**
 * Abstract array expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Arr extends Expr {
  /** Expression list. */
  public Expr[] expr;

  /**
   * Constructor.
   * @param e expression list
   */
  protected Arr(final Expr... e) {
    expr = e;
  }
  
  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    return this;
  }

  @Override
  public final boolean usesPos() {
    for(final Expr e : expr) if(e.usesPos()) return true;
    return false;
  }

  @Override
  public final boolean usesSize() {
    for(final Expr e : expr) if(e.usesSize()) return true;
    return false;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(cmp.getClass() != getClass()) return false;
    final Arr ex = (Arr) cmp;
    if(expr.length != ex.expr.length) return false;
    for(final Expr e : expr) {
      if(!e.sameAs(ex)) return false;
    }
    return true;
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

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }
}
