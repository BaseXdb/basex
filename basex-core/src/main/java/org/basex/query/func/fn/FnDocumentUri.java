package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDocumentUri extends ContextFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XNode node = toNodeOrNull(context(qc), qc);
    final byte[] uri = node != null ? node.documentURI() : Token.EMPTY;
    return uri.length == 0 ? Empty.VALUE : Uri.get(uri, false);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(false, false, cc.qc.focus.value);
  }
}
