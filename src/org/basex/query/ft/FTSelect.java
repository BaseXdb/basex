package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;

/**
 * Full-text select expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTSelect extends FTExpr {
  /** Filter array. */
  private final FTFilter[] filter;

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
    for(final FTFilter f : filter) f.comp(ctx);
    return super.comp(ctx);
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTItem it = expr[0].atomic(ctx);
    if(it.score() != 0 && !filter(ctx, it, ctx.fttoken)) it.score(0);
    return it;
  }

  /**
   * Evaluates the position filters.
   * @param ctx query context
   * @param node input node
   * @param ft tokenizer
   * @return result of check
   * @throws QueryException query exception
   */
  boolean filter(final QueryContext ctx, final FTItem node, final Tokenizer ft)
      throws QueryException {
    for(final FTFilter f : filter) if(!f.filter(ctx, node, ft)) return false;
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
    // [SG] check if/which filters can be evaluated by the index variant
    return expr[0].indexAccessible(ic) && filter.length == 0;
  }

  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    expr[0] = expr[0].indexEquivalent(ic);
    return new FTSelectIndex(this);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final FTFilter f : filter) f.plan(ser);
    expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(expr[0]);
    for(final FTFilter f : filter) sb.append(" " + f);
    return sb.toString();
  }
}
