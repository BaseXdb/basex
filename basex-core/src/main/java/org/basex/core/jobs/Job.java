package org.basex.core.jobs;

import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;

/**
 * Job class. This abstract class is implemented by all commands and query instances.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class Job {
  /** State of job. */
  public enum State {
    /** OK.      */ OK,
    /** Stopped. */ STOPPED,
    /** Timeout. */ TIMEOUT,
    /** Memory.  */ MEMORY;
  }

  /** Listener, reacting on job information. */
  public InfoListener listener;
  /** This flag indicates that a command may perform updates. */
  public boolean updating;
  /** Stopped flag. */
  public State state = State.OK;

  /** Timer. */
  private Timer timer;
  /** Sub job. */
  private Job sub;

  /**
   * Registers the job (puts it on a queue).
   * @param ctx context
   */
  public void register(final Context ctx) {
    ctx.jobs.put(this, Boolean.TRUE);
    ctx.locks.acquire(this);
    // non-admin users: stop process after timeout
    if(!ctx.user().has(Perm.ADMIN)) startTimeout(ctx.soptions.get(StaticOptions.TIMEOUT) * 1000L);
  }

  /**
   * Unregisters the job.
   * @param ctx context
   */
  public void unregister(final Context ctx) {
    stopTimeout();
    ctx.locks.release(this);
    ctx.jobs.remove(this);
  }


  /**
   * Returns the currently active job.
   * @return job
   */
  public final Job active() {
    return sub != null ? sub.active() : this;
  }

  /**
   * Attaches the specified info listener.
   * @param il info listener
   */
  public final void listener(final InfoListener il) {
    if(sub != null) sub.listener(il);
    listener = il;
  }

  /**
   * Sets a new sub job.
   * @param <J> job type
   * @param job job
   * @return passed on job reference
   */
  public final <J extends Job> J job(final J job) {
    sub = job;
    if(job != null) {
      job.listener = listener;
      job.job(sub.sub);
      if(state != State.OK) job.state(state);
    }
    return job;
  }

  /**
   * Stops a job or sub job.
   */
  public final void stop() {
    state(State.STOPPED);
  }

  /**
   * Stops a job because of a timeout.
   */
  public final void timeout() {
    state(State.TIMEOUT);
  }

  /**
   * Stops a job because a memory limit was exceeded.
   */
  public final void memory() {
    state(State.MEMORY);
  }

  /**
   * Sets a new job state.
   * @param st new state
   */
  final void state(final State st) {
    if(sub != null) sub.state(st);
    state = st;
    stopTimeout();
  }

  /**
   * Checks if the job was interrupted; if yes, sends a runtime exception.
   */
  public final void checkStop() {
    if(state != State.OK) throw new JobException();
  }

  /**
   * Aborts a failed or interrupted job.
   */
  protected void abort() {
    if(sub != null) sub.abort();
  }

  /**
   * Starts a timeout thread.
   * @param ms milliseconds to wait; deactivated if set to 0
   */
  public final void startTimeout(final long ms) {
    if(ms == 0) return;

    timer = new Timer(true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() { timeout(); }
    }, ms);
  }

  /**
   * Stops the timeout thread.
   */
  public final void stopTimeout() {
    if(timer != null) {
      timer.cancel();
      timer = null;
    }
  }

  /**
   * Adds the names of the databases that may be touched by the job.
   * @param lr container for lock result to pass around
   */
  public void databases(final LockResult lr) {
    // default (worst case): lock all databases
    lr.writeAll = true;
  }

  /**
   * Returns short progress information.
   * Can be overwritten to give more specific feedback.
   * @return header information
   */
  public String shortInfo() {
    return Text.PLEASE_WAIT_D;
  }

  /**
   * Returns detailed progress information.
   * Can be overwritten to give more specific feedback.
   * @return header information
   */
  public String detailedInfo() {
    return Text.PLEASE_WAIT_D;
  }

  /**
   * Returns a progress value (0 - 1).
   * Can be overwritten to give more specific feedback.
   * @return header information
   */
  public double progressInfo() {
    return 0;
  }
}
