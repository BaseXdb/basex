package org.basex.core.jobs;

import java.util.*;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.util.Performance;

/**
 * Job pool.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JobPool {
  /** Number of queries to be queued. */
  static final int MAXQUERIES = 1000;

  /** Queued or running jobs. */
  public final Map<String, Job> active = new ConcurrentHashMap<>();
  /** Cached results. */
  public final Map<String, QueryJobResult> results = new ConcurrentHashMap<>();
  /** Timer tasks. */
  public final Map<String, QueryJobTask> tasks = new ConcurrentHashMap<>();

  /** Timer. */
  final Timer timer = new Timer(true);
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
    while(active.size() >= MAXQUERIES) Performance.sleep(1);
    active.put(job.jc().id(), job);
  }

  /**
   * Unregisters a job.
   * @param job job
   */
  public void unregister(final Job job) {
    active.remove(job.jc().id());
  }

  /**
   * Stops all jobs before closing the application.
   */
  public synchronized void close() {
    // stop running tasks and queries
    timer.cancel();
    for(final Job job : active.values()) job.stop();
    while(!active.isEmpty()) Performance.sleep(1);
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
}
