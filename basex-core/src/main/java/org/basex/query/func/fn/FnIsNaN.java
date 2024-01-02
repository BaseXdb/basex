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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnIsNaN extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = toAtomItem(arg(0), qc);
    return Bln.get(value == Flt.NAN || value == Dbl.NAN);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return arg(0).seqType().instanceOf(SeqType.DECIMAL_O) ? Bln.FALSE : this;
  }
}
