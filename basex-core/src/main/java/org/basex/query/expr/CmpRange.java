package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Range comparison.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class CmpRange extends Single {
  /** Evaluation flag: atomic evaluation. */
  boolean single;

  /**
   * Constructor.
   * @param expr expression
   * @param info input info (can be {@code null})
   */
  CmpRange(final Expr expr, final InputInfo info) {
    super(info, expr, Types.BOOLEAN_O);
  }

  @Override
  public final Bln value(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected final boolean ebv(final QueryContext qc) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(single) {
      final Item item = expr.item(qc, info);
      return item != Empty.VALUE && inRange(item);
    }

    // pre-evaluate ranges
    if(expr instanceof Range || expr instanceof RangeSeq) {
      final Value value = expr.value(qc);
      final long size = value.size();
      if(size == 0) return false;
      if(size == 1) return inRange((Item) value);
      return inRange((RangeSeq) value);
    }

    // iterative evaluation
    final Iter iter = expr.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(inRange(item)) return true;
    }
    return false;
  }

  /**
   * Checks if the specified item is within the allowed range.
   * @param item item to be checked
   * @return result of check
   * @throws QueryException query exception
   */
  abstract boolean inRange(Item item) throws QueryException;

  /**
   * Checks if at least one value of the specified range sequence is within the allowed range.
   * @param seq range sequence to be checked
   * @return result of check
   * @throws QueryException query exception
   */
  boolean inRange(final RangeSeq seq) throws QueryException {
    for(final Item item : seq) {
      if(inRange(item)) return true;
    }
    return false;
  }

  /**
   * Creates a comparison with the same range and the specified operand.
   * @param operand operand
   * @param cc compilation context
   * @return expression
   * @throws QueryException query exception
   */
  abstract Expr with(Expr operand, CompileContext cc) throws QueryException;
}
