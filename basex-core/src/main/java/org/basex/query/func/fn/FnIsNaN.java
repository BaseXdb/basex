package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnIsNaN extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final Item value = toAtomItem(arg(0), qc);
    return value == Flt.NAN || value == Dbl.NAN;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return arg(0).seqType().instanceOf(Types.DECIMAL_O) ? Bln.FALSE : this;
  }
}
