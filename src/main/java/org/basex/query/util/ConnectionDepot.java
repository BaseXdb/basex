package org.basex.query.util;

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
   * Returns connection or prepared statement with the given id.
   * @param id id
   * @return connection or prepared statement
   */
  public Object get(final int id) {
    return conns.get(id);
  }
}
