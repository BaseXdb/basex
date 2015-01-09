package org.basex.query.func.unit;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class UnitAssert extends UnitFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = exprs.length < 2 ? null : toItem(exprs[1], qc);
    if(exprs[0].ebv(qc, info).bool(info)) return null;
    throw error(it);
  }
}
