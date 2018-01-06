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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnLocalNameFromQName extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final QNm nm = toQNm(exprs[0], qc, true);
    return nm == null ? null : AtomType.NCN.cast(Str.get(nm.local()), qc, sc, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
