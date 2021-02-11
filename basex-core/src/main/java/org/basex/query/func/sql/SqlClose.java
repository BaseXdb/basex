package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class SqlClose extends SqlFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    @SuppressWarnings("resource")
    final AutoCloseable ac = get(qc, true);
    try {
      ac.close();
    } catch(final Exception ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
    return Empty.VALUE;
  }
}
