package org.basex.core.jobs;

import java.util.*;
import java.util.concurrent.*;

import org.basex.core.*;

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
  public final Map<String, Job> queued = new ConcurrentHashMap<>();
  /** Cached results. */
  public final Map<String, JobResult> results = new ConcurrentHashMap<>();

  /** Timer. */
  public final Timer timer = new Timer(true);
  /** Timeout (ms). */
  public final long timeout;

  /**
   * Constructor.
   * @param sopts static options
   */
  public JobPool(final StaticOptions sopts) {
    timeout = sopts.get(StaticOptions.CACHETIMEOUT) * 1000L;
  }

  /**
   * Adds a job.
   * @param job job
   */
  public void add(final Job job) {
    final String id = job.job().id();
    queued.put(id, job);
  }

  /**
   * Removes a job.
   * @param job job
   */
  public void remove(final Job job) {
    queued.remove(job.job().id());
  }

  /**
   * Stops all jobs before closing the application.
   */
  public void close() {
    // stop running queries
    for(final Job job : queued.values()) job.stop();
    while(!queued.isEmpty()) Thread.yield();
  }

  /**
   * Schedules a result.
   * @param job job
   */
  public void schedule(final Job job) {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        results.remove(job.job().id());
      }
    }, timeout);
  }
}
