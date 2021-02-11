package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
abstract class SqlFn extends StandardFunc {
  /**
   * Returns a prepared statement.
   * @param qc query context
   * @return prepared statement
   * @throws QueryException query exception
   */
  final PreparedStatement prepared(final QueryContext qc) throws QueryException {
    return (PreparedStatement) conn(qc, 2, false);
  }

  /**
   * Returns a connection.
   * @param qc query context
   * @return connection
   * @throws QueryException query exception
   */
  final Connection connection(final QueryContext qc) throws QueryException {
    return (Connection) conn(qc, 1, false);
  }

  /**
   * Returns a connection or prepared statement.
   * @param qc query context
   * @param close close connection
   * @return connection
   * @throws QueryException query exception
   */
  final AutoCloseable get(final QueryContext qc, final boolean close) throws QueryException {
    return conn(qc, 0, close);
  }

  /**
   * Returns the JDBC connection handler.
   * @param qc query context
   * @return connection handler
   */
  static JDBCConnections jdbc(final QueryContext qc) {
    return qc.resources.index(JDBCConnections.class);
  }

  /**
   * Returns a connection or prepared statement.
   * @param qc query context
   * @param mode mode (0: all; 1: only connections; 2: only prepared statements)
   * @param close close connection
   * @return connection
   * @throws QueryException query exception
   */
  private AutoCloseable conn(final QueryContext qc, final int mode, final boolean close)
      throws QueryException {

    final JDBCConnections conns = jdbc(qc);
    final Uri id = (Uri) checkType(exprs[0], qc, AtomType.ANY_URI);
    final AutoCloseable ac = conns.get(id);
    switch(mode) {
      case 1:
        if(!(ac instanceof Connection)) throw SQL_ID1_X.get(info, id);
        break;
      case 2:
        if(!(ac instanceof PreparedStatement)) throw SQL_ID2_X.get(info, id);
        break;
      default:
        if(ac == null) throw SQL_ID1_X.get(info, id);
    }
    if(close) conns.remove(id);
    return ac;
  }
}
