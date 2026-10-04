package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnAbs extends NumericFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANum value = toNumberOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;
    if(value instanceof final Itr itr && itr.itr() == Long.MIN_VALUE) {
      throw RANGE_X.get(info, value);
    }
    return value.abs();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // abs(abs(E)) → abs(E)
    return ABS.is(arg(0)) ? arg(0) : super.opt(cc);
  }
}
