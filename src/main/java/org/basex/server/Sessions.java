package org.basex.server;

import static org.basex.core.Text.*;

import java.util.concurrent.CopyOnWriteArrayList;

import org.basex.util.TokenBuilder;
import org.basex.util.list.StringList;

/**
 * This class organizes all currently opened database sessions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Sessions extends CopyOnWriteArrayList<ClientListener> {
  /**
   * Returns information about the currently opened sessions.
   * @return data reference
   */
  public synchronized String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(SRVSESSIONS, size()).add(size() != 0 ? COL : DOT);

    final StringList sl = new StringList();
    for(final ClientListener sp : this) {
      sl.add(sp.context().user.name + ' ' + sp);
    }
    sl.sort(true, true);
    for(final String sp : sl) tb.add(NL).add(LI).add(sp);
    return tb.toString();
  }
}
