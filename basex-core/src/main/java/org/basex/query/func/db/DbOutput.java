package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
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
    cache(qc.iter(exprs[0]), qc.resources.output, qc);
    return null;
  }
}
