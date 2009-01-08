package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.util.Var;

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
  public Expr comp(final XQContext ctx) throws XQException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    return this;
  }

  @Override
  public boolean usesPos() {
    for(final Expr e : expr) if(e.usesPos()) return true;
    return false;
  }

  @Override
  public boolean usesVar(final Var v) {
    for(final Expr e : expr) if(e.usesVar(v)) return true;
    return false;
  }

  @Override
  public Type returned() {
    return null;
  }

  @Override
  public Expr indexEquivalent(final XQContext ctx, final IndexContext ic)
      throws XQException {

    for(int e = 0; e < expr.length; e++) {
      expr[e] = expr[e].indexEquivalent(ctx, ic);
    }
    return this;
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
