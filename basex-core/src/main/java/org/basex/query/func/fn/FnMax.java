package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnMax extends FnMin {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return minmax(false, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return opt(false, cc);
  }
}
