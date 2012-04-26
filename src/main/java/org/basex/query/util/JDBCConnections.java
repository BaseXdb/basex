package org.basex.query.util;

import java.sql.*;

import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Opened JDBC connections.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class JDBCConnections {
  /** Last inserted id. */
  private int lastId = -1;
  /** Map with all open connections and prepared statements with unique ids. */
  private final IntMap<Object> conns = new IntMap<Object>();

  /**
   * Adds a connection or prepared statement to depot.
   * @param obj connection or prepared statement
   * @return connection/prepared statement id
   */
  public int add(final Object obj) {
    conns.add(++lastId, obj);
    return lastId;
  }

  /**
   * Returns connection or prepared statement with the given id.
   * @param id id
   * @return connection or prepared statement
   */
  public Object get(final int id) {
    return conns.get(id);
  }

  /**
   * Removes either a connection or a prepared statement from the depot.
   * @param id connection/prepared statement id
   */
  public void remove(final int id) {
    conns.delete(id);
  }

  /**
   * Closes all opened connections.
   */
  public void close() {
    for(int i = 0; i < conns.size(); i++) {
      final int key = conns.key(i);
      final Object obj = conns.get(key);
      if(obj != null) {
        try {
          if(obj instanceof Connection) ((Connection) obj).close();
          else ((PreparedStatement) obj).close();
        } catch(final SQLException ex) {
          Util.debug(ex);
        }
      }
    }
  }
}
