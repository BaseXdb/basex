package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Abstract array expression.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public abstract class Arr extends ParseExpr {
  /** Expression list. */
  public Expr[] expr;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  protected Arr(final InputInfo ii, final Expr... e) {
    super(ii);
    expr = e;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; ++e)
      expr[e] = checkUp(expr[e].comp(ctx), ctx);
    return this;
  }

  @Override
  public boolean uses(final Use u) {
    for(final Expr e : expr) if(e.uses(u)) return true;
    return false;
  }

  @Override
  public boolean uses(final Var v) {
    for(final Expr e : expr) if(e.uses(v)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr e : expr) if(!e.removable(v)) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].remove(v);
    return this;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    for(int e = 0; e < expr.length; ++e) expr[e] = expr[e].indexEquivalent(ic);
    return this;
  }

  /**
   * Returns true if all arguments are values.
   * @return result of check
   */
  protected final boolean values() {
    for(final Expr e : expr) if(!e.value()) return false;
    return true;
  }

  /**
   * Returns true if at least one argument is empty, or will yield 0 results.
   * @return result of check
   */
  protected final boolean oneEmpty() {
    for(final Expr e : expr) if(e.empty()) return true;
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
  protected String toString(final String sep) {
    return new TokenBuilder().addSep(expr, sep).toString();
  }
}
