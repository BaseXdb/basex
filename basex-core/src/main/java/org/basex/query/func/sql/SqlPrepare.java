package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class SqlPrepare extends SqlFn {
  @Override
  public Uri value(final QueryContext qc) throws QueryException {
    final Connection conn = connection(qc);
    final String prepStmt = toString(arg(1), qc);
    final StatementOptions options = toOptions(arg(2), new StatementOptions(), qc);
    final boolean keys = options.get(StatementOptions.GENERATED_KEYS);
    // statement is discarded again if it cannot be registered
    PreparedStatement prep = null;
    try {
      // keep prepared statement
      prep = keys ? conn.prepareStatement(prepStmt, Statement.RETURN_GENERATED_KEYS) :
        conn.prepareStatement(prepStmt);
      final Uri uri = jdbc(qc).add(prep, keys);
      prep = null;
      return uri;
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    } finally {
      close(prep);
    }
  }
}
