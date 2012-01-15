package org.basex.query.ft;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.FTNode;
import org.basex.query.item.SeqType;
import org.basex.query.iter.FTIter;
import org.basex.query.util.IndexContext;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * This class defines is an abstract class for full-text expressions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class FTExpr extends ParseExpr {
  /** Expression list. */
  public final FTExpr[] expr;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  protected FTExpr(final InputInfo ii, final FTExpr... e) {
    super(ii);
    expr = e;
    type = SeqType.BLN;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].comp(ctx);
    return this;
  }

  /**
   * This method is called by the sequential full-text evaluation.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTNode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * This method is called by the index-based full-text evaluation.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTIter iter(final QueryContext ctx) throws QueryException;

  @Override
  public boolean uses(final Use u) {
    for(final FTExpr e : expr) if(e.uses(u)) return true;
    return false;
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final FTExpr e : expr) c += e.count(v);
    return c;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr e : expr) if(!e.removable(v)) return false;
    return true;
  }

  @Override
  public FTExpr remove(final Var v) {
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].remove(v);
    return this;
  }

  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].indexEquivalent(ic);
    return this;
  }

  /**
   * Checks if sub expressions of a mild not operator violate the grammar.
   * @return result of check
   */
  protected boolean usesExclude() {
    for(final FTExpr e : expr) if(e.usesExclude()) return true;
    return false;
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
    for(int e = 0; e != expr.length; ++e) {
      sb.append((e != 0 ? sep.toString() : "") + expr[e]);
    }
    return sb.toString();
  }
}
