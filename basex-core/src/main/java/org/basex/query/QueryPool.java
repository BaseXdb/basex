package org.basex.query;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Pool with asynchronous queries.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class QueryPool {
  /** Queries. */
  private final Map<String, Query> queries = new ConcurrentHashMap<>();

  /**
   * Constructor.
   */
  public QueryPool() { }

  /**
   * Adds and registers query.
   * @param qp query processor
   * @param cache cache results
   * @param info input info
   * @return query id
   */
  public String add(final QueryProcessor qp, final boolean cache, final InputInfo info) {
    final String id = "Query-" + UUID.randomUUID();
    queries.put(id, new Query(qp, id, cache, info));
    return id;
  }

  /**
   * Retrieves the query result.
   * @param id id
   * @param info input info
   * @return result
   * @throws QueryException query exception
   */
  public Value result(final String id, final InputInfo info) throws QueryException {
    final Query query = get(id, info);
    if(query.qp != null) throw ASYNC_RUNNING_X.get(info, id);

    try {
      if(query.result != null) return query.result;
      throw query.exception;
    } finally {
      queries.remove(id);
    }
  }

  /**
   * Checks if the specified query is running.
   * @param id id
   * @param info input info
   * @return result of check, or {@code null} if the query is unknown
   * @throws QueryException query exception
   */
  public boolean isRunning(final String id, final InputInfo info) throws QueryException {
    return get(id, info).qp != null;
  }

  /**
   * Checks if the specified query is running.
   * @param id id
   * @param info input info
   * @return result of check, or {@code null} if the query is unknown
   * @throws QueryException query exception
   */
  private Query get(final String id, final InputInfo info) throws QueryException {
    final Query query = queries.get(id);
    if(query != null) return query;
    throw ASYNC_WHICH_X.get(info, id);
  }

  /**
   * Stops a running query.
   * @param id id
   * @param info input info
   * @throws QueryException query exception
   */
  public void stop(final String id, final InputInfo info) throws QueryException {
    get(id, info).close();
  }

  /**
   * Closes all running queries.
   */
  public void close() {
    for(final Query query : queries.values()) query.close();
    queries.clear();
  }

  /**
   * Creates a context-independent copy of iterator results.
   * @param ctx database context
   * @param iter result iterator
   * @return result
   * @throws QueryException query exception
   */
  public Value copy(final Iter iter, final Context ctx) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(Item it; (it = iter.next()) != null;) {
      if(it instanceof FItem) throw BASX_FITEM_X.get(null, it);
      final Data data = it.data();
      if(data != null && !data.inMemory()) it = ((DBNode) it).dbNodeCopy(ctx.options);
      vb.add(it);
    }
    return vb.value();
  }

  /**
   * Representation of an asynchronous query.
   *
   * @author BaseX Team 2005-16, BSD License
   * @author Christian Gruen
   */
  final class Query extends Thread {
    /** Query id. */
    final String id;
    /** Input info. */
    final InputInfo info;
    /** Cache results. */
    boolean cache;
    /** Query processor. */
    QueryProcessor qp;
    /** Query result. */
    Value result;
    /** Exception. */
    QueryException exception;

    /**
     * Constructor.
     * @param qp query processor
     * @param id query id
     * @param cache cache results
     * @param info input info
     */
    Query(final QueryProcessor qp, final String id, final boolean cache, final InputInfo info) {
      this.qp = qp;
      this.id = id;
      this.info = info;
      this.cache = cache;
      start();
    }

    @Override
    public void run() {
      final Context ctx = qp.qc.context;
      QueryException exc = null;
      Value value = null;
      try {
        // register and evaluate query, cache results
        ctx.register(qp);
        value = copy(qp.iter(), ctx);
      } catch(final ProcException ex) {
        // query was interrupted: do not cache it
        cache = false;
      } catch(final QueryException ex) {
        exc = ex;
      } catch(final Throwable ex) {
        exc = ASYNC_UNEXP_X.get(info, ex);
      } finally {
        if(cache) {
          // cache result, discard it after timeout
          result = value;
          exception = exc;
          new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
              queries.remove(id);
            }
          }, ctx.soptions.get(StaticOptions.ASYNCTIMEOUT) * 1000L);
        } else {
          // no caching: immediately remove result
          queries.remove(id);
        }

        // close and invalidate query after result has been assigned
        qp.close();
        ctx.unregister(qp);
        qp = null;
      }
    }

    /**
     * Stops the query.
     */
    void close() {
      if(qp != null) qp.stop();
    }
  }
}
