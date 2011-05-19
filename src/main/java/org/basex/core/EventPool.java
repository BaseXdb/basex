package org.basex.core;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.basex.server.ServerProcess;
import org.basex.server.Sessions;
import org.basex.util.TokenBuilder;

/**
 * Management of Notification Events.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class EventPool {
  /** Active events - events name, registered sessions. */
  private final HashMap<String, Sessions> events =
    new HashMap<String, Sessions>();

  /**
   * Gets size of event list.
   * @return size of events list
   */
  public int size() {
    return events.size();
  }

  /**
   * Creates an event with the given name.
   * @param name event name
   * @return true if event was created successfully
   */
  public boolean create(final String name) {
    if (events.containsKey(name)) {
      return false;
    }
    events.put(name, new Sessions());
    return true;
  }

  /**
   * Drops the named event from the pool.
   * @param name event name
   * @return true if event was deleted successfully
   */
  public boolean drop(final String name) {
    if (!events.containsKey(name)) {
      return false;
    }
    events.remove(name);
    return true;
  }

  /**
   * Watches an event.
   * @param name event name
   * @param sp server process
   * @return success of operation
   */
  public boolean watch(final String name, final ServerProcess sp) {
    final Sessions s = events.get(name);
    if(s == null) return false;
    s.add(sp);
    sp.events.add(name);
    return true;
  }

  /**
   * Unwatches events.
   * @param name event name
   * @param sp server process
   * @return success of operation
   */
  public boolean unwatch(final String name, final ServerProcess sp) {
    final Sessions s = events.get(name);
    if(s == null) return false;
    s.delete(sp);
    sp.events.remove(name);
    return true;
  }

  /**
   * Returns information on all events.
   * @return information on all events.
   */
  public String info() {
    TokenBuilder tb = new TokenBuilder();
    for (String name : events.keySet()) {
      tb.add(NL + name);
    }
    return tb.toString();
  }

  /**
   * Removes session from all events.
   * @param sp server process
   * @param l list of events
   */
  public void remove(final ServerProcess sp, final ArrayList<String> l) {
    for(String s : l) {
      Sessions sess = events.get(s);
      if(sess != null) sess.delete(sp);
    }
  }

  /**
   * Notifies the watching sessions about an event.
   * @param sp server process
   * @param name name
   * @param msg message
   */
  public void notify(final ServerProcess sp, final String name,
      final String msg) {
    Sessions sessions = events.get(name);
    if (sessions == null) return;

    for (ServerProcess srv : sessions) {
      if(!srv.equals(sp)) {
        try {
          srv.tout.print(name);
          srv.tout.write(0);
          srv.tout.print(msg);
          srv.tout.write(0);
          srv.tout.flush();
        } catch(IOException e) {
          sessions.delete(srv);
        }
      }
    }
  }
}