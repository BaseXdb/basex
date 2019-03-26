package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class ProfVoid extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].iter(qc);

    // caches items; ensures that lazy items will be evaluated
    for(Item item; (item = qc.next(iter)) != null;) item.cache(false, info);
    return Empty.VALUE;
  }
}
