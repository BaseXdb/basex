package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnLowerCase extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc);
    return Str.get(lc(value.string(info), value.ascii(info)));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // lower-case(lower-case(E)) → lower-case(E)
    return LOWER_CASE.is(arg(0)) ? arg(0) : this;
  }
}
