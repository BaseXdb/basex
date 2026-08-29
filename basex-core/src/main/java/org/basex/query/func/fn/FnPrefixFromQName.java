package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnPrefixFromQName extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final QNm value = toQNmOrNull(arg(0), qc);
    return value == null || !value.hasPrefix() ? Empty.VALUE :
      BasicType.NCNAME.cast(Str.get(value.prefix()), qc, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(false, true, null);
  }
}
