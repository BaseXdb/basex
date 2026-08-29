package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnExpandedQName extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final QNm value = toQNmOrNull(arg(0), qc);
    return value == null ? Empty.VALUE : Str.get(QNm.eqName(value.uri(), value.local()));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
