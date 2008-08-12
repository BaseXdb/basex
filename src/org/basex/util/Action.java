package org.basex.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class defines an action which is executed as a thread.
 * If it is called another time, the current thread is skipped.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Action implements Runnable {
  /** Executor service. */
  private final ScheduledExecutorService sch =
    Executors.newSingleThreadScheduledExecutor();
  /** Running thread reference. */
  private ScheduledFuture<?> sf;

  /**
   * Creates a new repeated action thread.
   * @param ms number of milliseconds to wait before executions
   */
  public final void repeat(final int ms) {
    sf = sch.scheduleAtFixedRate(this, 0, ms, TimeUnit.MILLISECONDS);
  }
  
  /**
   * Creates a new action thread.
   */
  public final void execute() {
    delay(0);
  }

  /**
   * Sleeps for a while and executes the action afterwards.
   * @param ms number of milliseconds to wait before executions
   */
  public final void delay(final int ms) {
    cancel();
    sf = sch.schedule(this, ms, TimeUnit.MILLISECONDS);
  }

  /**
   * Stops the current process.
   * @return true if process was stopped.
   */
  public final boolean cancel() {
    return sf != null && sf.cancel(true);
  }

  /**
   * Checks if the process is running.
   * @return result of check
   */
  public final boolean running() {
    return sf != null && !sf.isDone();
  }
}
