package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnNamespaceUriFromQName extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final QNm qnm = toQNm(exprs[0], qc, true);
    return qnm == null ? null : Uri.uri(qnm.uri());
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
