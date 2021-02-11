package org.basex.core.jobs;

import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.util.*;

/**
 * Job class. This abstract class is implemented by all command and query instances.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Job {
  /** Child jobs. */
  private final List<Job> children = Collections.synchronizedList(new ArrayList<>(0));
  /** Job context. */
  private JobContext jc = new JobContext(this);
  // state and control flags must be volatile so that all threads see the actual non-cached values
  /** Timer. */
  private volatile Timer timer;

  /** This flag indicates that a job is updating. */
  public volatile boolean updating;
  /** State of job. */
  public volatile JobState state = JobState.SCHEDULED;
  /** Stopped flag. */
  private volatile boolean stopped;

  /**
   * Returns the job context.
   * @return info
   */
  public final JobContext jc() {
    return jc;
  }

  /**
   * Registers the job (puts it on a queue).
   * @param ctx context
   */
  public final void register(final Context ctx) {
    jc.context = ctx;
    ctx.jobs.register(this);
    state(JobState.QUEUED);
    ctx.locking.acquire(this, ctx);
    state(JobState.RUNNING);
    jc.performance = new Performance();
    // non-admin users: stop process after timeout
    if(!ctx.user().has(Perm.ADMIN)) startTimeout(ctx.soptions.get(StaticOptions.TIMEOUT));
  }

  /**
   * Unregisters the job.
   * @param ctx context
   */
  public final void unregister(final Context ctx) {
    stopTimeout();
    ctx.locking.release();
    ctx.jobs.unregister(this);
  }

  /**
   * Returns the currently active job.
   * @return job
   */
  public final Job active() {
    return children.isEmpty() ? this : children.get(0).active();
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
  public final synchronized void popJob() {
    children.remove(children.size() - 1);
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
   * Checks if the job was stopped; if yes, throws a runtime exception.
   */
  public final void checkStop() {
    if(stopped) throw new JobException(Text.INTERRUPTED);
  }

  /**
   * Indicates if the job was stopped.
   * @return result of check
   */
  public final boolean stopped() {
    return stopped;
  }

  /**
   * Sends a new job state.
   * @param js new state
   */
  public void state(final JobState js) {
    for(final Job job : children) job.state(js);
    state = js;
    if(js == JobState.STOPPED || js == JobState.TIMEOUT || js == JobState.MEMORY) {
      stopped = true;
      stopTimeout();
    }
  }

  /**
   * Adds the strings (databases, special identifiers) for which locks need to be acquired.
   */
  public void addLocks() {
    // default (worst case): lock all databases
    jc.locks.writes.addGlobal();
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
    for(final Job job : children) job.jobContext(ctx);
    jc = ctx;
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Starts a timeout thread.
   * @param sec seconds wait; deactivated if set to 0
   */
  private void startTimeout(final long sec) {
    if(sec == 0) return;
    timer = new Timer(true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() { timeout(); }
    }, sec * 1000L);
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
