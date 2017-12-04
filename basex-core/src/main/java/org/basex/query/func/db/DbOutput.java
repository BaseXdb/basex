package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class DbOutput extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Updates updates = qc.updates();
    if(updates.mod instanceof TransformModifier) throw BASEX_UPDATE.get(info);
    final Iter iter = exprs[0].iter(qc);
    for(Item it; (it = qc.next(iter)) != null;) qc.updates.cache.add(it);
    return null;
  }
}
