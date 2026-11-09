package org.basex.query.func.bit;

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
public final class BitNot extends BitFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Long value = toLongOrNull(arg(0), qc);
    return value == null ? Empty.VALUE : Itr.get(~value);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    if(expr != this) return expr;

    // bit:not(bit:not($i)) → $i
    final Expr value = arg(0);
    return _BIT_NOT.is(value) && integer(value.arg(0)) ? value.arg(0) : this;
  }
}
