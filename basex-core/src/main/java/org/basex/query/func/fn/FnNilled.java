package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnNilled extends ContextFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XNode node = toNodeOrNull(context(qc), qc);
    return node == null || node.kind() != Kind.ELEMENT ? Empty.VALUE : Bln.FALSE;
  }

  @Override
  protected boolean test(final QueryContext qc, final long pos) throws QueryException {
    // always false, as no schema information is given
    toNodeOrNull(context(qc), qc);
    return false;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(false, false, cc.qc.focus.value);
  }
}
