package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.basex.io.IO;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.server.ClientListener;
import org.basex.server.Sessions;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * This class organizes all known events.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class Events extends HashMap<String, Sessions> {
  /** Event file. */
  private final File file = new File(Prop.HOME, IO.BASEXSUFFIX + "events");

  /**
   * Constructor.
   */
  public Events() {
    if(!file.exists()) return;

    DataInput in = null;
    try {
      in = new DataInput(file);
      final int s = in.readNum();
      for(int u = 0; u < s; ++u) put(string(in.readToken()), new Sessions());
    } catch(final IOException ex) {
      Util.errln(ex);
    } finally {
      if(in != null) try { in.close(); } catch(final IOException ex) { }
    }
  }

  /**
   * Creates an event.
   * @param name event name
   * @return success flag
   */
  public synchronized boolean create(final String name) {
    final boolean b = put(name, new Sessions()) == null;
    if(b) write();
    return b;
  }

  /**
   * Drops an event.
   * @param name event name
   * @return success flag
   */
  public synchronized boolean drop(final String name) {
    final boolean b = remove(name) != null;
    if(b) write();
    return b;
  }

  /**
   * Writes global permissions to disk.
   */
  private synchronized void write() {
    try {
      final DataOutput out = new DataOutput(file);
      out.writeNum(size());
      for(final String name : keySet()) out.writeToken(token(name));
      out.close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }

  /**
   * Returns information on all events.
   * @return information on all events.
   */
  public synchronized String info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(EVENTS_X, size()).add(size() != 0 ? COL : DOT);

    final String[] names = keySet().toArray(new String[size()]);
    Arrays.sort(names);
    for(final String n : names) tb.add(NL).add(LI).add(n);
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

    final Sessions sess = get(string(name));
    // event was not found
    if(sess == null) return false;

    // refresh timestamp for last interaction
    for(final ClientListener srv : sess) {
      // ignore active client
      if(srv == ctx.listener) continue;
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
