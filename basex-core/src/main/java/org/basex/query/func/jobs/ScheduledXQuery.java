package org.basex.query.func.jobs;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Scheduled XQuery.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ScheduledXQuery extends Job implements Runnable {
  /** Result. */
  private final JobResult result = new JobResult(this);
  /** Variable bindings. */
  private final HashMap<String, Value> bindings;
  /** Input info. */
  private final InputInfo info;
  /** Caching flag. */
  private final boolean cache;
  /** Query string. */
  private final String query;
  /** Base URI. */
  private final String uri;
  /** Repeat flag. */
  private final boolean repeat;

  /** Query processor. */
  private QueryProcessor qp;

  /**
   * Constructor.
   * @param query query string
   * @param bindings variable bindings
   * @param opts options
   * @param info input info
   * @param qc query context
   * @param sc static context
   * @throws QueryException query exception
   */
  ScheduledXQuery(final String query, final HashMap<String, Value> bindings,
      final EvalOptions opts, final InputInfo info, final QueryContext qc,
      final StaticContext sc) throws QueryException {

    this.query = query;
    this.bindings = bindings;
    this.info = info;
    this.cache = opts.get(EvalOptions.CACHE);
    this.job().context = qc.context;

    final String bu = opts.get(EvalOptions.BASE_URI);
    uri = bu != null ? bu : string(sc.baseURI().string());

    // check when job is to be started
    final String del = opts.get(EvalOptions.START);
    final long delay = del.isEmpty() ? 0 : ms(del, 0, qc);

    // check when job is to be repeated
    long interval = 0;
    final String inter = opts.get(EvalOptions.INTERVAL);
    if(!inter.isEmpty()) interval = ms(new DTDur(Token.token(inter), info));
    if(interval < 1000 && interval != 0) throw JOBS_RANGE_X.get(info, inter);
    repeat = interval > 0;

    // check when job is to be stopped
    final String dur = opts.get(EvalOptions.END);
    final long duration = dur.isEmpty() ? Long.MAX_VALUE : ms(dur, delay, qc);

    // custom job id: check if it has already been assigned
    final JobPool pool = qc.context.jobs;
    String id = opts.get(EvalOptions.ID);
    if(id != null) {
      if(id.startsWith(JobContext.PREFIX))
        throw JOBS_ID_INVALID_X.get(info, id);
      if(pool.tasks.containsKey(id) || pool.active.containsKey(id) || pool.results.containsKey(id))
        throw JOBS_ID_EXISTS_X.get(info, id);
      job().id(id);
    } else {
      id = job().id();
    }

    if(cache) {
      if(repeat) throw JOBS_CONFLICT.get(info);
      pool.results.put(id, result);
    }

    // start job task and wait until job is registered or time was assigned
    new JobTask(this, pool, delay, interval, duration);
  }

  /**
   * Returns a delay.
   * @param string string with dayTimeDuration, date, or dateTime
   * @param min minimum time
   * @param qc query context
   * @return milliseconds to wait
   * @throws QueryException query exception
   */
  private long ms(final String string, final long min, final QueryContext qc)
      throws QueryException {

    qc.initDateTime();
    long ms = 0;
    if(Dur.DTD.matcher(string).matches()) {
      // dayTimeDuration
      ms = ms(new DTDur(Token.token(string), info));
    } else if(ADate.TIME.matcher(string).matches()) {
      // time
      ms = ms(new DTDur(new Tim(Token.token(string), info), qc.time, info));
      while(ms <= min) ms += 86400000;
    } else {
      // dateTime
      ms = ms(new DTDur(new Dtm(Token.token(string), info), qc.datm, info));
    }
    if(ms <= min) throw JOBS_RANGE_X.get(info, string);
    return ms;
  }

  /**
   * Extracts the seconds from the specified date/duration item and returns it as milliseconds.
   * @param date date or duration
   * @return milliseconds
   */
  private long ms(final ADateDur date) {
    return date.sec.multiply(BigDecimal.valueOf(1000)).longValue();
  }

  @Override
  public void run() {
    final Context ctx = job().context;
    try {
      // parse, push and register query. order is important!
      qp = parse();
      pushJob(qp);
      register(ctx);
      result.value = copy(qp.iter(), ctx, qp.qc);
    } catch(final JobException ex) {
      // query was interrupted: remove cached result
      ctx.jobs.results.remove(job().id());
    } catch(final QueryException ex) {
      result.exception = ex;
    } catch(final Throwable ex) {
      result.exception = BXXQ_UNEXPECTED_X.get(info, ex);
    } finally {
      // close and invalidate query after result has been assigned. order is important!
      if(cache) {
        ctx.jobs.scheduleResult(this);
        state(JobState.CACHED);
      } else {
        state(JobState.SCHEDULED);
      }

      if(qp != null) {
        result.time = job().performance.time();
        qp.close();
        unregister(ctx);
        popJob();
        qp = null;
        job().performance = null;
      }
    }
  }

  @Override
  public void databases(final LockResult lr) {
    qp.databases(lr);
  }

  /**
   * Returns a query processor. Creates a new instance if none exists.
   * @return query processor
   * @throws QueryException query exception
   */
  private QueryProcessor parse() throws QueryException {
    final QueryProcessor proc = new QueryProcessor(query, job().context);
    for(final Entry<String, Value> it : bindings.entrySet()) {
      final String key = it.getKey();
      final Value value = it.getValue();
      if(key.isEmpty()) proc.context(value);
      else proc.bind(key, value);
    }
    proc.parse(uri);
    updating = proc.updating;
    return proc;
  }

  /**
   * Creates a context-independent copy of the iterator results.
   * @param ctx database context
   * @param iter result iterator
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  static Value copy(final Iter iter, final Context ctx, final QueryContext qc)
      throws QueryException {

    final ValueBuilder vb = new ValueBuilder();
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      if(it instanceof FItem) throw BASX_FITEM_X.get(null, it);
      final Data data = it.data();
      if(data != null && !data.inMemory()) it = ((DBNode) it).dbNodeCopy(ctx.options);
      vb.add(it);
    }
    return vb.value();
  }

  @Override
  public String toString() {
    return query;
  }
}
