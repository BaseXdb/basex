package org.basex.server;

import static org.basex.core.Text.*;

import java.util.Iterator;
import org.basex.core.Main;
import org.basex.util.Array;
import org.basex.util.TokenBuilder;

/**
 * This is a simple container for sessions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Sessions implements Iterable<Session> {
  /** Entries. */
  Session[] list = new Session[8];
  /** Number of entries. */
  int size;

  /**
   * Adds a session to the array.
   * @param s string to be added
   */
  public void add(final Session s) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = s;
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return size;
  }

  /**
   * Returns the specified entry.
   * @param p position
   * @return entry
   */
  public Session get(final int p) {
    return list[p];
  }

  /**
   * Deletes the specified entry.
   * @param s entry to be deleted.
   */
  public void delete(final Session s) {
    for(int i = 0; i < size; i++) {
      if(list[i] == s) {
        Array.move(list, i + 1, -1, --size - i);
        break;
      }
    }
  }

  public Iterator<Session> iterator() {
    return new Iterator<Session>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public Session next() { return list[c]; }
      public void remove() { Main.notexpected(); }
    };
  }

  /**
   * Returns information on the opened database instances.
   * @return data reference
   */
  public String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(SRVSESSIONS, size);
    tb.add(size != 0 ? COL + NL : DOT);
    for(final Session s : this) tb.add(LI + s.info() + NL);
    return tb.toString();
  }
}
