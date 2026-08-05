package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnSeconds extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    return value.isEmpty() ? value : new DTDur(checkType(value, BasicType.DECIMAL, qc).dec(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr value = arg(0);
    return value.seqType().zero() ? value : this;
  }
}
