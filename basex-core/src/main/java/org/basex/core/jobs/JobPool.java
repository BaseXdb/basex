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
  /** Timer tasks. */
  public final Map<String, QueryJobTask> tasks = new ConcurrentHashMap<>();

  /** Timer. */
  final Timer timer = new Timer(true);
  /** Executor for running jobs. */
  private final ExecutorService pool = Executors.newCachedThreadPool(factory("basex-job"));
  /** Available slots for jobs running in parallel. */
  private final Semaphore slots = new Semaphore(MAX_RUNNING);
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
  }

  /**
   * Stops all jobs before closing the application.
   */
  public synchronized void close() {
    // stop running tasks and queries
    timer.cancel();
    for(final Job job : active.values()) job.stop();
    while(!active.isEmpty()) Performance.sleep(10);
    pool.shutdown();
  }

  /**
   * Discards a result after the timeout.
   * @param job job
   */
  public void scheduleResult(final Job job) {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        results.remove(job.jc().id());
      }
    }, timeout);
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
    final TimerTask task = tasks.remove(id);
    if(task != null) task.cancel();
    // send stop signal to job
    final Job job = active.get(id);
    if(job != null) job.stop();
    // remove potentially cached result
    results.remove(id);

    return job != null || task != null;
  }

  /**
   * Creates a thread factory that assigns readable, numbered names.
   * @param name thread name prefix
   * @return thread factory
   */
  private static ThreadFactory factory(final String name) {
    final AtomicInteger id = new AtomicInteger();
    return runnable -> new Thread(runnable, name + '-' + id.incrementAndGet());
  }
}
