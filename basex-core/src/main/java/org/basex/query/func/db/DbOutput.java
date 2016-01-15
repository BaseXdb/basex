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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DbOutput extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(qc.resources.updates().mod instanceof TransformModifier) throw BASX_DBTRANSFORM.get(info);
    final Iter iter = qc.iter(exprs[0]);
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      qc.resources.cache.add(it);
    }
    return null;
  }
}
