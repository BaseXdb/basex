package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.FTMatch;
import org.basex.data.FTMatches;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNode;
import org.basex.query.iter.FTIter;
import org.basex.query.util.IndexContext;
import org.basex.util.InputInfo;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTUnit;

/**
 * Abstract FTFilter expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class FTFilter extends FTExpr {
  /** Optional unit. */
  protected FTUnit unit = FTUnit.WORD;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  FTFilter(final InputInfo ii, final FTExpr e) {
    super(ii, e);
  }

  @Override
  public final FTNode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final FTNode it = expr[0].item(ctx, input);
    filter(ctx, it, ctx.fttoken);
    return it;
  }

  @Override
  public final FTIter iter(final QueryContext ctx) throws QueryException {
    final FTIter ir = expr[0].iter(ctx);

    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        FTNode it;
        while((it = ir.next()) != null) {
          if(filter(ctx, it, content() ?
              new FTLexer().init(it.string(input)) : null)) break;
        }
        return it;
      }
    };
  }

  /**
   * Evaluates the position filters.
   * @param ctx query context
   * @param item input node
   * @param lex tokenizer
   * @return result of check
   * @throws QueryException query exception
   */
  final boolean filter(final QueryContext ctx, final FTNode item,
      final FTLexer lex) throws QueryException {

    final FTMatches all = item.all;
    for(int a = 0; a < all.size; ++a) {
      if(!filter(ctx, all.match[a], lex)) all.delete(a--);
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
      final FTLexer ft) throws QueryException;

  /**
   * Checks if the filter requires the whole text node to be parsed.
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
  protected final int pos(final int p, final FTLexer ft) {
    // ft can be zero if unit is WORD
    return unit == FTUnit.WORD ? p : ft.pos(p, unit);
  }

  @Override
  public final boolean indexAccessible(final IndexContext ic)
      throws QueryException {
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
