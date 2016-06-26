package org.basex.core.jobs;

import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.util.*;

/**
 * Job class. This abstract class is implemented by all command and query instances.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class Job {
  /** Child jobs. */
  private final ArrayList<Job> children = new ArrayList<>(0);
  /** Job context. */
  private JobContext jc = new JobContext(this);

  /** This flag indicates that a job is updating. */
  public boolean updating;
  /** Stopped flag. */
  public JobState state = JobState.OK;

  /** Timer. */
  private Timer timer;

  /**
   * Returns the job context.
   * @return info
   */
  public final JobContext job() {
    return jc;
  }

  /**
   * Registers the job (puts it on a queue).
   * @param ctx context
   */
  public final void register(final Context ctx) {
    ctx.jobs.add(this);
    ctx.locks.acquire(this);
    jc.perf = new Performance();
    // non-admin users: stop process after timeout
    if(!ctx.user().has(Perm.ADMIN)) startTimeout(ctx.soptions.get(StaticOptions.TIMEOUT) * 1000L);
  }

  /**
   * Unregisters the job.
   * @param ctx context
   */
  public final void unregister(final Context ctx) {
    stopTimeout();
    ctx.locks.release(this);
    ctx.jobs.remove(this);
  }

  /**
   * Returns the currently active job.
   * @return job
   */
  public final Job active() {
    for(final Job job : children) return job.active();
    return this;
  }

  /**
   * Adds a new child job.
   * @param <J> job type
   * @param job child job
   * @return passed on job reference
   */
  public final <J extends Job> J pushJob(final J job) {
    children.add(job);
    job.jobContext(jc);
    return job;
  }

  /**
   * Pops the last job.
   */
  public final void popJob() {
    final int last = children.size() - 1;
    children.remove(last);
  }

  /**
   * Stops a job or sub job.
   */
  public final void stop() {
    state(JobState.STOPPED);
  }

  /**
   * Stops a job because of a timeout.
   */
  public final void timeout() {
    state(JobState.TIMEOUT);
  }

  /**
   * Stops a job because a memory limit was exceeded.
   */
  public final void memory() {
    state(JobState.MEMORY);
  }

  /**
   * Sets a new job state.
   * @param st new state
   */
  final void state(final JobState st) {
    for(final Job job : children) job.state(st);
    state = st;
    stopTimeout();
  }

  /**
   * Checks if the job was interrupted; if yes, sends a runtime exception.
   */
  public final void checkStop() {
    if(state != JobState.OK) throw new JobException();
  }

  /**
   * Aborts a failed or interrupted job.
   */
  protected void abort() {
    for(final Job job : children) job.abort();
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

  /**
   * Recursively assigns the specified job context.
   * @param ctx job context
   */
  final void jobContext(final JobContext ctx) {
    for(final Job ch : children) ch.jobContext(ctx);
    jc = ctx;
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Starts a timeout thread.
   * @param ms milliseconds to wait; deactivated if set to 0
   */
  private void startTimeout(final long ms) {
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
  private void stopTimeout() {
    if(timer != null) {
      timer.cancel();
      timer = null;
    }
  }
}
