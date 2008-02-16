package org.basex.util;

/**
 * This class defines an action which is executed as a thread.
 * If it is called another time, the current thread is skipped.
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
  public final Thread single() {
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
   * Creates a new repeated action thread.
   * @param ms number of milliseconds to wait before executions
   */
  public final void repeat(final int ms) {
    final int c = ++counter;
    
    new Thread() {
      @Override
      public void run() {
        while(c == counter) {
          action();
          Performance.sleep(ms);
        }
      }
    }.start();
  }

  /**
   * Sleeps for a while and executes the action afterwards.
   * @param ms number of milliseconds to wait before executions
   */
  public final void sleep(final int ms) {
    final int c = ++counter;
    
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(ms);
        if(c == counter) action();
      }
    }.start();
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
