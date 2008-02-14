package org.basex.util;

/**
 * This class defines an action which is offered as a thread.
 * If it is called several times, only the latest call is executed.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Action {
  /** Counter. */
  protected int counter;

  /**
   * Creates a new action thread.
   * @return thread
   */
  public final Thread create() {
    final int c = ++counter;
    
    return new Thread() {
      @Override
      public void run() {
        if(c != counter) return;
        action();
      }
    };
  }

  /**
   * Creates a new action thread.
   * @return thread
   */
  public final Thread repeat() {
    final int c = ++counter;
    
    return new Thread() {
      @Override
      public void run() {
        while(c == counter) action();
      }
    };
  }

  /**
   * Increases the action counter. If a repeated process is running,
   * it is stopped.
   */
  public final void stop() {
    ++counter;
  }

  /** Action to be executed. */
  public abstract void action();
}
