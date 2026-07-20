package org.basex.core.jobs;

import java.time.*;
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
  /** Interval (ms; no repetition: {@code 0}). */
  public final long interval;
  /** End time (@link {@link Long#MAX_VALUE}: no end). */
  public final long end;

  /** Job pool (for rescheduling). */
  private final JobPool jobs;
  /** Time zone for wall-clock interval arithmetic. */
  private final ZoneId zone = ZoneId.systemDefault();

  /** Next start time (ms). */
  public long start;
  /** Next start time as local wall-clock time. */
  private LocalDateTime next;
  /** Handle for cancelling the scheduled task. */
  private ScheduledFuture<?> future;
  /** Indicates that cancellation was requested before the future was assigned. */
  private boolean canceled;

  /**
   * Constructor.
   * @param job job
   * @param jobs job pool
   * @param delay delay (ms)
   * @param interval interval (ms; no repetition: {@code 0})
   * @param duration total duration (ms; no limit: {@link Long#MAX_VALUE})
   */
  public QueryJobTask(final QueryJob job, final JobPool jobs, final long delay, final long interval,
      final long duration) {
    this.job = job;
    this.jobs = jobs;
    this.interval = interval;
    final long time = System.currentTimeMillis();
    start = time + delay;
    end = duration == Long.MAX_VALUE ? duration : time + duration;
    next = Instant.ofEpochMilli(start).atZone(zone).toLocalDateTime();
  }

  @Override
  public synchronized void run() {
    if(interval != 0) {
      next = next.plus(Duration.ofMillis(interval));
      start = next.atZone(zone).toInstant().toEpochMilli();
    }
    if(interval == 0 || start >= end) {
      job.remove();
      cancel();
    } else {
      reschedule();
    }
    job.startIfNotRunning();
  }

  /**
   * Schedules the first execution.
   * @param delay initial delay (ms)
   */
  synchronized void schedule(final long delay) {
    future(jobs.schedule(this, delay));
  }

  /**
   * Schedules the next repetition.
   */
  private void reschedule() {
    if(canceled) return;
    try {
      future(jobs.schedule(this, Math.max(0, start - System.currentTimeMillis())));
    } catch(final RejectedExecutionException ex) {
      // scheduler has been shut down (application is closing): stop repeating
      Util.debug(ex);
    }
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
