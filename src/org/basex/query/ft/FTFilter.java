package org.basex.query.ft;

import org.basex.data.FTMatch;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.util.Tokenizer;

/**
 * Abstract FTFilter expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class FTFilter extends Arr {
  /** Units. */
  public enum FTUnit {
    /** Word unit. */      WORD,
    /** Sentence unit. */  SENTENCE,
    /** Paragraph unit. */ PARAGRAPH;

    /**
     * Returns a string representation.
     * @return string representation
     */
    @Override
    public String toString() { return name().toLowerCase(); }
  }

  /** Optional unit. */
  FTUnit unit = FTUnit.WORD;

  /**
   * Evaluates the filter expression.
   * @param ctx query context
   * @param m full-text match
   * @param ft tokenizer
   * @return result of check
   * @throws QueryException query exception
   */
  abstract boolean filter(final QueryContext ctx, final FTMatch m,
      final Tokenizer ft) throws QueryException;

  /**
   * Checks if the filter needs the whole text node to be parsed.
   * Is overwritten by some filters to perform other checks.
   * @return result of check
   */
  boolean content() {
    return unit != FTUnit.WORD;
  }

  /**
   * Calculates a position value, dependent on the specified unit.
   * @param p word position
   * @param ft tokenizer
   * @return new position
   */
  final int pos(final int p, final Tokenizer ft) {
    if(unit == FTUnit.WORD) return p;
    ft.init();
    while(ft.more() && ft.pos != p);
    return unit == FTUnit.SENTENCE ? ft.sent : ft.para;
  }
}
