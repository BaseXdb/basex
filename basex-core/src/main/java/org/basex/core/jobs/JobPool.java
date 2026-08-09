package org.basex.core.jobs;

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
  /** Interval between two memory checks (ms). */
  private static final long MEMORY_INTERVAL = 250;

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
  /** Jobs with a memory limit, watched by the memory guard. */
  private final Map<Job, Watch> watched = new ConcurrentHashMap<>();
  /** Indicates that a memory check is in progress. */
  private final AtomicBoolean checking = new AtomicBoolean();
  /** Timeout (ms). */
  private final long timeout;

  /** Handle for canceling the memory guard (can be {@code null}). */
  private ScheduledFuture<?> memoryFuture;

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
   * Watches the memory consumption of a job, which will be stopped if the heap usage grows beyond
   * the specified limit. Must be called from the thread that runs the job.
   * @param job job
   * @param mb maximum number of megabytes that may be allocated
   */
  public void watchMemory(final Job job, final long mb) {
    // enforce garbage collection: unreachable objects must not raise the baseline
    Performance.gc(2);
    final Thread thread = Thread.currentThread();
    final Watch watch = new Watch(Performance.memory() + (mb << 20), thread,
        Performance.allocated(thread));
    synchronized(watched) {
      watched.put(job, watch);
      if(memoryFuture == null) memoryFuture = scheduler.scheduleWithFixedDelay(this::checkMemory,
          MEMORY_INTERVAL, MEMORY_INTERVAL, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Stops watching the memory consumption of a job.
   * @param job job
   */
  public void unwatchMemory(final Job job) {
    synchronized(watched) {
      if(watched.remove(job) != null && watched.isEmpty()) {
        memoryFuture.cancel(false);
        memoryFuture = null;
      }
    }
  }

  /**
   * Stops the greediest job if a memory limit was exceeded.
   */
  private void checkMemory() {
    // skip garbage collection if all limits are met, or if a previous check is still running
    if(victim() == null || !checking.compareAndSet(false, true)) return;
    // collect garbage outside the scheduler thread, as it may take a long time
    pool.execute(() -> {
      try {
        Performance.gc(1);
        final Job job = victim();
        if(job != null) {
          unwatchMemory(job);
          job.outOfMemory();
        }
      } finally {
        checking.set(false);
      }
    });
  }

  /**
   * Returns the job to be stopped: of all jobs that exceed their memory limit, the one that has
   * allocated most memory since it was watched.
   * @return job, or {@code null} if all limits are met
   */
  private Job victim() {
    final long memory = Performance.memory();
    Job victim = null;
    long max = -1;
    for(final Map.Entry<Job, Watch> entry : watched.entrySet()) {
      final Job job = entry.getKey();
      final Watch watch = entry.getValue();
      if(job.stopped() || memory <= watch.limit()) continue;
      final long allocated = Performance.allocated(watch.thread()) - watch.allocated();
      if(allocated > max) {
        max = allocated;
        victim = job;
      }
    }
    return victim;
  }

  /**
   * Memory limit of a watched job.
   * @param limit heap usage above which the job will be stopped (bytes)
   * @param thread thread that runs the job
   * @param allocated bytes allocated by the thread when the job was watched
   */
  private record Watch(long limit, Thread thread, long allocated) { }

  /**
   * Returns all registered IDs.
   * @return sorted ID list
   */
  public TokenList ids() {
    final Set<String> set = new HashSet<>(results.keySet());
    set.addAll(active.keySet());
    set.addAll(tasks.keySet());

    // compare generated IDs by their job counter, custom IDs as strings
    final int pl = JobContext.PREFIX.length();
    final List<String> list = new ArrayList<>(set);
    list.sort((id1, id2) -> JobContext.generated(id1) && JobContext.generated(id2) ?
      Long.compare(Long.parseLong(id1.substring(pl)), Long.parseLong(id2.substring(pl))) :
      id1.compareTo(id2));

    final TokenList ids = new TokenList(list.size());
    for(final String id : list) ids.add(id);
    return ids;
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
