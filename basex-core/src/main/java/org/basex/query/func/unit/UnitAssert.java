package org.basex.query.func.unit;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UnitAssert extends UnitFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(exprs[0].ebv(qc, info).bool(info)) return Empty.VALUE;
    throw error(exprs.length > 1 ? toNodeOrAtomItem(1, qc) : null);
  }
}
