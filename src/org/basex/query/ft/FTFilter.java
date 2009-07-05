package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.FTMatch;
import org.basex.data.FTMatches;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.util.Tokenizer;
import org.basex.util.Tokenizer.FTUnit;

/**
 * Abstract FTFilter expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class FTFilter extends FTExpr {
  /** Optional unit. */
  protected FTUnit unit = FTUnit.WORD;

  /**
   * Constructor.
   * @param e expression
   */
  FTFilter(final FTExpr e) {
    super(e);
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTItem it = expr[0].atomic(ctx);
    filter(ctx, it, ctx.fttoken);
    return it;
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    final FTIter ir = expr[0].iter(ctx);

    return new FTIter() {
      @Override
      public FTItem next() throws QueryException {
        FTItem it;
        while((it = ir.next()) != null) {
          if(filter(ctx, it, content() ? new Tokenizer(it.str()) : null)) break;
        }
        return it;
      }
    };
  }

  /**
   * Evaluates the position filters.
   * @param ctx query context
   * @param item input node
   * @param ft tokenizer
   * @return result of check
   * @throws QueryException query exception
   */
  boolean filter(final QueryContext ctx, final FTItem item, final Tokenizer ft)
      throws QueryException {

    final FTMatches all = item.all;
    for(int a = 0; a < all.size; a++) {
      if(!filter(ctx, all.match[a], ft)) all.delete(a--);
    }
    return all.size != 0;
  }

  /**
   * Evaluates the filter expression.
   * @param ctx query context
   * @param m full-text match
   * @param ft tokenizer
   * @return result of check
   * @throws QueryException query exception
   */
  protected abstract boolean filter(final QueryContext ctx, final FTMatch m,
      final Tokenizer ft) throws QueryException;

  /**
   * Checks if the filter needs the whole text node to be parsed.
   * Is overwritten by some filters to perform other checks.
   * @return result of check
   */
  protected boolean content() {
    return unit != FTUnit.WORD;
  }

  /**
   * Calculates a position value, dependent on the specified unit.
   * @param p word position
   * @param ft tokenizer
   * @return new position
   */
  protected final int pos(final int p, final Tokenizer ft) {
    // ft can be zero if unit is WORD
    return unit == FTUnit.WORD ? p : ft.pos(p, unit);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    return expr[0].indexAccessible(ic);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr[0] + " ";
  }
}
