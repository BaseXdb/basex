package org.basex.core.jobs;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.log.*;

/**
 * Scheduled XQuery job.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryJob extends Job implements Runnable {
  /** Result. */
  private final QueryJobResult result = new QueryJobResult(this);
  /** Job specification. */
  private final QueryJobSpec job;
  /** Notify function. */
  private final Consumer<QueryJobResult> notify;
  /** Locks held by a caller that waits for this job; {@code null} if not applicable. */
  private final Locks callerLocks;
  /** Input info of the calling expression (for error reporting). */
  private final InputInfo info;

  /** Query processor. */
  private QueryProcessor qp;
  /** Remove flag. */
  private boolean remove;
  /** Running flag. */
  private final AtomicBoolean running = new AtomicBoolean(false);

  /**
   * Constructor, which creates and registers the specified job.
   * @param job job info
   * @param context database context
   * @param info input info (can be {@code null})
   * @param notify notify function (ignored if {@code null})
   * @param callerLocks locks held by a caller that waits for this job (can be {@code null})
   * @throws QueryException query exception
   */
  public QueryJob(final QueryJobSpec job, final Context context, final InputInfo info,
      final Consumer<QueryJobResult> notify, final Locks callerLocks) throws QueryException {

    this.job = job;
    this.notify = notify;
    this.callerLocks = callerLocks;
    this.info = info;
    jc().context = context;

    // check when job is to be started
    final JobOptions opts = job.options;
    final Item start = toTime(opts.get(JobOptions.START), info);
    long delay = start == null ? 0 : toDelay(start, 0, info);

    // check when job is to be repeated
    long interval = 0;
    final String inter = opts.get(JobOptions.INTERVAL);
    if(inter != null && !inter.isEmpty()) {
      interval = new DTDur(token(inter), info).ms(info);
      if(interval < 1000) throw JOBS_RANGE_X.get(info, inter);
      while(delay < 0) delay += interval;
    }
    if(delay < 0) throw JOBS_RANGE_X.get(info, start);

    // check when job is to be stopped
    final Item end = toTime(opts.get(JobOptions.END), info);
    final long duration = end == null ? Long.MAX_VALUE : toDelay(end, delay, info);
    if(duration <= delay) throw JOBS_RANGE_X.get(info, end);

    // check job results are to be cached
    final boolean cache = opts.contains(JobOptions.CACHE) && opts.get(JobOptions.CACHE);
    if(cache && interval > 0) throw JOBS_OPTIONS.get(info);

    // number of scheduled and active tasks must not exceed limit
    final JobPool jobs = context.jobs;
    if(jobs.tasks.size() + jobs.active.size() >= JobPool.MAX_REGISTERED)
      throw JOBS_OVERFLOW1_X.get(info, JobPool.MAX_REGISTERED);

    synchronized(jobs.tasks) {
      // custom job ID: check if it is invalid or has already been assigned
      String id = opts.get(JobOptions.ID);
      if(id != null) {
        if(id.startsWith(JobContext.PREFIX)) throw JOBS_ID_INVALID_X.get(info, id);
        if(jobs.tasks.containsKey(id) || jobs.active.containsKey(id) ||
           jobs.results.containsKey(id)) throw JOBS_ID_EXISTS_X.get(info, id);
        jc().id(id);
      } else {
        id = jc().id();
      }
      if(cache) {
        // check if too many query results are cached
        if(jobs.results.size() >= JobPool.MAX_CACHED) {
          throw JOBS_OVERFLOW2_X.get(info, JobPool.MAX_CACHED);
        }
        jobs.results.put(id, result);
      }

      // create and schedule job task
      final QueryJobTask task = new QueryJobTask(this, delay, interval, duration);
      jobs.tasks.put(id, task);
      jobs.schedule(task, delay, interval);
    }
  }

  /**
   * Converts the specified start/end time to an item.
   * @param string start (integer, dayTimeDuration, dateTime, time); can be {@code null}
   * @param info input info (can be {@code null})
   * @return item or {@code null}
   * @throws QueryException query exception
   */
  public static Item toTime(final String string, final InputInfo info) throws QueryException {
    // undefined
    if(string == null || string.isEmpty()) return null;
    // integer
    if(string.matches("^\\d+$")) return Itr.get(Itr.parse(token(string), info));
    // dayTimeDuration
    if(Dur.DTD.matcher(string).matches()) return new DTDur(token(string), info);
    // time
    if(ADate.TIME.matcher(string).matches()) return new Tim(token(string), info);
    // dateTime
    return new Dtm(token(string), BasicType.DATE_TIME, info);
  }

  /**
   * Returns the bindings for a query.
   * @return bindings
   */
  public HashMap<String, Value> bindings() {
    return job.bindings;
  }

  /**
   * Returns a delay.
   * @param start start (integer, dayTimeDuration, dateTime, time)
   * @param min minimum time
   * @param info input info (can be {@code null})
   * @return milliseconds to wait
   * @throws QueryException query exception
   */
  public static long toDelay(final Item start, final long min, final InputInfo info)
      throws QueryException {

    final QueryDateTime qdt = new QueryDateTime();
    long ms;
    if(start instanceof final Itr itr) {
      // time
      ms = itr.itr() * 60000;
      ms -= qdt.time.daySeconds().multiply(Dec.BD_1000).longValue();
      while(ms <= min) ms += 3600000;
    } else if(start instanceof final DTDur dur) {
      // dayTimeDuration
      ms = dur.ms(info);
    } else if(start instanceof final Dtm dtm) {
      // dateTime
      ms = new DTDur(dtm, qdt.datm, info).ms(info);
    } else {
      // time
      ms = new DTDur((Tim) start, qdt.time, info).ms(info);
      while(ms <= min) ms += 86400000;
    }
    return ms;
  }

  /**
   * Removes the job from the task list as soon as it has been activated.
   */
  void remove() {
    remove = true;
  }

  /**
   * Starts the job if it is not currently running.
   */
  void startIfNotRunning() {
    if(running.compareAndSet(false, true)) jc().context.jobs.execute(this);
  }

  @Override
  public void run() {
    // clear a possibly stale interrupt status
    Thread.interrupted();
    try {
      result.init();

      final JobContext jc = jc();
      final String id = jc.id();
      final Context ctx = jc.context;
      final JobOptions opts = job.options;

      String log = opts.get(JobOptions.LOG);
      if(log != null && log.isEmpty()) log = null;
      if(log != null) ctx.log.write(LogType.REQUEST, log, null, "JOB:" + id, ctx);

      final Performance perf = new Performance();
      qp = new QueryProcessor(job.query, opts.get(JobOptions.BASE_URI), ctx, null);
      boolean registered = false;
      try {
        // parse, push and register query. order is important!
        for(final Entry<String, Value> binding : job.bindings.entrySet()) {
          final String key = binding.getKey();
          final Value value = binding.getValue();
          if(key.isEmpty()) qp.context(value);
          else qp.variable(key, value);
        }
        qp.parse();
        updating = qp.updating;
        qp.compile();
        result.time = perf.nanoRuntime();

        // fail instead of blocking if a caller waiting for this job holds conflicting locks
        if(callerLocks != null && callerLocks.locking()) {
          qp.addLocks();
          final Locks required = qp.jc().locks.finish(ctx);
          if(callerLocks.conflicts(required)) throw JOBS_DEADLOCK_X.get(info, required);
        }

        // register job
        pushJob(qp);
        registered = true;
        register(ctx);
        // reset timer
        perf.nanoRuntime();
        if(remove) ctx.jobs.tasks.remove(id);

        // retrieve result; copy persistent database nodes
        result.value = qp.value().materialize(d -> d == null || d.inMemory(), null, qp.qc);
      } catch(final JobException ex) {
        // query was interrupted: remove cached result
        Util.debug(ex);
        ctx.jobs.results.remove(id);
      } catch(final QueryException ex) {
        result.exception = ex;
      } catch(final Throwable ex) {
        result.exception = XQUERY_UNEXPECTED_X.get(null, ex);
      } finally {
        // close and invalidate query after result has been assigned. order is important!
        if(Boolean.TRUE.equals(opts.get(JobOptions.CACHE))) {
          ctx.jobs.scheduleResult(this);
          state(JobState.CACHED);
        } else {
          state(JobState.SCHEDULED);
        }

        if(qp != null) {
          qp.close();
          if(registered) {
            unregister(ctx);
            popJob();
            result.time += jc.performance.nanoRuntime();
          }
          qp = null;
        }

        // write concluding log entry, invalidate performance measurements
        if(log != null) {
          final LogType type;
          String msg = null;
          if(result.exception != null) {
            type = LogType.ERROR;
            msg = result.exception.getMessage();
          } else {
            type = LogType.OK;
          }
          ctx.log.write(type, msg, perf, "JOB:" + id, ctx);
        }
        jc.performance = null;

        if(remove) ctx.jobs.tasks.remove(id);
        ctx.jobs.notifyChange();
        if(notify != null) notify.accept(result);
        if(result.value != null && result.value.isEmpty()) ctx.jobs.results.remove(id);
      }
    } finally {
      running.set(false);
    }
  }

  @Override
  public void addLocks() {
    qp.addLocks();
  }

  @Override
  public String toString() {
    return job.simple ? job.query : job.options.get(JobOptions.BASE_URI);
  }
}
