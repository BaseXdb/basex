package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnCeiling extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANum value = toNumberOrNull(exprs[0], qc);
    return value == null ? Empty.VALUE : value.ceiling();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
