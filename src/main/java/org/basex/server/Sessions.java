package org.basex.server;

import static org.basex.core.Text.*;

import java.util.ArrayList;

import org.basex.util.StringList;
import org.basex.util.TokenBuilder;

/**
 * This class organizes all database sessions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Sessions extends ArrayList<ServerProcess> {
  /**
   * Returns information on the opened sessions.
   * @return data reference
   */
  public synchronized String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(SRVSESSIONS, size()).add(size() != 0 ? COL : DOT);

    final StringList sl = new StringList();
    for(final ServerProcess sp : this) sl.add(sp.user().name + ' ' + sp);
    sl.sort(true, true);
    for(final String sp : sl) tb.add(NL).add(LI).add(sp);
    return tb.toString();
  }
}
