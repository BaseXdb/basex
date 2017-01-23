package org.basex.query.func.sql;

import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * JDBC connections.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Rositsa Shadura
 */
public final class JDBCConnections implements QueryResource {
  /** Last inserted id. */
  private int lastId = -1;
  /** Map with all open connections and prepared statements with unique ids. */
  private final IntObjMap<AutoCloseable> conns = new IntObjMap<>();

  /**
   * Adds a connection or prepared statement.
   * @param ac connection or prepared statement
   * @return connection/prepared statement id
   */
  synchronized int add(final AutoCloseable ac) {
    conns.put(++lastId, ac);
    return lastId;
  }

  /**
   * Returns connection or prepared statement with the given id.
   * @param id id
   * @return connection or prepared statement
   */
  synchronized AutoCloseable get(final int id) {
    return conns.get(id);
  }

  /**
   * Removes either a connection or a prepared statement.
   * @param id connection/prepared statement id
   */
  synchronized void remove(final int id) {
    conns.delete(id);
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
