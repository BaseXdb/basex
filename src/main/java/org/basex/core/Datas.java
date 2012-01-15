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
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 */
public final class Datas {
  /** List for data and pins. */
  private final List<PData> list =
    Collections.synchronizedList(new ArrayList<PData>());

  /**
   * Pins and returns an existing data reference for the specified database, or
   * returns {@code null}.
   * @param db name of the database
   * @return data reference
   */
  synchronized Data pin(final String db) {
    for(final PData d : list) {
      if(d.data.meta.name.equals(db)) {
        d.pins++;
        return d.data;
      }
    }
    return null;
  }

  /**
   * Unpins a data reference.
   * @param data data reference
   * @return true if reference was removed from the pool
   */
  synchronized boolean unpin(final Data data) {
    for(final PData d : list) {
      if(d.data == data) {
        final boolean close = --d.pins == 0;
        if(close) list.remove(d);
        return close;
      }
    }
    return false;
  }

  /**
   * Checks if the specified database is pinned.
   * @param db name of the database
   * @return result of check
   */
  synchronized boolean pinned(final String db) {
    for(final PData d : list) if(d.data.meta.name.equals(db)) return true;
    return false;
  }

  /**
   * Adds a data reference to the pool.
   * @param d data reference
   */
  synchronized void add(final Data d) {
    list.add(new PData(d));
  }

  /**
   * Returns the number of opened databases.
   * @return number of databases
   */
  public synchronized int size() {
    return list.size();
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
  synchronized void close() {
    for(final PData d : list) {
      try { d.data.close(); } catch(final IOException ex) { Util.debug(ex); }
    }
    list.clear();
  }

  /**
   * Returns the number of pins for the specified database, or 0. Used for
   * testing.
   * @param db name of the database
   * @return number of references
   */
  public synchronized int pins(final String db) {
    for(final PData d : list) {
      if(d.data.meta.name.equals(db)) return d.pins;
    }
    return 0;
  }

  /**
   * Inner class for a data object in the pool.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Andreas Weiler
   */
  private static final class PData {
    /** Number of current database users. */
    int pins = 1;
    /** Data reference. */
    Data data;

    /**
     * Default constructor.
     * @param d data reference
     */
    PData(final Data d) {
      data = d;
    }
  }
}
