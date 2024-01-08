package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnDoc extends FnDocAvailable {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return doc(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    return expr != this ? expr : super.opt(cc);
  }
}
