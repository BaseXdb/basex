package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNode;
import org.basex.util.IntList;

/**
 * FTSelect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTSelect extends FTExpr {
  /** Filter array. */
  private final FTFilter[] filter;
  /** Input token. */
  Tokenizer ft;

  /**
   * Constructor.
   * @param e expression
   * @param f filters
   */
  public FTSelect(final FTExpr e, final FTFilter... f) {
    super(e);
    filter = f;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    for(final FTFilter f : filter) {
      f.comp(ctx);
      f.sel = this;
    }
    return super.comp(ctx);
  }

  @Override
  public FTNode atomic(final QueryContext ctx) throws QueryException {
    final FTSelect tmp = ctx.ftselect;
    ctx.ftselect = this;

    ft = ctx.fttoken;
    final FTNode it = expr[0].atomic(ctx);
    ctx.ftselect = tmp;

    double s = it.score();
    if(s != 0 && !filter(ctx, it)) s = 0;

    if(s == 0) it.pos = new IntList[] {};
    it.score(s);
    return it;
  }

  /**
   * Evaluates the position filters.
   * @param ctx query context
   * @param node input node
   * @return result of check
   * @throws QueryException query exception
   */
  boolean filter(final QueryContext ctx, final FTNode node)
      throws QueryException {
    for(final FTFilter f : filter) if(!f.filter(ctx, node)) return false;
    return true;
  }

  /**
   * Returns true if no position filters are specified.
   * @return result of check
   */
  boolean standard() {
    return filter.length == 0;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // [SG] are all position filters supported by the index?
    return expr[0].indexAccessible(ic) && filter.length == 0;
  }

  @Override
  public FTExpr indexEquivalent(final IndexContext ic)throws QueryException {
    expr[0] = expr[0].indexEquivalent(ic);
    return new FTSelectIndex(this);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final FTFilter f : filter) f.plan(ser);
    if(expr[0] != null) expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(expr[0] != null) sb.append(expr[0]);
    for(final FTFilter f : filter) sb.append(" " + f);
    return sb.toString();
  }
}
