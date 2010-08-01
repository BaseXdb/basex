package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.FTItem;
import org.basex.query.item.SeqType;
import org.basex.query.iter.FTIter;
import org.basex.query.util.Var;

/**
 * This class defines is an abstract class for full-text expressions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class FTExpr extends ParseExpr {
  /** Expression list. */
  protected final FTExpr[] expr;

  /**
   * Constructor.
   * @param i query info
   * @param e expression
   */
  protected FTExpr(final QueryInfo i, final FTExpr... e) {
    super(i);
    expr = e;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    ctx.ftfast = false;
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    return this;
  }

  /**
   * This method is called by the sequential full-text evaluation.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTItem atomic(final QueryContext ctx) throws QueryException;

  /**
   * This method is called by the index-based full-text evaluation.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTIter iter(final QueryContext ctx) throws QueryException;

  @Override
  public final boolean uses(final Use u, final QueryContext ctx) {
    for(final FTExpr e : expr) if(e.uses(u, ctx)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var v, final QueryContext ctx) {
    for(final Expr e : expr) if(!e.removable(v, ctx)) return false;
    return true;
  }

  @Override
  public FTExpr remove(final Var v) {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].remove(v);
    return this;
  }

  @Override
  public final SeqType returned(final QueryContext ctx) {
    return SeqType.BLN;
  }

  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].indexEquivalent(ic);
    return this;
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
