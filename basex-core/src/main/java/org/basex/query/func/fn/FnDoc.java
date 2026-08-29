package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDoc extends FnDocAvailable {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return doc(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    return expr != this ? expr : super.opt(cc);
  }
}
