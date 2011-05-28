package org.basex.core;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.basex.server.ServerProcess;
import org.basex.server.Sessions;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class organizes all known events.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class Events extends HashMap<String, Sessions> {
  /**
   * Returns information on all events.
   * @return information on all events.
   */
  public String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(SRVEVENTS, size()).add(size() != 0 ? COL : DOT);

    final String[] events = keySet().toArray(new String[size()]);
    Arrays.sort(events);
    for(final String name : events) tb.add(NL).add(LI).add(name);
    return tb.toString();
  }

  /**
   * Notifies the watching sessions about an event.
   * @param ctx database context
   * @param name name
   * @param msg message
   * @return success flag
   */
  public synchronized boolean notify(final Context ctx, final byte[] name,
      final byte[] msg) {

    final Sessions sess = get(Token.string(name));
    // event was not found
    if(sess == null) return false;

    for(final ServerProcess srv : sess) {
      // ignore active client
      if(srv == ctx.session) continue;
      try {
        srv.notify(name, msg);
      } catch(final IOException ex) {
        // remove client if event could not be delivered
        sess.remove(srv);
      }
    }
    return true;
  }
}
