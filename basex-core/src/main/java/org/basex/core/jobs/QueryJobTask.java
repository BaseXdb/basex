package org.basex.core.jobs;

import java.util.*;

/**
 * Scheduled job.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QueryJobTask extends TimerTask {
  /** Job. */
  public final QueryJob job;
  /** Job pool. */
  public final JobPool jobs;
  /** Interval. */
  public final long interval;
  /** End time (@link {@link Long#MAX_VALUE}: no end). */
  public final long end;

  /** Next start time. */
  public long start;

  /**
   * Constructor.
   * @param job job
   * @param jobs job pool
   * @param delay delay (ms)
   * @param interval interval (ms; no repetition: {@code 0})
   * @param duration total duration (ms; no limit: {@link Long#MAX_VALUE})
   */
  public QueryJobTask(final QueryJob job, final JobPool jobs, final long delay,
      final long interval, final long duration) {

    this.job = job;
    this.jobs = jobs;
    this.interval = interval;
    final long time = System.currentTimeMillis();
    start = time + delay;
    end = duration == Long.MAX_VALUE ? duration : time + duration;
  }

  @Override
  public void run() {
    // check if job needs to be evaluated repeatedly
    start += interval;
    if(interval == 0 || start >= end) {
      job.remove();
      cancel();
    }
    // skip execution if same job is still running
    if(!jobs.active.containsKey(job.jc().id())) new Thread(job).start();
  }
}
