package org.basex.core.jobs;

import java.util.*;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.query.func.jobs.*;

/**
 * Job pool.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobPool {
  /** Number of queries to be queued. */
  public static final int MAXQUERIES = 1000;
  /** Queued or running jobs. */
  public final Map<String, Job> active = new ConcurrentHashMap<>();
  /** Cached results. */
  public final Map<String, JobResult> results = new ConcurrentHashMap<>();
  /** Timer tasks. */
  public final Map<String, TimerTask> tasks = new ConcurrentHashMap<>();

  /** Timer. */
  private final Timer timer = new Timer(true);
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
   * Registers a job (puts it on a queue).
   * @param job job
   */
  public void register(final Job job) {
    while(active.size() >= JobPool.MAXQUERIES) Thread.yield();
    active.put(job.job().id(), job);
  }

  /**
   * Unregisters a job.
   * @param job job
   */
  public void unregister(final Job job) {
    active.remove(job.job().id());
  }

  /**
   * Stops all jobs before closing the application.
   */
  public void close() {
    // stop running tasks and queries
    timer.cancel();
    for(final Job job : active.values()) job.stop();
    while(!active.isEmpty()) Thread.yield();
  }

  /**
   * Schedules a job.
   * @param job job
   * @param delay delay in milliseconds
   * @param interval milliseconds after which the job will be repeated
   */
  public void scheduleJob(final ScheduledXQuery job, final long delay, final long interval) {
    final String id = job.job().id();
    final TimerTask task = new TimerTask() {
      @Override
      public void run() {
        // skip execution if same job is still running
        if(active.get(id) == null) new Thread(job).start();
      }
    };
    tasks.put(id, task);
    if(interval > 0) {
      timer.scheduleAtFixedRate(task, delay, interval);
    } else {
      timer.schedule(task, delay);
    }
  }

  /**
   * Discards a result after the timeout.
   * @param job job
   */
  public void scheduleResult(final Job job) {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        results.remove(job.job().id());
      }
    }, timeout);
  }
}
