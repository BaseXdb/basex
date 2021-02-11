package org.basex.server;

import static org.basex.core.Text.*;

import java.util.concurrent.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class organizes all currently opened database sessions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Sessions extends CopyOnWriteArrayList<ClientListener> {
  /**
   * Returns information about the currently opened sessions.
   * @return data reference
   */
  public synchronized String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(SESSIONS_X, size()).add(size() == 0 ? DOT : COL);

    final StringList sl = new StringList();
    for(final ClientListener cl : this) {
      sl.add(cl.context().user().name() + ' ' + cl);
    }
    for(final String sp : sl.sort()) tb.add(NL).add(LI).add(sp);
    return tb.toString();
  }

  /**
   * Closes all sessions.
   */
  public synchronized void close() {
    while(!isEmpty()) {
      get(size() - 1).close();
    }
  }
}
