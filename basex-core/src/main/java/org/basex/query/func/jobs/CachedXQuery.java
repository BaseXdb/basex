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
final class CachedXQuery extends Job implements Runnable {
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
   * @param cache cache results
   * @param info input info
   */
  CachedXQuery(final QueryProcessor qp, final boolean cache, final InputInfo info) {
    this.qp = pushJob(qp);
    this.info = info;
    context = qp.qc.context;
    updating = qp.updating;
    if(cache) context.jobs.results.put(job().id(), result);
  }

  @Override
  public void run() {
    try {
      // register query
      register(context);
      result.value = copy(qp.iter(), context);
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
   * @return result
   * @throws QueryException query exception
   */
  static Value copy(final Iter iter, final Context ctx) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(Item it; (it = iter.next()) != null;) {
      if(it instanceof FItem) throw BASX_FITEM_X.get(null, it);
      final Data data = it.data();
      if(data != null && !data.inMemory()) it = ((DBNode) it).dbNodeCopy(ctx.options);
      vb.add(it);
    }
    return vb.value();
  }
}
