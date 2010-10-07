package org.basex.core;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.data.Data;
import org.basex.util.Array;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * This class organizes all currently opened database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class DataPool {
  /** Data references. */
  private Data[] data = new Data[1];
  /** Number of current database users. */
  private int[] pins = new int[1];
  /** Number of data references. */
  private int size;

  /**
   * Pins and returns an existing data reference for the specified
   * database, or returns null.
   * @param db name of the database
   * @return data reference
   */
  Data pin(final String db) {
    for(int i = 0; i < size; ++i) {
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
  boolean unpin(final Data d) {
    for(int i = 0; i < size; ++i) {
      if(data[i] == d) {
        final boolean close = --pins[i] == 0;
        if(close) {
          Array.move(data, i + 1, -1, size - i - 1);
          Array.move(pins, i + 1, -1, size - i - 1);
          --size;
        }
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
  boolean pinned(final String db) {
    for(int i = 0; i < size; ++i) if(data[i].meta.name.equals(db)) return true;
    return false;
  }

  /**
   * Adds a data reference to the pool.
   * @param d data reference
   */
  void add(final Data d) {
    if(size == data.length) {
      data = Arrays.copyOf(data, size << 1);
      pins = Arrays.copyOf(pins, size << 1);
    }
    data[size] = d;
    pins[size++] = 1;
  }

  /**
   * Returns information on the opened database instances.
   * @return data reference
   */
  public String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(SRVDATABASES, size);
    tb.add(size != 0 ? COL : DOT);
    for(int i = 0; i < size; ++i) {
      tb.add(NL + LI + data[i].meta.name + " (" + pins[i] + "x)");
    }
    return tb.toString();
  }

  /**
   * Closes all data references.
   */
  void close() {
    try {
      for(int i = 0; i < size; ++i) data[i].close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
    size = 0;
  }

  /**
   * Returns the number of pins for the specified database, or 0.
   * Used for testing.
   * @param db name of the database
   * @return number of references
   */
  public int pins(final String db) {
    for(int i = 0; i < size; ++i) {
      if(data[i].meta.name.equals(db)) return pins[i];
    }
    return 0;
  }
}
