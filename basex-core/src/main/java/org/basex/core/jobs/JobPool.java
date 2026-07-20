package org.basex.core.jobs;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Job pool.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobPool {
  /** Maximum number of jobs running in parallel. */
  static final int MAX_RUNNING = 1 << 10;
  /** Maximum number of cached jobs. */
  static final int MAX_CACHED = 1 << 10;
  /** Maximum number of registered jobs. */
  static final int MAX_REGISTERED = 1 << 20;

  /** Queued or running jobs. */
  public final Map<String, Job> active = new ConcurrentHashMap<>();
  /** Cached results. */
  public final Map<String, QueryJobResult> results = new ConcurrentHashMap<>();
  /** Scheduled tasks. */
  public final Map<String, QueryJobTask> tasks = new ConcurrentHashMap<>();

  /** Scheduler for delayed and periodic job tasks. */
  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(factory("basex-scheduler"));
  /** Executor for running jobs. */
  private final ExecutorService pool = Executors.newCachedThreadPool(factory("basex-job"));
  /** Available slots for jobs running in parallel. */
  private final Semaphore slots = new Semaphore(MAX_RUNNING);
  /** Monitor, notified whenever a job or task completes (see {@link #awaitChange}). */
  private final Object monitor = new Object();
  /** Timeout (ms). */
  private final long timeout;

  /**
   * Constructor.
   * @param sopts static options
   */
  public JobPool(final StaticOptions sopts) {
    timeout = sopts.get(StaticOptions.CACHETIMEOUT) * 1000L;
  }

  /**
   * Runs a job in a separate thread.
   * @param job job to run
   */
  void execute(final QueryJob job) {
    pool.execute(job);
  }

  /**
   * Schedules a job task for delayed or periodic execution.
   * @param task job task
   * @param delay initial delay (ms)
   * @param interval repeat interval (ms; run once: {@code 0})
   */
  void schedule(final QueryJobTask task, final long delay, final long interval) {
    // assign the cancellation handle before the task can run its body (see QueryJobTask#run)
    synchronized(task) {
      task.future(interval > 0
          ? scheduler.scheduleAtFixedRate(task, delay, interval, TimeUnit.MILLISECONDS)
          : scheduler.schedule(task, delay, TimeUnit.MILLISECONDS));
    }
  }

  /**
   * Registers a job, blocking until a run slot is available.
   * @param job job
   */
  public void register(final Job job) {
    slots.acquireUninterruptibly();
    active.put(job.jc().id(), job);
  }

  /**
   * Unregisters a job and releases its run slot.
   * @param job job
   */
  public void unregister(final Job job) {
    active.remove(job.jc().id());
    slots.release();
    notifyChange();
  }

  /**
   * Wakes threads that wait for a job or task to complete.
   */
  public void notifyChange() {
    synchronized(monitor) {
      monitor.notifyAll();
    }
  }

  /**
   * Waits a bounded time for a job or task to complete.
   */
  public void awaitChange() {
    synchronized(monitor) {
      try {
        monitor.wait(1000);
      } catch(final InterruptedException ex) {
        Util.debug(ex);
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Stops all jobs before closing the application.
   */
  public synchronized void close() {
    // stop running tasks and queries
    scheduler.shutdownNow();
    for(final Job job : active.values()) job.stop();
    while(!active.isEmpty()) awaitChange();
    pool.shutdown();
  }

  /**
   * Schedules a one-off task for delayed execution.
   * @param task task
   * @param delay delay (ms)
   * @return cancellation handle
   */
  ScheduledFuture<?> schedule(final Runnable task, final long delay) {
    return scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
  }

  /**
   * Discards a result after the timeout.
   * @param job job
   */
  public void scheduleResult(final Job job) {
    schedule(() -> results.remove(job.jc().id()), timeout);
  }

  /**
   * Returns all registered IDs.
   * @return sorted ID list
   */
  public TokenList ids() {
    final Set<String> set = new HashSet<>(results.keySet());
    set.addAll(active.keySet());
    set.addAll(tasks.keySet());
    final TokenList ids = new TokenList(set.size());
    for(final String id : set) ids.add(id);

    // compare default job counter, or compare custom IDs as strings
    final byte[] prefix = token(JobContext.PREFIX);
    final int pl = prefix.length;
    return ids.sort((id1, id2) -> startsWith(id1, prefix) && startsWith(id2, prefix) ?
        toInt(substring(id1, pl)) - toInt(substring(id2, pl)) : compare(id1, id2), true);
  }

  /**
   * Removes a job.
   * @param id ID
   * @return return success flag
   */
  public boolean remove(final String id) {
    // stop scheduled task
    final QueryJobTask task = tasks.remove(id);
    if(task != null) task.cancel();
    // send stop signal to job
    final Job job = active.get(id);
    if(job != null) job.stop();
    // remove potentially cached result
    results.remove(id);

    notifyChange();
    return job != null || task != null;
  }

  /**
   * Creates a thread factory that assigns readable, numbered names.
   * @param name thread name prefix
   * @return thread factory
   */
  private static ThreadFactory factory(final String name) {
    final AtomicInteger id = new AtomicInteger();
    return runnable -> {
      final Thread thread = new Thread(runnable, name + '-' + id.incrementAndGet());
      thread.setDaemon(true);
      return thread;
    };
  }
}
