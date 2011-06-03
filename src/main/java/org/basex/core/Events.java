package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.server.ServerProcess;
import org.basex.server.Sessions;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * This class organizes all known events.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class Events extends HashMap<String, Sessions> {

  /** Eventfile. */
  private File file;

  /**
   * Constructor.
   */
  public Events() {
    file = new File(Prop.HOME + ".basexevents");
    try {
      if(file.exists()) {
        final DataInput in = new DataInput(file);
        read(in);
        in.close();
      } else {
        write();
      }
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }

  /**
   * Adds an event.
   * @param name event name
   */
  public void put(final String name) {
    this.put(name, new Sessions());
    write();
  }

  /**
   * Deletes an event.
   * @param name event name
   * @return success flag
   */
  public boolean delete(final String name) {
    boolean b = this.remove(name) != null;
    if(b) write();
    return b;
  }

  /**
   * Reads events from disk.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public void read(final DataInput in) throws IOException {
    final int s = in.readNum();
    for(int u = 0; u < s; ++u) {
      final String name = string(in.readBytes());
      put(name, new Sessions());
    }
  }

  /**
   * Writes global permissions to disk.
   */
  public void write() {
    try {
      final DataOutput out = new DataOutput(file);
      out.writeNum(size());
      for(final String name : this.keySet()) {
        out.writeString(name);
      }
      out.close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }

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
