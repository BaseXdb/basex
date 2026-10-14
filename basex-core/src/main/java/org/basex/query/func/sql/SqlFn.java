package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
abstract class SqlFn extends StandardFunc {
  /** Statement Options. */
  public static class StatementOptions extends Options {
    /** Query timeout. */
    public static final NumberOption TIMEOUT = new NumberOption("timeout", 0);
    /** Return auto-generated keys instead of the update count. */
    public static final BooleanOption GENERATED_KEYS = new BooleanOption("generated-keys", false);
    /** Represent null values as empty columns instead of omitting them. */
    public static final BooleanOption NULL = new BooleanOption("null", false);
  }

  /**
   * Returns a prepared statement.
   * @param qc query context
   * @return prepared statement
   * @throws QueryException query exception
   */
  final PreparedStatement prepared(final QueryContext qc) throws QueryException {
    return (PreparedStatement) conn(qc, true);
  }

  /**
   * Returns a connection.
   * @param qc query context
   * @return connection
   * @throws QueryException query exception
   */
  final Connection connection(final QueryContext qc) throws QueryException {
    return (Connection) conn(qc, false);
  }

  /**
   * Returns a connection or prepared statement and removes it from the connection handler.
   * @param qc query context
   * @return connection
   * @throws QueryException query exception
   */
  final AutoCloseable remove(final QueryContext qc) throws QueryException {
    final Uri id = id(qc);
    final AutoCloseable ac = jdbc(qc).remove(id);
    if(ac == null) throw SQL_ID1_X.get(info, id);
    return ac;
  }

  /**
   * Closes a JDBC resource, ignoring errors.
   * @param ac resource (can be {@code null})
   */
  static void close(final AutoCloseable ac) {
    if(ac == null) return;
    try {
      ac.close();
    } catch(final Exception ex) {
      Util.debug(ex);
    }
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
   * @param prepared prepared statement (otherwise: connection)
   * @return connection or prepared statement
   * @throws QueryException query exception
   */
  private AutoCloseable conn(final QueryContext qc, final boolean prepared) throws QueryException {
    final Uri id = id(qc);
    final AutoCloseable ac = jdbc(qc).get(id);
    if(!(prepared ? ac instanceof PreparedStatement : ac instanceof Connection)) {
      throw (prepared ? SQL_ID2_X : SQL_ID1_X).get(info, id);
    }
    return ac;
  }

  /**
   * Returns the ID of a connection or prepared statement.
   * @param qc query context
   * @return ID
   * @throws QueryException query exception
   */
  private Uri id(final QueryContext qc) throws QueryException {
    return (Uri) checkType(arg(0), BasicType.ANY_URI, qc);
  }
}
