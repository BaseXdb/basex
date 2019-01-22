package org.basex.core.jobs;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Scheduled XQuery job.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class QueryJob extends Job implements Runnable {
  /** Result. */
  private final QueryJobResult result = new QueryJobResult(this);
  /** Job info. */
  private final QueryJobSpec job;

  /** Query processor. */
  private QueryProcessor qp;
  /** Remove flag. */
  private boolean remove;

  /**
   * Constructor.
   * @param job job info
   * @param info input info
   * @param ctx database context
   * @throws QueryException query exception
   */
  public QueryJob(final QueryJobSpec job, final InputInfo info, final Context ctx)
      throws QueryException {

    this.job = job;
    jc().context = ctx;

    // check when job is to be started
    final JobsOptions opts = job.options;
    final String start = opts.get(JobsOptions.START);
    long delay = start == null || start.isEmpty() ? 0 : delay(start, 0, info);

    // check when job is to be repeated
    long interval = 0;
    final String inter = opts.get(JobsOptions.INTERVAL);
    if(inter != null && !inter.isEmpty()) {
      interval = ms(new DTDur(token(inter), info));
      if(interval < 1000) throw JOBS_RANGE_X.get(info, inter);
      while(delay < 0) delay += interval;
    }
    if(delay < 0) throw JOBS_RANGE_X.get(info, start);

    // check when job is to be stopped
    final String end = opts.get(JobsOptions.END);
    final long duration = end == null || end.isEmpty() ? Long.MAX_VALUE : delay(end, delay, info);
    if(duration <= delay) throw JOBS_RANGE_X.get(info, end);

    // check job results are to be cached
    final boolean cache = opts.contains(JobsOptions.CACHE) && opts.get(JobsOptions.CACHE);
    if(cache && interval > 0) throw JOBS_OPTIONS.get(info);

    final JobPool jobs = ctx.jobs;
    synchronized(jobs.tasks) {
      // custom job id: check if it is invalid or has already been assigned
      String id = opts.get(JobsOptions.ID);
      if(id != null) {
        if(id.startsWith(JobContext.PREFIX)) throw JOBS_ID_INVALID_X.get(info, id);
        if(jobs.tasks.containsKey(id) || jobs.active.containsKey(id) ||
           jobs.results.containsKey(id)) throw JOBS_ID_EXISTS_X.get(info, id);
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
   * Returns a delay.
   * @param string string with dayTimeDuration, date, or dateTime
   * @param min minimum time
   * @param info input info
   * @return milliseconds to wait
   * @throws QueryException query exception
   */
  private static long delay(final String string, final long min, final InputInfo info)
      throws QueryException {

    final QueryDateTime qdt = new QueryDateTime();
    long ms;
    if(Dur.DTD.matcher(string).matches()) {
      // dayTimeDuration
      ms = ms(new DTDur(token(string), info));
    } else if(ADate.TIME.matcher(string).matches()) {
      // time
      ms = ms(new DTDur(new Tim(token(string), info), qdt.time, info));
      while(ms <= min) ms += 86400000;
    } else {
      // dateTime
      ms = ms(new DTDur(new Dtm(token(string), info), qdt.datm, info));
    }
    return ms;
  }

  /**
   * Extracts the seconds from the specified date/duration item and returns it as milliseconds.
   * @param date date or duration
   * @return milliseconds
   */
  private static long ms(final ADateDur date) {
    return date.sec.multiply(BigDecimal.valueOf(1000)).longValue();
  }

  /**
   * Removes the job from the task list as soon as it has been activated.
   */
  public void remove() {
    remove = true;
  }

  @Override
  public void run() {
    final JobContext jc = jc();
    final Context ctx = jc.context;
    final JobsOptions opts = job.options;
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
      result.value = materialize(qp.iter(), qp.qc);
    } catch(final JobException ex) {
      // query was interrupted: remove cached result
      ctx.jobs.results.remove(jc.id());
    } catch(final QueryException ex) {
      result.exception = ex;
    } catch(final Throwable ex) {
      result.exception = XQUERY_UNEXPECTED_X.get(null, ex);
    } finally {
      // close and invalidate query after result has been assigned. order is important!
      final Boolean cache = opts.get(JobsOptions.CACHE);
      if(cache != null && cache) {
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
        result.time += jc.performance.ns();
        // invalidates the performance measurements
        jc.performance = null;
      }

      if(remove) ctx.jobs.tasks.remove(jc.id());
    }
  }

  @Override
  public void addLocks() {
    qp.addLocks();
  }

  /**
   * Creates a materialized, context-independent version of the iterator results.
   * @param iter result iterator
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  public static Value materialize(final Iter iter, final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      vb.add(item.materialize(qc, item.persistent()));
    }
    return vb.value();
  }

  @Override
  public String toString() {
    final String uri = job.options.get(JobsOptions.BASE_URI);
    return uri == null || uri.isEmpty() ? job.query : uri;
  }
}
