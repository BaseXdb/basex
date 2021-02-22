package org.basex.core.jobs;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.Map.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.server.Log.*;
import org.basex.util.*;

/**
 * Scheduled XQuery job.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QueryJob extends Job implements Runnable {
  /** Result. */
  private final QueryJobResult result = new QueryJobResult(this);
  /** Job specification. */
  private final QueryJobSpec job;
  /** Notify function. */
  private final Consumer<QueryJobResult> notify;

  /** Query processor. */
  private QueryProcessor qp;
  /** Remove flag. */
  private boolean remove;

  /**
   * Constructor.
   * @param job job info
   * @param context database context
   * @param ii input info
   * @param notify notify function (ignored if {@code null})
   * @throws QueryException query exception
   */
  public QueryJob(final QueryJobSpec job, final Context context, final InputInfo ii,
      final Consumer<QueryJobResult> notify) throws QueryException {

    this.job = job;
    this.notify = notify;
    jc().context = context;

    // check when job is to be started
    final JobsOptions opts = job.options;
    final Item start = time(opts.get(JobsOptions.START), ii);
    long delay = start == null ? 0 : delay(start, 0, ii);

    // check when job is to be repeated
    long interval = 0;
    final String inter = opts.get(JobsOptions.INTERVAL);
    if(inter != null && !inter.isEmpty()) {
      interval = ms(new DTDur(token(inter), ii));
      if(interval < 1000) throw JOBS_RANGE_X.get(ii, inter);
      while(delay < 0) delay += interval;
    }
    if(delay < 0) throw JOBS_RANGE_X.get(ii, start);

    // check when job is to be stopped
    final Item end = time(opts.get(JobsOptions.END), ii);
    final long duration = end == null ? Long.MAX_VALUE : delay(end, delay, ii);
    if(duration <= delay) throw JOBS_RANGE_X.get(ii, end);

    // check job results are to be cached
    final boolean cache = opts.contains(JobsOptions.CACHE) && opts.get(JobsOptions.CACHE);
    if(cache && interval > 0) throw JOBS_OPTIONS.get(ii);

    final JobPool jobs = context.jobs;
    synchronized(jobs.tasks) {
      // check if number of maximum queries has been reached
      if(jobs.active.size() >= JobPool.MAXQUERIES) throw JOBS_OVERFLOW.get(ii);

      // custom job id: check if it is invalid or has already been assigned
      String id = opts.get(JobsOptions.ID);
      if(id != null) {
        if(id.startsWith(JobContext.PREFIX)) throw JOBS_ID_INVALID_X.get(ii, id);
        if(jobs.tasks.containsKey(id) || jobs.active.containsKey(id) ||
           jobs.results.containsKey(id)) throw JOBS_ID_EXISTS_X.get(ii, id);
        jc().id(id);
      } else {
        id = jc().id();
      }
      if(cache) jobs.results.put(id, result);

      // create and schedule job task
      final QueryJobTask task = new QueryJobTask(this, jobs, delay, interval, duration);
      jobs.tasks.put(id, task);
      if(interval > 0) {
        jobs.timer.scheduleAtFixedRate(task, delay, interval);
      } else {
        jobs.timer.schedule(task, delay);
      }
    }
  }

  /**
   * Converts the specified start/end time to an item.
   * @param string start (integer, dayTimeDuration, dateTime, time); can be {@code null}
   * @param ii input info
   * @return item or {@code null}
   * @throws QueryException query exception
   */
  private static Item time(final String string, final InputInfo ii) throws QueryException {
    // undefined
    if(string == null || string.isEmpty()) return null;
    // integer
    if(string.matches("^\\d+$")) return Int.get(Int.parse(Str.get(string), ii));
    // dayTimeDuration
    if(Dur.DTD.matcher(string).matches()) return new DTDur(token(string), ii);
    // time
    if(ADate.TIME.matcher(string).matches()) return new Tim(token(string), ii);
    // dateTime
    return new Dtm(token(string), ii);
  }

  /**
   * Returns a delay.
   * @param start start (integer, dayTimeDuration, dateTime, time)
   * @param min minimum time
   * @param ii input info
   * @return milliseconds to wait
   * @throws QueryException query exception
   */
  private static long delay(final Item start, final long min, final InputInfo ii)
      throws QueryException {

    final QueryDateTime qdt = new QueryDateTime();
    long ms;
    if(start instanceof Int) {
      // time
      ms = start.itr(ii) * 60000;
      ms -= qdt.time.seconds().multiply(Dec.BD_1000).longValue();
      while(ms <= min) ms += 3600000;
    } else if(start instanceof DTDur) {
      // dayTimeDuration
      ms = ms((DTDur) start);
    } else if(start instanceof Dtm) {
      // dateTime
      ms = ms(new DTDur((Dtm) start, qdt.datm, ii));
    } else {
      // time
      ms = ms(new DTDur((Tim) start, qdt.time, ii));
      while(ms <= min) ms += 86400000;
    }
    return ms;
  }

  /**
   * Extracts the seconds from the specified date/duration item and returns it as milliseconds.
   * @param date date or duration
   * @return milliseconds
   */
  private static long ms(final ADateDur date) {
    return date.sec.multiply(Dec.BD_1000).longValue();
  }

  /**
   * Removes the job from the task list as soon as it has been activated.
   */
  void remove() {
    remove = true;
  }

  @Override
  public void run() {
    final JobContext jc = jc();
    final Context ctx = jc.context;
    final JobsOptions opts = job.options;
    log(LogType.REQUEST, opts.get(JobsOptions.LOG));

    qp = new QueryProcessor(job.query, opts.get(JobsOptions.BASE_URI), ctx);
    try {
      // parse, push and register query. order is important!
      final Performance perf = new Performance();
      for(final Entry<String, Value> binding : job.bindings.entrySet()) {
        final String key = binding.getKey();
        final Value value = binding.getValue();
        if(key.isEmpty()) qp.context(value);
        else qp.bind(key, value);
      }
      qp.parse();
      updating = qp.updating;
      result.time = perf.ns();

      // register job
      pushJob(qp);
      register(ctx);
      if(remove) ctx.jobs.tasks.remove(jc.id());

      // retrieve result
      result.value = qp.value().materialize(qp.qc, BASEX_FUNCTION_X, null);

    } catch(final JobException ex) {
      // query was interrupted: remove cached result
      ctx.jobs.results.remove(jc.id());
    } catch(final QueryException ex) {
      result.exception = ex;
    } catch(final Throwable ex) {
      result.exception = XQUERY_UNEXPECTED_X.get(null, ex);
    } finally {
      // close and invalidate query after result has been assigned. order is important!
      if(Boolean.TRUE.equals(opts.get(JobsOptions.CACHE))) {
        ctx.jobs.scheduleResult(this);
        state(JobState.CACHED);
      } else {
        state(JobState.SCHEDULED);
      }

      if(ctx.jobs.active.containsKey(jc.id())) {
        qp.close();
        unregister(ctx);
        popJob();
        qp = null;
        result.time += jc.performance.ns(false);
      }

      // write concluding log entry, invalidate performance measurements
      if(result.exception != null) {
        log(LogType.ERROR, result.exception.getLocalizedMessage());
      } else {
        log(LogType.OK, null);
      }
      jc.performance = null;

      if(remove) ctx.jobs.tasks.remove(jc.id());
      if(notify != null) notify.accept(result);
    }
  }

  /**
   * Creates a log entry.
   * @param type log type
   * @param info info string (can be {@code null})
   */
  private void log(final LogType type, final String info) {
    final String log = job.options.get(JobsOptions.LOG);
    if(log == null || log.isEmpty()) return;

    final JobContext jc = jc();
    final Context ctx = jc.context;
    ctx.log.write(type, info, jc.performance, "JOB:" + jc.id(), ctx);
  }


  @Override
  public void addLocks() {
    qp.addLocks();
  }

  @Override
  public String toString() {
    return job.simple ? job.query : job.options.get(JobsOptions.BASE_URI);
  }
}
