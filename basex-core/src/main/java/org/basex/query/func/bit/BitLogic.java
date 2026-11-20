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
  final Value value(final long value, final QueryContext qc) throws QueryException {
    final Item item = atomItem(arg(1), qc);
    return item == Empty.VALUE ? Empty.VALUE : Itr.get(combine(value, toLong(item, arg(1))));
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr value1 = arg(0), value2 = arg(1);
    final Expr expr = optPair(value1, value2, cc);
    if(expr != this) return expr;

    // bit:or($i, 0) → $i
    final long identity = identity();
    if(literal(value2, identity) && integer(value1)) return value1;
    if(literal(value1, identity) && integer(value2)) return value2;
    return this;
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
