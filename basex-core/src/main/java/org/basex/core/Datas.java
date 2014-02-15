package org.basex.core;

import java.util.*;

import org.basex.data.*;

/**
 * This class organizes all currently opened databases.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Datas {
  /** List of data references. */
  private final ArrayList<Data> list = new ArrayList<Data>();

  /**
   * Pins and returns an existing data reference for the specified database, or
   * returns {@code null}.
   * @param db name of the database
   * @return data reference
   */
  public synchronized Data pin(final String db) {
    for(final Data d : list) {
      if(d.meta.name.equals(db)) {
        pin(d);
        return d;
      }
    }
    return null;
  }

  /**
   * Pins a data reference.
   * @param data data reference
   */
  public synchronized void pin(final Data data) {
    data.pins++;
  }

  /**
   * Unpins a data reference.
   * @param data data reference
   * @return true if reference was removed from the pool
   */
  public synchronized boolean unpin(final Data data) {
    for(int d = 0; d < list.size(); d++) {
      final Data dt = list.get(d);
      if(dt == data) {
        final boolean close = --data.pins == 0;
        if(close) list.remove(d);
        return close;
      }
    }
    return false;
  }

  /**
   * Adds an already pinned data reference to the pool.
   * @param d data reference
   */
  public synchronized void add(final Data d) {
    list.add(d);
  }

  /**
   * Checks if the specified database is pinned.
   * @param db name of the database
   * @return result of check
   */
  synchronized boolean pinned(final String db) {
    for(final Data d : list) if(d.meta.name.equals(db)) return true;
    return false;
  }

  /**
   * Closes all data references.
   */
  synchronized void close() {
    for(final Data d : list) d.close();
    list.clear();
  }

  /**
   * Returns the number of pins for the specified database,
   * or {@code 0} if the database is not opened.
   * @param db name of the database
   * @return number of references
   */
  public synchronized int pins(final String db) {
    for(final Data d : list) {
      if(d.meta.name.equals(db)) return d.pins;
    }
    return 0;
  }
}
