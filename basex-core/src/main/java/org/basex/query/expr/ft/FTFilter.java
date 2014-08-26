package org.basex.query.expr.ft;

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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class FTFilter extends FTExpr {
  /** Optional unit. */
  final FTUnit unit;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   */
  FTFilter(final InputInfo info, final FTExpr expr) {
    this(info, expr, FTUnit.WORDS);
  }

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param unit unit
   */
  FTFilter(final InputInfo info, final FTExpr expr, final FTUnit unit) {
    super(info, expr);
    this.unit = unit;
  }

  @Override
  public final FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FTNode it = exprs[0].item(qc, info);
    filter(qc, it, qc.ftToken);
    return it;
  }

  @Override
  public final FTIter iter(final QueryContext qc) throws QueryException {
    final FTIter ir = exprs[0].iter(qc);
    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        FTNode it;
        while((it = ir.next()) != null) {
          if(filter(qc, it, content() ? new FTLexer().init(it.string(info)) : null)) break;
        }
        return it;
      }
    };
  }

  /**
   * Evaluates the position filters.
   * @param qc query context
   * @param item input node
   * @param lex tokenizer
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean filter(final QueryContext qc, final FTNode item, final FTLexer lex)
      throws QueryException {

    final FTMatches all = item.all;
    for(int a = 0; a < all.size(); a++) {
      if(!filter(qc, all.match[a], lex)) all.delete(a--);
    }
    return !all.isEmpty();
  }

  /**
   * Evaluates the filter expression.
   * @param qc query context
   * @param m full-text match
   * @param ft tokenizer
   * @return result of check
   * @throws QueryException query exception
   */
  protected abstract boolean filter(final QueryContext qc, final FTMatch m, final FTLexer ft)
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
  public final boolean indexAccessible(final IndexInfo ii) throws QueryException {
    return exprs[0].indexAccessible(ii);
  }

  @Override
  public String toString() {
    return exprs[0] + " ";
  }
}
