package org.basex.query.func.sql;

import static org.basex.util.Token.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * JDBC connections.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class JDBCConnections implements QueryResource {
  /** Last inserted id. */
  private int lastId = -1;
  /** Map with all open connections and prepared statements with unique ids. */
  private final TokenObjMap<AutoCloseable> conns = new TokenObjMap<>();

  /**
   * Adds a connection.
   * @param conn connection
   * @param url url
   * @return generated id
   */
  synchronized Uri add(final Connection conn, final String url) {
    final byte[] uri = token(url + "/connection-" + ++lastId);
    conns.put(uri, conn);
    return Uri.uri(uri);
  }

  /**
   * Adds a prepared statement.
   * @param stmt prepared statement
   * @return generated id
   * @throws SQLException SQL connection
   */
  synchronized Uri add(final PreparedStatement stmt) throws SQLException {
    final String url = string(get(stmt.getConnection())).replaceAll("^(.+)/.+$", "$1");
    final byte[] uri = token(url + "/statement-" + ++lastId);
    conns.put(uri, stmt);
    return Uri.uri(uri);
  }

  /**
   * Returns connection or prepared statement with the given id.
   * @param id id
   * @return connection, prepared statement or {@code null}
   */
  synchronized AutoCloseable get(final Uri id) {
    return conns.get(id.string());
  }

  /**
   * Removes either a connection or a prepared statement.
   * @param id connection/prepared statement id
   */
  synchronized void remove(final Uri id) {
    conns.remove(id.string());
  }

  /**
   * Returns the key for the specified connection or prepared statement.
   * @param ac connection or prepared statement
   * @return id or {@code null}
   */
  synchronized byte[] get(final AutoCloseable ac) {
    for(final byte[] id : conns) {
      if(conns.get(id) == ac) return id;
    }
    return null;
  }

  @Override
  public void close() {
    for(final AutoCloseable ac : conns.values()) {
      try {
        if(ac != null) ac.close();
      } catch(final Exception ex) {
        Util.debug(ex);
      }
    }
  }
}
