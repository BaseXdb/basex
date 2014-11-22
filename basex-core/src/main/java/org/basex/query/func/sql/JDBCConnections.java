package org.basex.query.func.sql;

import java.sql.*;

import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * JDBC connections.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class JDBCConnections implements DataResources {
  /** Last inserted id. */
  private int lastId = -1;
  /** Map with all open connections and prepared statements with unique ids. */
  private final IntObjMap<Object> conns = new IntObjMap<>();

  /**
   * Adds a connection or prepared statement to depot.
   * @param obj connection or prepared statement
   * @return connection/prepared statement id
   */
  public int add(final Object obj) {
    conns.put(++lastId, obj);
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

  @Override
  public void close() {
    final int is = conns.size();
    for(int i = 0; i < is; i++) {
      final int key = conns.key(i);
      final Object obj = conns.get(key);
      if(obj == null) continue;
      try {
        if(obj instanceof Connection) ((Connection) obj).close();
        else ((Statement) obj).close();
      } catch(final SQLException ex) {
        Util.debug(ex);
      }
    }
  }
}
