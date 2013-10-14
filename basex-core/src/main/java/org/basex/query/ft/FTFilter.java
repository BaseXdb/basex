package org.basex.query.ft;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Abstract FTFilter expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class FTFilter extends FTExpr {
  /** Optional unit. */
  protected final FTUnit unit;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  protected FTFilter(final InputInfo ii, final FTExpr e) {
    this(ii, e, FTUnit.WORDS);
  }

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param u unit
   */
  protected FTFilter(final InputInfo ii, final FTExpr e, final FTUnit u) {
    super(ii, e);
    unit = u;
  }

  @Override
  public final FTNode item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final FTNode it = expr[0].item(ctx, info);
    filter(ctx, it, ctx.ftToken);
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
          if(filter(ctx, it, content() ? new FTLexer().init(it.string(info)) : null)) break;
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
  final boolean filter(final QueryContext ctx, final FTNode item, final FTLexer lex)
      throws QueryException {

    final FTMatches all = item.all;
    for(int a = 0; a < all.size(); a++) {
      if(!filter(ctx, all.match[a], lex)) all.delete(a--);
    }
    return !all.isEmpty();
  }

  /**
   * Evaluates the filter expression.
   * @param ctx query context
   * @param m full-text match
   * @param ft tokenizer
   * @return result of check
   * @throws QueryException query exception
   */
  protected abstract boolean filter(final QueryContext ctx, final FTMatch m, final FTLexer ft)
      throws QueryException;

  /**
   * Checks if the filter requires the whole text node to be parsed.
   * Is overwritten by some filters to perform other checks.
   * @return result of check
   */
  boolean content() {
    return unit != FTUnit.WORDS;
  }

  /**
   * Calculates a position value, dependent on the specified unit.
   * @param p word position
   * @param ft tokenizer
   * @return new position
   */
  final int pos(final int p, final FTLexer ft) {
    // ft can be zero if unit is WORDS
    return unit == FTUnit.WORDS ? p : ft.pos(p, unit);
  }

  @Override
  public final boolean indexAccessible(final IndexCosts ic) throws QueryException {
    return expr[0].indexAccessible(ic);
  }

  @Override
  public String toString() {
    return expr[0] + " ";
  }
}
