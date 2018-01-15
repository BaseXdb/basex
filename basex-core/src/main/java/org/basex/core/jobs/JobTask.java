package org.basex.core.jobs;

import java.util.*;

import org.basex.query.func.jobs.*;

/**
 * Scheduled job.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class JobTask extends TimerTask {
  /** Job. */
  public final Scheduled job;
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
   * @param interval interval (ms)
   * @param duration total duration (ms)
   */
  public JobTask(final Scheduled job, final JobPool jobs, final long delay,
      final long interval, final long duration) {

    this.job = job;
    this.jobs = jobs;
    this.interval = interval;
    final long time = System.currentTimeMillis();
    start = time + delay;
    end = duration == Long.MAX_VALUE ? duration : time + duration;

    jobs.tasks.put(job.jc().id(), this);
    if(interval > 0) {
      jobs.timer.scheduleAtFixedRate(this, delay, interval);
    } else {
      jobs.timer.schedule(this, delay);
    }
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
