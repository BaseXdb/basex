package org.basex.query.func.jobs;

import static org.basex.query.QueryError.*;

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
 * Asynchronous query.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class ScheduledXQuery extends Job implements Runnable {
  /** Result. */
  private final JobResult result = new JobResult();
  /** Input info. */
  private final InputInfo info;
  /** Query processor. */
  private final QueryProcessor qp;
  /** Database context. */
  private final Context context;

  /**
   * Constructor.
   * @param qp query processor
   * @param info input info
   * @param cache cache results
   */
  ScheduledXQuery(final QueryProcessor qp, final InputInfo info, final boolean cache) {
    this.qp = qp;
    this.info = info;
    context = qp.qc.context;
    updating = qp.updating;

    // cache result
    final JobPool pool = context.jobs;
    final String id = job().id();
    if(cache) pool.results.put(id, result);

    // start thread; do not continue unless job has been added to the job pool
    new Thread(this).start();
    while(pool.jobs.get(id) != null) Thread.yield();
  }

  @Override
  public void run() {
    try {
      // register query
      pushJob(qp);
      register(context);
      result.value = copy(qp.iter(), context, qp.qc);
      result.time = System.currentTimeMillis();
    } catch(final JobException ex) {
      // query was interrupted: remove cached result
      context.jobs.results.remove(job().id());
    } catch(final QueryException ex) {
      result.exception = ex;
    } catch(final Throwable ex) {
      result.exception = BXXQ_UNEXPECTED_X.get(info, ex);
    } finally {
      // close and invalidate query after result has been assigned
      qp.close();
      unregister(context);
      popJob();
    }
  }

  @Override
  public void databases(final LockResult lr) {
    qp.databases(lr);
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
    return qp.toString();
  }
}
