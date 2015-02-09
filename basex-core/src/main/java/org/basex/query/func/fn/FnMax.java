package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnMax extends Num {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return minmax(OpV.LT, qc);
  }
}
