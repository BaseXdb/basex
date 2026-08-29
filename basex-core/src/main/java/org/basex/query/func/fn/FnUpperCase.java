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
public final class FnUpperCase extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc);
    return Str.get(uc(value.string(info), value.ascii(info)));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // upper-case(upper-case(E)) → upper-case(E)
    return UPPER_CASE.is(arg(0)) ? arg(0) : this;
  }
}
