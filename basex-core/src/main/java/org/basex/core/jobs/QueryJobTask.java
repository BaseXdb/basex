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
  /** Cron expression (can be {@code null}). */
  public final Cron cron;
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
   * @param cron cron expression (can be {@code null})
   * @param first first cron occurrence ({@code null} if no cron expression was supplied)
   * @param duration total duration (ms; no limit: {@link Long#MAX_VALUE})
   */
  public QueryJobTask(final QueryJob job, final JobPool jobs, final long delay, final long interval,
      final Cron cron, final LocalDateTime first, final long duration) {
    this.job = job;
    this.jobs = jobs;
    this.interval = interval;
    this.cron = cron;
    final long time = System.currentTimeMillis();
    end = duration == Long.MAX_VALUE ? duration : time + duration;
    if(first != null) {
      // cron occurrences are exact wall-clock times
      next = first;
      start = millis(first);
    } else {
      // interval starts are exact instants
      start = time + delay;
      next = Instant.ofEpochMilli(start).atZone(zone).toLocalDateTime();
    }
  }

  @Override
  public synchronized void run() {
    next = cron != null ? cron.next(next) :
      interval != 0 ? next.plus(Duration.ofMillis(interval)) : null;
    if(next != null) start = millis(next);
    if(next == null || start >= end) {
      job.remove();
      cancel();
    } else {
      reschedule();
    }
    job.startIfNotRunning();
  }

  /**
   * Projects a local time onto the time zone and returns its milliseconds.
   * @param dt local date and time
   * @return milliseconds since 01/01/1970
   */
  private long millis(final LocalDateTime dt) {
    return dt.atZone(zone).toInstant().toEpochMilli();
  }

  /**
   * Schedules the first execution.
   */
  synchronized void schedule() {
    future(jobs.schedule(this, Math.max(0, start - System.currentTimeMillis())));
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
    if(cron != null) sb.append(", cron:").append(cron);
    else sb.append(", interval:").append(interval);
    sb.append(", start:").append(start);
    if(end != Long.MAX_VALUE) sb.append(", end:").append(end);
    return sb.append(']').toString();
  }
}
