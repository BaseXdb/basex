package org.basex.core.jobs;

import java.util.concurrent.*;

import org.basex.util.*;

/**
 * Scheduled job.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryJobTask implements Runnable {
  /** Job. */
  public final QueryJob job;
  /** Interval. */
  public final long interval;
  /** End time (@link {@link Long#MAX_VALUE}: no end). */
  public final long end;

  /** Next start time. */
  public long start;
  /** Handle for cancelling the scheduled task. */
  private ScheduledFuture<?> future;
  /** Indicates that cancellation was requested before the future was assigned. */
  private boolean canceled;

  /**
   * Constructor.
   * @param job job
   * @param delay delay (ms)
   * @param interval interval (ms; no repetition: {@code 0})
   * @param duration total duration (ms; no limit: {@link Long#MAX_VALUE})
   */
  public QueryJobTask(final QueryJob job, final long delay, final long interval,
      final long duration) {
    this.job = job;
    this.interval = interval;
    final long time = System.currentTimeMillis();
    start = time + delay;
    end = duration == Long.MAX_VALUE ? duration : time + duration;
  }

  @Override
  public synchronized void run() {
    // check if job needs to be evaluated repeatedly
    start += interval;
    if(interval == 0 || start >= end) {
      job.remove();
      cancel();
    }
    job.startIfNotRunning();
  }

  /**
   * Assigns the handle for canceling the scheduled task.
   * @param ftr scheduled future
   */
  synchronized void future(final ScheduledFuture<?> ftr) {
    future = ftr;
    if(canceled) ftr.cancel(false);
  }

  /**
   * Cancels the scheduled task, stopping any further repetitions.
   */
  synchronized void cancel() {
    canceled = true;
    if(future != null) future.cancel(false);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this)).append('[').append(job);
    sb.append(", interval:").append(interval);
    sb.append(", start:").append(start);
    if(end != Long.MAX_VALUE) sb.append(", end:").append(end);
    return sb.append(']').toString();
  }
}
