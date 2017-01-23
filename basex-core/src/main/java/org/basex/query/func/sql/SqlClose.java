package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Rositsa Shadura
 */
public final class SqlClose extends SqlFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    final int id = (int) toLong(exprs[0], qc);
    final JDBCConnections jdbc = jdbc(qc);
    try(AutoCloseable sql = jdbc.get(id)) {
      // try-with-resources: resource will automatically be closed
      jdbc.remove(id);
    } catch(final Exception ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
    return null;
  }
}
