package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJvalue extends ContextFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final JNode jnode = toJNodeOrNull(context(qc), qc);
    return jnode != null ? jnode.value : Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // jvalue(jtree(E)) → E
    final Expr input = arg(0);
    return JTREE.is(input) ? input.arg(0) : this;
  }
}
