package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.path.AxisPath;
import org.basex.query.util.Var;

/**
 * Abstract array expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    return this;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    for(final Expr e : expr) if(e.uses(u, ctx)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var v, final QueryContext ctx) {
    for(final Expr e : expr) if(!e.removable(v, ctx)) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].remove(v);
    return this;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    for(int e = 0; e < expr.length; e++) expr[e] = expr[e].indexEquivalent(ic);
    return this;
  }

  /**
   * Checks if this expression has a path and an item as arguments.
   * @param num number flag
   * @return result of check
   */
  final boolean standard(final boolean num) {
    return expr.length == 2  && expr[0] instanceof AxisPath && expr[1].i() &&
      (num ? ((Item) expr[1]).n() : expr[1] instanceof Str);
  }

  @Override
  public boolean duplicates(final QueryContext ctx) {
    for(final Expr e : expr) if(e.duplicates(ctx)) return true;
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr e : expr) e.plan(ser);
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
