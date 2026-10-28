package org.basex.core.jobs;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

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
  /** Maximum number of listed finished jobs per job type. */
  static final int MAX_FINISHED = 100;
  /** Maximum number of registered jobs. */
  static final int MAX_REGISTERED = 1 << 20;
  /** Interval between two memory checks (ms). */
  private static final long MEMORY_INTERVAL = 250;
  /** Time for which finished jobs are listed (ms). */
  private static final long FINISHED_TIME = 30000;

  /** Queued or running jobs. */
  public final Map<String, Job> active = new ConcurrentHashMap<>();
  /** Cached results. */
  public final Map<String, QueryJobResult> results = new ConcurrentHashMap<>();
  /** Scheduled tasks. */
  public final Map<String, QueryJobTask> tasks = new ConcurrentHashMap<>();
  /** Finished jobs, in the order of their completion. */
  private final LinkedHashMap<String, JobSnapshot> finished = new LinkedHashMap<>();
  /** Finished jobs, grouped by their type, in the order of their completion. */
  private final HashMap<String, ArrayDeque<JobSnapshot>> finishedTypes = new HashMap<>();

  /** Scheduler for delayed and periodic job tasks. */
  private final ScheduledThreadPoolExecutor scheduler =
      new ScheduledThreadPoolExecutor(1, factory("basex-scheduler"));
  /** Executor for running jobs. */
  private final ExecutorService pool = Executors.newCachedThreadPool(factory("basex-job"));
  /** Available slots for jobs running in parallel. */
  private final Semaphore slots = new Semaphore(MAX_RUNNING);
  /** Monitor, notified whenever a job or task completes. */
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
    // canceled tasks may reference query contexts: release them immediately
    scheduler.setRemoveOnCancelPolicy(true);
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
  void notifyChange() {
    synchronized(monitor) {
      monitor.notifyAll();
    }
  }

  /**
   * Waits until a condition is met; aborted if the waiting job is stopped.
   * @param job waiting job
   * @param done condition, checked whenever a job or task completes
   */
  public void await(final Job job, final BooleanSupplier done) {
    try {
      job.runStoppable(() -> {
        synchronized(monitor) {
          // bounded wait: state changes outside the pool are not notified
          while(!done.getAsBoolean()) monitor.wait(1000);
        }
        return null;
      });
    } catch(final IOException | InterruptedException ex) {
      Util.debug(ex);
      throw new JobException(Text.INTERRUPTED);
    }
  }

  /**
   * Stops all jobs before closing the application.
   */
  public synchronized void close() {
    // stop running tasks and queries
    scheduler.shutdownNow();
    for(final Job job : active.values()) job.stop();
    synchronized(monitor) {
      try {
        while(!active.isEmpty()) monitor.wait(1000);
      } catch(final InterruptedException ex) {
        Util.debug(ex);
        Thread.currentThread().interrupt();
      }
    }
    pool.shutdown();
  }

  /**
   * Schedules a one-off task for delayed execution.
   * @param task task
   * @param delay delay (ms)
   * @return cancellation handle ({@code null} if the application is closing)
   */
  public ScheduledFuture<?> schedule(final Runnable task, final long delay) {
    try {
      return scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    } catch(final RejectedExecutionException ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Discards a result after the timeout.
   * @param job job
   */
  void scheduleResult(final Job job) {
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
      if(memoryFuture == null) {
        try {
          memoryFuture = scheduler.scheduleWithFixedDelay(this::checkMemory,
              MEMORY_INTERVAL, MEMORY_INTERVAL, TimeUnit.MILLISECONDS);
        } catch(final RejectedExecutionException ex) {
          // scheduler has been shut down (application is closing): the job needs no guard
          Util.debug(ex);
          return;
        }
      }
      watched.put(job, watch);
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
   * Lists a finished job.
   * @param job snapshot of the job
   */
  void finish(final JobSnapshot job) {
    synchronized(finished) {
      unfinish(job.id());
      finished.put(job.id(), job);
      final ArrayDeque<JobSnapshot> jobs =
          finishedTypes.computeIfAbsent(job.type(), type -> new ArrayDeque<>());
      jobs.add(job);
      if(jobs.size() > MAX_FINISHED) remove(jobs.getFirst());
      prune();
    }
  }

  /**
   * Returns a finished job.
   * @param id ID
   * @return snapshot of the job, or {@code null} if it is not listed
   */
  public JobSnapshot finished(final String id) {
    synchronized(finished) {
      prune();
      return finished.get(id);
    }
  }

  /**
   * Removes a finished job.
   * @param id ID
   */
  void unfinish(final String id) {
    synchronized(finished) {
      final JobSnapshot job = finished.get(id);
      if(job != null) remove(job);
    }
  }

  /**
   * Removes the finished jobs that are listed for too long.
   */
  private void prune() {
    final long min = System.currentTimeMillis() - FINISHED_TIME;
    while(!finished.isEmpty()) {
      final JobSnapshot job = finished.firstEntry().getValue();
      if(job.end() >= min) break;
      remove(job);
    }
  }

  /**
   * Removes a finished job from both lists.
   * @param job snapshot of the job
   */
  private void remove(final JobSnapshot job) {
    finished.remove(job.id());
    final ArrayDeque<JobSnapshot> jobs = finishedTypes.get(job.type());
    jobs.remove(job);
    if(jobs.isEmpty()) finishedTypes.remove(job.type());
  }

  /**
   * Returns all registered IDs.
   * @param fin include finished jobs
   * @return sorted ID list
   */
  public TokenList ids(final boolean fin) {
    final Set<String> set = new HashSet<>(results.keySet());
    set.addAll(active.keySet());
    set.addAll(tasks.keySet());
    if(fin) {
      synchronized(finished) {
        prune();
        set.addAll(finished.keySet());
      }
    }

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
   * Returns the IDs of all scheduled and running query jobs.
   * @return IDs
   */
  public HashSet<String> queryIds() {
    // one-off jobs are removed from the tasks when they are started
    final HashSet<String> ids = new HashSet<>(tasks.keySet());
    active.forEach((id, job) -> {
      if(job instanceof QueryJob) ids.add(id);
    });
    return ids;
  }

  /**
   * Removes a job.
   * @param id ID
   * @return success flag
   */
  public boolean remove(final String id) {
    // stop scheduled task; the job is stopped as well, as it may not be registered yet
    final QueryJobTask task = tasks.remove(id);
    if(task != null) {
      task.cancel();
      task.job.stop();
    }
    // send stop signal to job
    final Job job = active.get(id);
    if(job != null) job.stop();
    // remove potentially cached result
    results.remove(id);
    unfinish(id);

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
