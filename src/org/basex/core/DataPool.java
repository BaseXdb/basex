package org.basex.core;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.util.Array;

/**
 * Class for all referenced Data.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class DataPool {
  /** Data references. */
  private Data[] data = new Data[1];
  /** Number of opened database for each reference. */
  private int[] pins = new int[1];
  /** Number of data references. */
  private int size = 0;

  /**
   * Returns an existing data reference for the specified database, or null.
   * @param db name of the database
   * @return data reference
   */
  public Data pin(final String db) {
    for(int i = 0; i < size; i++) {
      if(data[i].meta.name.equals(db)) {
        pins[i]++;
        return data[i];
      }
    }
    return null;
  }

  /**
   * Unpins a data reference.
   * @param d data reference
   * @return true if reference was removed from the pool
   */
  public boolean unpin(final Data d) {
    // ignore main memory database instances
    if(d instanceof MemData) return false;
    
    for(int i = 0; i < size; i++) {
      if(data[i] == d) {
        final boolean close = --pins[i] == 0;
        if(close) {
          Array.move(data, i + 1, -1, size - i - 1);
          Array.move(pins, i + 1, -1, size - i - 1);
          size--;
        }
        return close;
      }
    }
    // [AW] later: this should not happen anymore, as all data instances
    // to be unpinned should be referenced in the data pool.
    return false;
  }

  /**
   * Checks if the specified database is pinned.
   * @param db name of the database
   * @return result of check
   */
  public boolean pinned(final String db) {
    for(int i = 0; i < size; i++) {
      if(data[i].meta.name.equals(db)) return true;
    }
    return false;
  }

  /**
   * Adds a data reference to the pool.
   * @param d data reference
   */
  public void add(final Data d) {
    // ignore main memory database instances
    if(d instanceof MemData) return;

    if(size == data.length) {
      data = Array.extend(data);
      pins = Array.extend(pins);
    }
    data[size] = d;
    pins[size] = 1;
    size++;
  }
}
