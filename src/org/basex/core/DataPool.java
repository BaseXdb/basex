package org.basex.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.basex.data.Data;

/**
 * Class for all referenced Data.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class DataPool {
  
  /** Map for Datareferences. */
  public static final HashMap<Data, Integer> DBPOOL = new
  HashMap<Data, Integer>();
  
  
  /**
   * Returns the Data of the Database.
   * @param db Name of the Database.
   * @return Data
   */
  public Data pin(final String db) {
    Iterator<Map.Entry<Data, Integer>> i = DBPOOL.entrySet().iterator();
    while(i.hasNext()) {
      Entry<Data, Integer> next = i.next();
      if(next.getKey().meta.dbname.equals(db)) {
        int j = next.getValue();
        j++;
        DBPOOL.put(next.getKey(), j);
        return next.getKey();
      }
    }
    return null;
  }
  
  /**
   * Removes a Datareference.
   * @param d Data
   */
  public void unpin(final Data d) {
    if(DBPOOL.get(d) == 0) {
      DBPOOL.remove(d);
    } else {
      int i = DBPOOL.get(d);
      i--;
      DBPOOL.put(d, i);
    }
  }
  
  /**
   * Adds a Datareference to the DBPOOL.
   * @param d Data
   */
  public void add(final Data d) {
    DBPOOL.put(d, 1);
  }
}
