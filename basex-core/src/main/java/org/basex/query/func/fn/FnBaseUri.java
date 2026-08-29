package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnBaseUri extends ContextFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XNode node = toNodeOrNull(context(qc), qc);
    if(node != null) {
      final Uri uri = node.baseURI(sc().baseURI(), false, info);
      if(uri != null) return uri;
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(false, false, cc.qc.focus.value);
  }
}
