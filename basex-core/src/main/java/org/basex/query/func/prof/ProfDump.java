package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ProfDump extends FnTrace {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    super.value(qc);
    return Empty.SEQ;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // overwrite fn:trace optimization
    return this;
  }
}
