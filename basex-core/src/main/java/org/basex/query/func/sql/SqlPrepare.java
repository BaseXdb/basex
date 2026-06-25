package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class SqlPrepare extends SqlFn {
  @Override
  public Uri item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Connection conn = connection(qc);
    final String prepStmt = toString(arg(1), qc);
    final StatementOptions options = toOptions(arg(2), new StatementOptions(), qc);
    final boolean keys = options.get(StatementOptions.GENERATED_KEYS);
    try {
      // keep prepared statement
      final PreparedStatement prep = keys ?
        conn.prepareStatement(prepStmt, Statement.RETURN_GENERATED_KEYS) :
        conn.prepareStatement(prepStmt);
      return jdbc(qc).add(prep, keys);
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
  }
}
