package org.basex.query.var;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Values and evaluation state of the static variables of a query, shared by all of its contexts.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GlobalValues {
  /**
   * State of a single static variable: it is either evaluated by a context, or it has a result.
   * @param context context that evaluates the variable (can be {@code null})
   * @param result value of the variable, or raised error (can be {@code null})
   */
  private record Global(QueryContext context, Object result) { }

  /** State of a variable that is not being evaluated and has no result. */
  private static final Global FREE = new Global(null, null);

  /** States of the variables that have been requested. */
  private final ConcurrentHashMap<StaticVar, Global> globals = new ConcurrentHashMap<>();
  /** Variables that contexts are waiting for. */
  private final IdentityHashMap<QueryContext, StaticVar> waiting = new IdentityHashMap<>();

  /**
   * Returns the value of a static variable and evaluates it if it has not been evaluated yet.
   * @param var static variable
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  Value value(final StaticVar var, final QueryContext qc) throws QueryException {
    // evaluated variables are returned without locking
    final Global global = globals.get(var);
    if(global != null && global.result != null) return result(var, global.result);

    final Value cached = acquire(var, qc);
    if(cached != null) return cached;

    try {
      final Value value = var.compute(qc);
      release(var, value);
      return value;
    } catch(final QueryException | RuntimeException | Error ex) {
      release(var, ex);
      throw ex;
    }
  }

  /**
   * Acquires the right to evaluate a variable, or returns its value.
   * @param var static variable
   * @param qc query context
   * @return value, or {@code null} if the caller must evaluate the variable
   * @throws QueryException query exception
   */
  private synchronized Value acquire(final StaticVar var, final QueryContext qc)
      throws QueryException {
    while(true) {
      final Global global = globals.getOrDefault(var, FREE);
      if(global.result != null) return result(var, global.result);
      if(global.context == null) {
        // variable is not being evaluated: take ownership
        globals.put(var, new Global(qc, null));
        return null;
      }
      // another context is evaluating this variable: check for circular dependencies, wait
      if(circular(var, qc)) throw CIRCVAR_X.get(var.info, var.name());
      waiting.put(qc, var);
      try {
        // bounded wait: a stopped job must not wait for a variable that is never released
        wait(1000);
      } catch(final InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new JobException(Text.INTERRUPTED, ex);
      } finally {
        waiting.remove(qc);
      }
      qc.checkStop();
    }
  }

  /**
   * Caches the result of an evaluation, releases the evaluation right and wakes waiting contexts.
   * @param var static variable
   * @param result value, or raised error
   */
  private synchronized void release(final StaticVar var, final Object result) {
    globals.put(var, new Global(null, result));
    notifyAll();
  }

  /**
   * Returns the result of a finished evaluation, or rethrows the error that was raised.
   * @param var static variable
   * @param result value, or raised error
   * @return value
   * @throws QueryException query exception
   */
  private static Value result(final StaticVar var, final Object result) throws QueryException {
    if(result instanceof final Value value) return value;
    if(result instanceof final RuntimeException ex) throw ex;
    if(result instanceof final Error ex) throw ex;
    throw var.error((QueryException) result);
  }

  /**
   * Checks if waiting for a variable would introduce a circular dependency, that is, if the chain
   * of blocked evaluations leads back to the current context or one of its ancestors.
   * @param var static variable
   * @param qc current query context
   * @return result of check
   */
  private boolean circular(final StaticVar var, final QueryContext qc) {
    for(StaticVar sv = var; sv != null;) {
      final QueryContext context = evaluating(sv);
      if(context == null) return false;
      if(nested(qc, context)) return true;
      sv = waitingFor(context);
    }
    return false;
  }

  /**
   * Returns the context that is currently evaluating a variable.
   * @param var static variable
   * @return context, or {@code null} if the variable is not being evaluated
   */
  private QueryContext evaluating(final StaticVar var) {
    return globals.getOrDefault(var, FREE).context;
  }

  /**
   * Returns the variable that a context, or a context nested in it, is waiting for.
   * @param qc query context
   * @return variable, or {@code null} if no context is waiting
   */
  private StaticVar waitingFor(final QueryContext qc) {
    for(final Map.Entry<QueryContext, StaticVar> entry : waiting.entrySet()) {
      if(nested(entry.getKey(), qc)) return entry.getValue();
    }
    return null;
  }

  /**
   * Checks if a context is identical to another one or nested in it.
   * @param qc query context
   * @param ancestor potential ancestor
   * @return result of check
   */
  private static boolean nested(final QueryContext qc, final QueryContext ancestor) {
    for(QueryContext context = qc; context != null; context = context.parent) {
      if(context == ancestor) return true;
    }
    return false;
  }
}
