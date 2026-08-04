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
public final class FnIsNaN extends StandardFunc {
  @Override
  protected Bln item(final QueryContext qc) throws QueryException {
    return Bln.get(test(qc, 0));
  }

  @Override
  protected boolean test(final QueryContext qc, final long pos) throws QueryException {
    final Item value = toAtomItem(arg(0), qc);
    return value == Flt.NAN || value == Dbl.NAN;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return arg(0).seqType().instanceOf(Types.DECIMAL_O) ? Bln.FALSE : this;
  }
}
