package org.basex.query.func.prof;

import static org.basex.util.Token.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ProfTime extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // create timer
    final Performance p = new Performance();
    return value(qc, () -> token(p.getTime()));
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return adoptType(exprs[0]);
  }

  @Override
  public boolean ddo() {
    return exprs[0].ddo();
  }

  /**
   * Profiles the argument.
   * @param qc query context
   * @param func profiling function
   * @return value
   * @throws QueryException query exception
   */
  final Value value(final QueryContext qc, final Supplier<byte[]> func) throws QueryException {
    final byte[] label = exprs.length > 1 ? toToken(exprs[1], qc) : null;
    final Value value = exprs[0].value(qc);
    FnTrace.trace(func.get(), label, qc);
    return value;
  }
}
