package org.basex.query.util;

import static org.basex.query.util.Err.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.basex.query.QueryException;
import org.basex.util.hash.IntMap;

/**
 * Connection depot.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class ConnectionDepot {

  /** ID counter. */
  private int counter;
  /**
   * Map containing all opened connections and prepared statements with unique
   * ids.
   */
  private IntMap<Object> conns;

  /** Constructor. */
  public ConnectionDepot() {
    counter = 0;
    conns = new IntMap<Object>();
  }

  /**
   * Adds a connection or prepared statement to depot.
   * @param obj connection or prepared statement
   * @return connection/prepared statement id
   */
  public int add(final Object obj) {
    conns.add(counter, obj);
    return counter++;
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
   * @throws QueryException query exception
   */
  public void closeAll() throws QueryException {
    for(int i = 0; i < conns.size(); i++) {
      final int key = conns.key(i);
      final Object obj = conns.get(key);
      if(obj != null) {
        try {
          if(obj instanceof Connection) ((Connection) obj).close();
          else ((PreparedStatement) obj).close();
        } catch(final SQLException ex) {
          throw SQLEXC.thrw(null, ex.getMessage());
        }
      }
    }
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
   * Returns the number of opened connections and prepared statements.
   * @return result
   */
  public int size() {
    return conns.size();
  }
}
