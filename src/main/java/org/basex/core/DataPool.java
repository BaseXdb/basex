package org.basex.core;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.basex.data.Data;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * This class organizes all currently opened database.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class DataPool {

  /** List for data and pins. */
  List<PData> list = Collections.synchronizedList(new ArrayList<PData>());

  /**
   * Pins and returns an existing data reference for the specified database, or
   * returns null.
   * @param db name of the database
   * @return data reference
   */
  Data pin(final String db) {
    synchronized(list) {
      for(final PData d : list) {
        if(d.data.meta.name.equals(db)) {
          d.pins++;
          return d.data;
        }
      }
      return null;
    }
  }

  /**
   * Unpins a data reference.
   * @param data data reference
   * @return true if reference was removed from the pool
   */
  boolean unpin(final Data data) {
    synchronized(list) {
      for(final PData d : list) {
        if(d.data == data) {
          final boolean close = --d.pins == 0;
          if(close) {
            list.remove(d);
          }
          return close;
        }
      }
      return false;
    }
  }

  /**
   * Checks if the specified database is pinned.
   * @param db name of the database
   * @return result of check
   */
  boolean pinned(final String db) {
    synchronized(list) {
      for(final PData d : list)
        if(d.data.meta.name.equals(db)) return true;
      return false;
    }
  }

  /**
   * Adds a data reference to the pool.
   * @param d data reference
   */
  void add(final Data d) {
    list.add(new PData(d));
  }

  /**
   * Returns information on the opened database instances.
   * @return data reference
   */
  public String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(SRVDATABASES, list.size());
    tb.add(list.size() != 0 ? COL : DOT);
    for(final PData d : list) {
      tb.add(NL + LI + d.data.meta.name + " (" + d.pins + "x)");
    }
    return tb.toString();
  }

  /**
   * Closes all data references.
   */
  void close() {
    synchronized(list) {
      try {
        for(final PData d : list)
          d.data.close();
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }
    list.clear();
  }

  /**
   * Returns the number of pins for the specified database, or 0. Used for
   * testing.
   * @param db name of the database
   * @return number of references
   */
  public int pins(final String db) {
    for(final PData d : list) {
      if(d.data.meta.name.equals(db)) return d.pins;
    }
    return 0;
  }

  /**
   * Inner class for a data object in the pool.
   * 
   * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
   * @author Andreas Weiler
   */
  private final class PData {

    /** Number of current database users. */
    int pins;
    /** Data reference. */
    Data data;

    /**
     * Default constructor.
     * @param d data reference
     */
    PData(final Data d) {
      pins = 1;
      data = d;
    }
  }
}
