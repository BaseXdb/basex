package org.basex.core;

import java.util.*;
import java.util.Map.Entry;

import org.basex.data.*;
import org.basex.util.*;

/**
 * This class organizes pins of currently opened databases.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class Datas {
  /** List of data references. */
  private final IdentityHashMap<Data, Integer> list = new IdentityHashMap<>();

  /**
   * Pins and returns a database with the specified name, or returns {@code null}.
   * @param name name of the database
   * @return data reference
   */
  public synchronized Data pin(final String name) {
    final Entry<Data, Integer> entry = get(name);
    if(entry == null) return null;

    final Data data = entry.getKey();
    list.put(data, entry.getValue() + 1);
    return data;
  }

  /**
   * Pins a data reference.
   * @param data data reference
   */
  public synchronized void pin(final Data data) {
    final Integer pins = list.get(data);
    list.put(data, pins == null ? 1 : pins + 1);
  }

  /**
   * Unpins a data reference and closes the database if no references exist anymore.
   * @param data data reference
   */
  public synchronized void unpin(final Data data) {
    final Integer pins = list.get(data);
    // main-memory instances are not pinned
    if(pins == null) return;

    data.unpin();
    final int p = pins;
    if(p == 1) {
      data.close();
      list.remove(data);
    } else {
      list.put(data, p - 1);
    }
  }

  /**
   * Checks if the database with the specified name is pinned.
   * @param name name of the database
   * @return result of check
   */
  synchronized boolean pinned(final String name) {
    return get(name) != null;
  }

  /**
   * Returns the number of pins for the database with the specified name,
   * or {@code 0} if the database is not opened.
   * @param name name of the database
   * @return number of references
   */
  public synchronized int pins(final String name) {
    final Entry<Data, Integer> entry = get(name);
    return entry == null ? 0 : entry.getValue();
  }

  /**
   * Closes all data references.
   */
  synchronized void close() {
    for(final Data data : list.keySet()) data.close();
    list.clear();
  }

  /**
   * Returns an entry for the database with the specified name.
   * @param name name of the database
   * @return entry, or {@code null}
   */
  private Entry<Data, Integer> get(final String name) {
    for(final Entry<Data, Integer> entry : list.entrySet()) {
      final String db = entry.getKey().meta.name;
      if(Prop.CASE ? db.equals(name) : db.equalsIgnoreCase(name)) return entry;
    }
    return null;
  }
}
