package org.basex.query.expr.ft;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.index.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Abstract FTFilter expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class FTFilter extends FTExpr {
  /** Unit. */
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
    final FTNode item = exprs[0].item(qc, info);
    filter(qc, item, qc.ftLexer);
    return item;
  }

  @Override
  public final FTIter iter(final QueryContext qc) throws QueryException {
    final FTIter iter = exprs[0].iter(qc);
    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        FTNode it;
        while((it = iter.next()) != null) {
          qc.checkStop();
          // only create lexer if content needs to be parsed
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
   * @param lexer lexer (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean filter(final QueryContext qc, final FTNode item, final FTLexer lexer)
      throws QueryException {

    final FTMatches all = item.matches();
    for(int a = 0; a < all.size(); a++) {
      if(!filter(qc, all.list[a], lexer)) all.remove(a--);
    }
    return !all.isEmpty();
  }

  /**
   * Evaluates the filter expression.
   * @param qc query context
   * @param match full-text match
   * @param lexer lexer (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  protected abstract boolean filter(QueryContext qc, FTMatch match, FTLexer lexer)
      throws QueryException;

  /**
   * Checks if the filter requires the whole text node to be parsed.
   * Is overwritten by {@link FTContent} to perform other checks.
   * @return result of check
   */
  boolean content() {
    return unit != FTUnit.WORDS;
  }

  /**
   * Calculates a position value, dependent on the specified unit.
   * @param pos word position
   * @param lexer tokenizer (can be {@code null} if {@link #unit} is {@link FTUnit#WORDS})
   * @return new position
   */
  final int pos(final int pos, final FTLexer lexer) {
    return unit == FTUnit.WORDS ? pos : lexer.pos(pos, unit);
  }

  @Override
  public final boolean indexAccessible(final IndexInfo ii) throws QueryException {
    return exprs[0].indexAccessible(ii);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof FTFilter && unit == ((FTFilter) obj).unit && super.equals(obj);
  }
}
