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
  /** Running jobs. */
  public final Map<String, Job> jobs = new ConcurrentHashMap<>();
  /** Cached results. */
  public final Map<String, JobResult> results = new ConcurrentHashMap<>();

  /**
   * Constructor.
   * @param sopts static options
   */
  public JobPool(final StaticOptions sopts) {
    // check cached results every 60 seconds
    final Timer timer = new Timer(true);
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        final int co = sopts.get(StaticOptions.CACHETIMEOUT);
        for(final Map.Entry<String, JobResult> entry : results.entrySet()) {
          final JobResult result = entry.getValue();
          if(result != null && !result.valid(co)) jobs.remove(entry.getKey());
        }
      }
    }, 0, 60000);
  }

  /**
   * Adds a job.
   * @param job job
   */
  public void add(final Job job) {
    jobs.put(job.job().id(), job);
  }

  /**
   * Removes a job.
   * @param job job
   */
  public void remove(final Job job) {
    jobs.remove(job.job().id());
  }

  /**
   * Stops all jobs before closing the application.
   */
  public void close() {
    // stop running queries
    for(final Job job : jobs.values()) job.stop();
    while(!jobs.isEmpty()) Thread.yield();
  }
}
