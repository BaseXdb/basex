package org.basex.core.jobs;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.util.*;

/**
 * Job class. This abstract class is implemented by all command and query instances.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Job {
  /** Child jobs. */
  private final List<Job> children = new CopyOnWriteArrayList<>();
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

  /** Job registered on the current thread (used to interrupt blocking operations). */
  private static final ThreadLocal<Job> CURRENT = new ThreadLocal<>();
  /** Stoppable threads. */
  private final Set<Thread> threads = ConcurrentHashMap.newKeySet();
  /** Job that was registered on the current thread before this one. */
  private Job previous;

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
    previous = CURRENT.get();
    CURRENT.set(this);
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
    CURRENT.set(previous);
    previous = null;
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
    if(stopped) job.state(state);
    return job;
  }

  /**
   * Pops the last child job (LIFO).
   */
  public final synchronized void popJob() {
    children.remove(children.size() - 1);
  }

  /**
   * Removes the specified child job. Used when children are closed concurrently in arbitrary order.
   * @param job child job to remove
   */
  public final void popJob(final Job job) {
    children.remove(job);
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
  public final void outOfMemory() {
    state(JobState.MEMORY);
  }

  /**
   * Checks if the job was stopped; if yes, throws a runtime exception.
   */
  public final void checkStop() {
    if(stopped) throw new JobException(Text.INTERRUPTED);
  }

  /**
   * Runs a stoppable operation. If no job is registered for the current thread,
   * the operation is run directly.
   * @param <T> result type
   * @param op operation to run
   * @return result
   * @throws IOException I/O exception
   * @throws InterruptedException interrupted exception
   */
  public static <T> T run(final Stoppable<T> op) throws IOException, InterruptedException {
    final Job job = CURRENT.get();
    return job != null ? job.runStoppable(op) : op.run();
  }

  /**
   * Binds this job to the current thread until the returned handle is closed. Blocking operations
   * started on the thread (see {@link #run(Stoppable)}) can then be interrupted when the job is
   * stopped. Intended for jobs that run on a thread outside {@link #register(Context)} (such as
   * parallelized query branches).
   * @return handle that restores the previously bound job
   */
  public final Binding bind() {
    final Job job = CURRENT.get();
    CURRENT.set(this);
    return () -> CURRENT.set(job);
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
  public final void state(final JobState js) {
    state = js;
    final boolean stop = js == JobState.STOPPED || js == JobState.TIMEOUT || js == JobState.MEMORY;
    if(stop) {
      stopped = true;
      for(final Thread thread : threads) thread.interrupt();
    }
    for(final Job job : children) job.state(js);
    if(stop) stopTimeout();
  }

  /**
   * Collects lock strings (databases, special identifiers) when registering a query.
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

  /**
   * Runs a blocking operation that is aborted if the job is stopped. The executing thread is
   * registered while the operation is in progress; {@link #state(JobState)} interrupts it if the
   * job is stopped, and a resulting failure is replaced with the regular interruption exception.
   * @param <T> result type
   * @param op operation to run
   * @return result
   * @throws IOException I/O exception
   * @throws InterruptedException interrupted exception
   */
  final <T> T runStoppable(final Stoppable<T> op) throws IOException, InterruptedException {
    final Thread thread = Thread.currentThread();
    threads.add(thread);
    try {
      checkStop();
      return op.run();
    } catch(final IOException | InterruptedException ex) {
      Util.debug(ex);
      checkStop();
      throw ex;
    } finally {
      threads.remove(thread);
      Thread.interrupted();
    }
  }

  /**
   * A blocking operation that can be interrupted when the surrounding job is stopped.
   * @param <T> result type
   */
  @FunctionalInterface
  public interface Stoppable<T> {
    /**
     * Runs the operation.
     * @return result
     * @throws IOException I/O exception
     * @throws InterruptedException interrupted exception
     */
    T run() throws IOException, InterruptedException;
  }

  /** Handle that unbinds a job from the current thread; see {@link #bind()}. */
  @FunctionalInterface
  public interface Binding extends AutoCloseable {
    @Override
    void close();
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
