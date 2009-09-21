package org.basex.server;

import static org.basex.core.Text.*;
import org.basex.util.Array;
import org.basex.util.TokenBuilder;

/**
 * This is a simple container for sessions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Sessions {
  /** Entries. */
  ServerSession[] list = new ServerSession[8];
  /** Number of entries. */
  int size;

  /**
   * Adds a session to the array.
   * @param s string to be added
   */
  public void add(final ServerSession s) {
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
  public ServerSession get(final int p) {
    return list[p];
  }

  /**
   * Deletes the specified entry.
   * @param s entry to be deleted.
   */
  public void delete(final ServerSession s) {
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
    for(int i = 0; i < size; i++) tb.add(NL + LI + list[i].info());
    return tb.toString();
  }
}
