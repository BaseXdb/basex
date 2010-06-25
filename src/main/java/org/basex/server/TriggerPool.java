package org.basex.server;

import java.util.HashMap;

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
   * Standard constructor.
   */
  public TriggerPool() { }

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
    Sessions s = triggers.get(trigger);
    if(s != null) {
      s.add(sp);
      return true;
    }
    return false;
  }

  /**
   * Attaches the server process to the trigger.
   * @param trigger trigger
   * @param sp server process
   * @return success of operation
   */
  public boolean detach(final ServerProcess trigger, final ServerProcess sp) {
    Sessions s = triggers.get(trigger);
    if(s != null) {
      s.delete(sp);
      return true;
    }
    return false;
  }

  /**
   * Removes the trigger from the pool.
   * @param trigger server process
   */
  public void delete(final ServerProcess trigger) {
    triggers.remove(trigger);
  }
}