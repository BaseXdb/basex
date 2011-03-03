package org.basex.core;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.basex.server.ServerProcess;
import org.basex.server.Sessions;
import org.basex.util.TokenBuilder;

/**
 * Management of Notification Triggers.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class TriggerPool {
  /** Active triggers - trigger name, registered sessions. */
  private final HashMap<String, Sessions> triggers =
    new HashMap<String, Sessions>();

  /**
   * Gets size of trigger list.
   * @return size of triggers list
   */
  public int size() {
    return triggers.size();
  }

  /**
   * Creates a trigger with the given name.
   * @param name trigger name
   * @return true if trigger was created successfully
   */
  public boolean create(final String name) {
    if (triggers.containsKey(name)) {
      return false;
    }
    triggers.put(name, new Sessions());
    return true;
  }

  /**
   * Drops the named trigger from the pool.
   * @param name trigger name
   * @return true if trigger was deleted successfully
   */
  public boolean drop(final String name) {
    if (!triggers.containsKey(name)) {
      return false;
    }
    triggers.remove(name);
    return true;
  }

  /**
   * Attaches the server process to the trigger.
   * @param name trigger name
   * @param sp server process
   * @return success of operation
   */
  public boolean attach(final String name, final ServerProcess sp) {
    final Sessions s = triggers.get(name);
    if(s == null) return false;
    s.add(sp);
    sp.triggers.add(name);
    return true;
  }

  /**
   * Detaches the server process from the trigger.
   * @param name trigger name
   * @param sp server process
   * @return success of operation
   */
  public boolean detach(final String name, final ServerProcess sp) {
    final Sessions s = triggers.get(name);
    if(s == null) return false;
    s.delete(sp);
    sp.triggers.remove(name);
    return true;
  }

  /**
   * Returns information on all triggers.
   * @return information on all triggers.
   */
  public String info() {
    TokenBuilder tb = new TokenBuilder();
    for (String name : triggers.keySet()) {
      tb.add(NL + name);
    }
    return tb.toString();
  }

  /**
   * Removes session from all triggers.
   * @param sp server process
   * @param l list of triggers
   */
  public void remove(final ServerProcess sp, final ArrayList<String> l) {
    for(String s : l) {
      Sessions sess = triggers.get(s);
      if(sess != null) sess.delete(sp);
    }
  }

  /**
   * Notifies the attached sessions about a triggered event.
   * @param sp server process
   * @param name name
   * @param i item
   */
  public void notify(final ServerProcess sp, final String name,
      final byte[] i) {
    Sessions sessions = triggers.get(name);
    if (sessions == null) return;

    for (ServerProcess srv : sessions) {
      if(!srv.equals(sp)) {
        try {
          srv.tout.write(1);
          srv.tout.writeString(name);
          srv.tout.write(i);
          srv.tout.write(0);
          srv.tout.flush();
        } catch(IOException e) {
          sessions.delete(srv);
        }
      }
    }
  }
}