package org.basex.core.jobs;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;
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
 * @author BaseX Team 2005-24, BSD License
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
   * @param info input info (can be {@code null})
   * @param notify notify function (ignored if {@code null})
   * @param qc query context (ignored if {@code null})
   * @throws QueryException query exception
   */
  public QueryJob(final QueryJobSpec job, final Context context, final InputInfo info,
      final Consumer<QueryJobResult> notify, final QueryContext qc) throws QueryException {

    this.job = job;
    this.notify = notify;
    jc().context = context;

    // check when job is to be started
    final JobOptions opts = job.options;
    final Item start = time(opts.get(JobOptions.START), info);
    long delay = start == null ? 0 : delay(start, 0, info);

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
    final Item end = time(opts.get(JobOptions.END), info);
    final long duration = end == null ? Long.MAX_VALUE : delay(end, delay, info);
    if(duration <= delay) throw JOBS_RANGE_X.get(info, end);

    // check job results are to be cached
    final boolean cache = opts.contains(JobOptions.CACHE) && opts.get(JobOptions.CACHE);
    if(cache && interval > 0) throw JOBS_OPTIONS.get(info);

    // number of scheduled and active tasks must not exceed limit
    final JobPool jobs = context.jobs;
    while(jobs.tasks.size() + jobs.active.size() >= JobPool.MAXQUERIES) {
      Performance.sleep(10);
      if(qc != null) qc.checkStop();
    }

    synchronized(jobs.tasks) {
      // custom job id: check if it is invalid or has already been assigned
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
        if(jobs.results.size() >= JobPool.MAXQUERIES) throw JOBS_OVERFLOW.get(info);
        jobs.results.put(id, result);
      }

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
   * @param info input info (can be {@code null})
   * @return item or {@code null}
   * @throws QueryException query exception
   */
  private static Item time(final String string, final InputInfo info) throws QueryException {
    // undefined
    if(string == null || string.isEmpty()) return null;
    // integer
    if(string.matches("^\\d+$")) return Int.get(Int.parse(token(string), info));
    // dayTimeDuration
    if(Dur.DTD.matcher(string).matches()) return new DTDur(token(string), info);
    // time
    if(ADate.TIME.matcher(string).matches()) return new Tim(token(string), info);
    // dateTime
    return new Dtm(token(string), info);
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
  private static long delay(final Item start, final long min, final InputInfo info)
      throws QueryException {

    final QueryDateTime qdt = new QueryDateTime();
    long ms;
    if(start instanceof Int) {
      // time
      ms = start.itr(info) * 60000;
      ms -= qdt.time.daySeconds().multiply(Dec.BD_1000).longValue();
      while(ms <= min) ms += 3600000;
    } else if(start instanceof DTDur) {
      // dayTimeDuration
      ms = ((DTDur) start).ms(info);
    } else if(start instanceof Dtm) {
      // dateTime
      ms = new DTDur((Dtm) start, qdt.datm, info).ms(info);
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

  @Override
  public void run() {
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
      result.time = perf.ns();

      // register job
      pushJob(qp);
      register(ctx);
      // reset timer
      perf.ns();
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

      if(ctx.jobs.active.containsKey(id)) {
        qp.close();
        unregister(ctx);
        popJob();
        qp = null;
        result.time += jc.performance.ns();
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
      if(notify != null) notify.accept(result);
      if(result.value != null && result.value.isEmpty()) ctx.jobs.results.remove(id);
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
