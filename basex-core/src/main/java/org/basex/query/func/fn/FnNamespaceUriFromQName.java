package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnNamespaceUriFromQName extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final QNm value = toQNm(exprs[0], true, qc);
    return value == null ? Empty.VALUE : Uri.uri(value.uri());
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
