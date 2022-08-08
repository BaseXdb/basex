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
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnIsNaN extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = toAtomItem(exprs[0], qc);
    return Bln.get(item == Flt.NAN || item == Dbl.NAN);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final SeqType st = exprs[0].seqType();
    final Type type = st.type;
    return st.one() && !st.mayBeArray() && type.instanceOf(AtomType.DECIMAL) ? Bln.FALSE : this;
  }
}
