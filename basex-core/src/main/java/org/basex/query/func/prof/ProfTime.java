package org.basex.query.func.prof;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class ProfTime extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // create timer
    final Performance p = new Performance();
    return evaluate(qc, () -> p.formatRuntime());
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return adoptType(arg(0));
  }

  @Override
  public final boolean ddo() {
    return arg(0).ddo();
  }

  /**
   * Evaluates the result.
   * @param qc query context
   * @param func profiling function
   * @return value
   * @throws QueryException query exception
   */
  final Value evaluate(final QueryContext qc, final Supplier<String> func) throws QueryException {
    final Value value = arg(0).value(qc);
    final String label = toStringOrNull(arg(1), qc);
    qc.trace(func.get(), label);
    return value;
  }
}
