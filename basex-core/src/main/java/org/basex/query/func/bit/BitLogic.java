package org.basex.query.func.bit;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Bitwise logical function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class BitLogic extends BitFn {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Long value1 = toLongOrNull(arg(0), qc);
    if(value1 == null) return Empty.VALUE;
    final Long value2 = toLongOrNull(arg(1), qc);
    return value2 == null ? Empty.VALUE : Itr.get(combine(value1, value2));
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr value1 = arg(0), value2 = arg(1);
    final Expr expr = optPair(value1, value2, cc);
    if(expr != this) return expr;

    // bit:or($i, 0) → $i
    if(identity(value2) && integer(value1)) return value1;
    if(identity(value1) && integer(value2)) return value2;
    return this;
  }

  /**
   * Checks if an expression is the identity value.
   * @param expr expression
   * @return result of check
   */
  private boolean identity(final Expr expr) {
    return expr instanceof final Itr itr && itr.itr() == identity();
  }

  /**
   * Returns the value that leaves the other operand unchanged.
   * @return identity value
   */
  abstract long identity();

  /**
   * Combines two values.
   * @param value1 first value
   * @param value2 second value
   * @return result
   */
  abstract long combine(long value1, long value2);
}
