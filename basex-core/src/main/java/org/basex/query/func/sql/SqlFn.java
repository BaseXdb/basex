package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.func.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Rositsa Shadura
 */
abstract class SqlFn extends StandardFunc {
  /**
   * Returns a connection.
   * @param qc query context
   * @return connection
   * @throws QueryException query exception
   */
  final Connection connection(final QueryContext qc) throws QueryException {
    final int id = (int) toLong(exprs[0], qc);
    final Object ac = jdbc(qc).get(id);
    if(ac instanceof Connection) return (Connection) ac;
    throw BXSQ_CONN_X.get(info, id);
  }

  /**
   * Returns the JDBC connection handler.
   * @param qc query context
   * @return connection handler
   */
  static JDBCConnections jdbc(final QueryContext qc) {
    return qc.resources.index(JDBCConnections.class);
  }
}
