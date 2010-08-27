package org.basex.core;

import java.util.HashMap;
import org.basex.server.ServerProcess;
import org.basex.server.Sessions;

/**
 * Management of Notification Triggers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class TriggerPool {
  /** Active triggers. */
  private final HashMap<ServerProcess, Sessions> triggers =
    new HashMap<ServerProcess, Sessions>();

  /**
   * Adds a trigger to the pool.
   * @param trigger server process
   */
  public void add(final ServerProcess trigger) {
    triggers.put(trigger, new Sessions());
  }

  /**
   * Attaches the server process to the trigger.
   * @param trigger server process
   * @param sp server process
   * @return success of operation
   */
  public boolean attach(final ServerProcess trigger, final ServerProcess sp) {
    final Sessions s = triggers.get(trigger);
    if(s == null) return false;
    s.add(sp);
    return true;
  }

  /**
   * Detaches the server process from the trigger.
   * @param trigger trigger
   * @param sp server process
   * @return success of operation
   */
  public boolean detach(final ServerProcess trigger, final ServerProcess sp) {
    final Sessions s = triggers.get(trigger);
    if(s == null) return false;
    s.delete(sp);
    return true;
  }

  /**
   * Removes the trigger from the pool.
   * @param trigger server process
   */
  public void delete(final ServerProcess trigger) {
    triggers.remove(trigger);
  }
}