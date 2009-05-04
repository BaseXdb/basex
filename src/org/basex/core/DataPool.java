package org.basex.core;

import org.basex.data.Data;
import org.basex.util.Array;

/**
 * Class for all referenced Data.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class DataPool {

  /** Datareferences. */
  private Data[] data = new Data[8];
  /** Number of opened DB for each reference. */
  private int[] pins = new int[8];
  /** Number of Datareferences. */
  private int size = 0;

  /**
   * Returns the Data of the Database.
   * @param db Name of the Database.
   * @return Data
   */
  public Data pin(final String db) {
    for(int i = 0; i < size; i++) {
      if(data[i].meta.dbname.equals(db)) {
        pins[i]++;
        return data[i];
      }
    }
    return null;
  }

  /**
   * Removes a Datareference.
   * @param d Data
   */
  public void unpin(final Data d) {
    for(int i = 0; i < size; i++) {
      if(data[i].equals(d)) {
        pins[i]--;
        if(pins[i] == 0) {
          Array.move(data, i + 1, -1, size - i);
          Array.move(pins, i + 1, -1, size - i);
        }
      }
    }
  }

  /**
   * Adds a Datareference to the DBPOOL.
   * @param d Data
   */
  public void add(final Data d) {
    if(size == data.length) {
      data = Array.extend(data);
      pins = Array.extend(pins);
    }
    data[size] = d;
    pins[size] = 1;
    size++;
  }
}
