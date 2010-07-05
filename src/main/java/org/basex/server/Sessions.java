package org.basex.server;

import static org.basex.core.Text.*;
import java.util.Arrays;
import java.util.Iterator;
import org.basex.core.Main;
import org.basex.util.Array;
import org.basex.util.TokenBuilder;

/**
 * This is a simple container for sessions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Sessions implements Iterable<ServerProcess> {
  /** Entries. */
  ServerProcess[] list = new ServerProcess[1];
  /** Number of entries. */
  int size;

  /**
   * Adds a session to the array.
   * @param s string to be added
   */
  public void add(final ServerProcess s) {
    if(size == list.length) list = Arrays.copyOf(list, size << 1);
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
  public ServerProcess get(final int p) {
    return list[p];
  }

  /**
   * Deletes the specified entry.
   * @param s entry to be deleted
   */
  public void delete(final ServerProcess s) {
    for(int i = 0; i < size; i++) {
      if(list[i] == s) {
        Array.move(list, i + 1, -1, --size - i);
        break;
      }
    }
  }

  /**
   * Returns information on the opened database instances.
   * @return data reference
   */
  public String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(SRVSESSIONS, size);
    tb.add(size != 0 ? COL : DOT);
    for(int i = 0; i < size; i++) tb.add(NL + LI +
        list[i].context.user.name + " " + list[i].info());
    return tb.toString();
  }

  @Override
  public Iterator<ServerProcess> iterator() {
    return new Iterator<ServerProcess>() {
      private int c = -1;
      @Override
      public boolean hasNext() { return ++c < size; }
      @Override
      public ServerProcess next() { return list[c]; }
      @Override
      public void remove() { Main.notexpected(); }
    };
  }
}
